package at.discord.bot.service.binance;

import at.discord.bot.config.binance.BinanceConfigProperties;
import at.discord.bot.persistent.PriceAlertRepository;
import at.discord.bot.persistent.model.PriceAlertEntity;
import com.binance.connector.client.impl.SpotClientImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SymbolService {

    private final BinanceConfigProperties binanceConfigProperties;
    private final PriceAlertRepository priceAlertRepository;

    @Cacheable("symbols")
    public List<String> getAllAvailableSymbols() {
        SpotClientImpl spotClient = new SpotClientImpl(binanceConfigProperties.getBaseUrl());
        String result = spotClient.createMarket().exchangeInfo(Map.of());
        JSONObject jsonObject = new JSONObject(result);

        JSONArray symbolsArray = jsonObject.getJSONArray("symbols");
        List<String> availableSymbols = new ArrayList<>();

        // Extract symbols from the response
        for (int i = 0; i < symbolsArray.length(); i++) {
            JSONObject symbolObject = symbolsArray.getJSONObject(i);
            String symbol = symbolObject.getString("symbol");
            if ("TRADING".equals(symbolObject.getString("status"))) {
                availableSymbols.add(symbol);
            }
        }

        return availableSymbols;
    }

    public List<String> getMonitoredSymbols() {
        List<PriceAlertEntity> allAlerts = priceAlertRepository.findAll();
        if (allAlerts.isEmpty()) {
            return List.of();
        }

        return allAlerts.stream()
                .map(PriceAlertEntity::getSymbol)
                .distinct()
                .collect(Collectors.toList());
    }
}
