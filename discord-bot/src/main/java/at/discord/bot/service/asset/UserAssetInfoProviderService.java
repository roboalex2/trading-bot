package at.discord.bot.service.asset;

import at.discord.bot.model.asset.UserAsset;
import at.discord.bot.model.binance.BinanceContext;
import at.discord.bot.service.binance.SymbolPriceMonitorService;
import at.discord.bot.service.binance.SymbolProviderService;
import at.discord.bot.service.binance.credential.BinanceContextProviderService;
import at.discord.bot.service.candle.BarSeriesHolderService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserAssetInfoProviderService {

    private static final String BTC_USD_SYMBOL = "BTCUSDC";

    private final SymbolPriceMonitorService symbolPriceMonitorService;
    private final SymbolProviderService symbolProviderService;
    private final BinanceContextProviderService binanceContextProviderService;
    private final BarSeriesHolderService barSeriesHolderService;
    private final ObjectMapper objectMapper;



    public List<UserAsset> getUserAssets(Long userId) {
        if (userId == null) {
            return List.of();
        }

        BinanceContext userContext = binanceContextProviderService.getUserContext(userId);
        try {
            String assetResponse = userContext.getSpotClient().createWallet().getUserAsset(new HashMap<>(Map.of(
                    "timestamp", Instant.now().toEpochMilli(),
                    "needBtcValuation", true
            )));

            BigDecimal btcPrice = getCurrentBtcPrice();

            List<UserAsset> assets = new ArrayList<>();
            JSONArray assetsArray = new JSONArray(assetResponse);
            for (int i = 0; i < assetsArray.length(); i++) {
                JSONObject jsonAsset = assetsArray.getJSONObject(i);
                UserAsset asset = objectMapper.readValue(jsonAsset.toString(), UserAsset.class);
                if (btcPrice != null) {
                    asset.setUsdcValuation(btcPrice.multiply(asset.getBtcValuation()));
                } else {
                    asset.setUsdcValuation(new BigDecimal(-1));
                }
                asset.setDiscordUserId(userId);
                assets.add(asset);
            }

            return assets;
        } catch (JSONException | JsonProcessingException exception) {
            throw new RuntimeException("Failed to retrieve assets from binance. Details: " + exception.getMessage());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    private void registerBtcMonitoring() {
        if (symbolProviderService.getAllAvailableSymbols().contains(BTC_USD_SYMBOL)) {
            symbolPriceMonitorService.registerSymbol(BTC_USD_SYMBOL);
        }
    }

    public BigDecimal getCurrentBtcPrice() {
        try {
            if (symbolProviderService.getAllAvailableSymbols().contains(BTC_USD_SYMBOL)) {
                BarSeries barSeries = barSeriesHolderService.getBarSeries(BTC_USD_SYMBOL);
                if (barSeries.getLastBar() != null) {
                    return new BigDecimal(String.valueOf(barSeries.getLastBar().getClosePrice()));
                }
            }
        } catch (Exception exception) {
            return null;
        }
        return null;
    }
}
