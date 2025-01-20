package at.discord.bot.service.binance.symbol;

import at.discord.bot.mapper.CandlestickMapper;
import at.discord.bot.service.candle.BarSeriesHolderService;
import com.binance.connector.client.WebSocketStreamClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.ta4j.core.BaseBar;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymbolPriceMonitorService {

    private final CandlestickMapper candlestickMapper;
    private final WebSocketStreamClient webSocketStreamClient;
    private final BarSeriesHolderService barSeriesHolderService;
    private final SymbolProviderService symbolProviderService;

    private Map<String, Integer> openStreams = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    private void openWebsocketStream() {
        symbolProviderService.getMonitoredSymbols().forEach(this::registerSymbol);
    }

    public synchronized void registerSymbol(String symbol) {
        if (openStreams.get(symbol) != null) {
            int streamId = openStreams.get(symbol);
            webSocketStreamClient.closeConnection(streamId);
            openStreams.remove(symbol);
        }

        int streamId = webSocketStreamClient.klineStream(
                symbol.toLowerCase(),
                "1s",
                msg -> {},
                msg -> priceUpdateEvent(symbol, msg),
                (i, m) -> {},
                (i, m) -> websocketClosureEvent(symbol, i, m),
                (i, m) -> websocketFailureEvent(symbol, i, m)
        );
        openStreams.put(symbol, streamId);
    }

    private void priceUpdateEvent(String symbol, String message) {
        JSONObject jsonKline = new JSONObject(message).getJSONObject("k");
        BaseBar secondKline = candlestickMapper.map(jsonKline);
        try {
            barSeriesHolderService.updateBarSeries(symbol, secondKline);
        } catch (RuntimeException exception) {
            log.warn("Failure on priceUpdateEvent: ", exception);
        }
    }

    private void websocketClosureEvent(String symbol, int i, String message) {
        log.warn(message);
        openStreams.remove(symbol);
        registerSymbol(symbol);
    }

    private void websocketFailureEvent(String symbol, Throwable throwable, Response response) {
        log.warn(Optional.ofNullable(response).map(Response::message).orElse("Websocket Failure for price update: ") , throwable);
        openStreams.remove(symbol);
        registerSymbol(symbol);
    }
}