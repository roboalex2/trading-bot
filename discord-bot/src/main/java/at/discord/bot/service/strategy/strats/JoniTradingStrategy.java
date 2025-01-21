package at.discord.bot.service.strategy.strats;

import at.discord.bot.model.binance.Order;
import at.discord.bot.model.strategy.StrategyDeploymentContext;
import at.discord.bot.service.binance.order.OrderService;
import at.discord.bot.service.binance.symbol.SymbolPriceMonitorService;
import at.discord.bot.service.candle.BarSeriesHolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoniTradingStrategy implements BaseStrategy {

    private static final Map<String, String> DEFAULT_SETTINGS = Map.of(
            "SYMBOL", "BTCUSDT",
            "RSI_LENGTH", "14",                 // Look-back period for RSI
            "RSI_OVERBOUGHT", "70.0",          // Overbought threshold
            "RSI_OVERSOLD", "30.0",            // Oversold threshold
            "MIN_ORDER_INTERVAL_SECONDS", "30",// Minimum interval between trades
            "TRADE_QUANTITY", "0.001"          // Example trade quantity
    );

    private final BarSeriesHolderService barSeriesHolderService;
    private final SymbolPriceMonitorService symbolPriceMonitorService;
    private final OrderService orderService;

    private static class DeploymentState {
        private Instant lastOrderTime = Instant.EPOCH;
        private boolean inPosition = false;
    }

    private final Map<Long, DeploymentState> stateMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, String> getDefaultSetting() {
        return new ConcurrentHashMap<>(DEFAULT_SETTINGS);
    }

    @Override
    public String getStrategyName() {
        return "RSI_STRATEGY";
    }

    @Override
    public void update(StrategyDeploymentContext deploymentContext) {
        long deploymentId = deploymentContext.getDeploymentId();
        DeploymentState state = stateMap.computeIfAbsent(deploymentId, id -> new DeploymentState());

        // Fetch strategy settings
        Map<String, String> settings = deploymentContext.getSettings();

        String symbol = settings.get("SYMBOL");
        int rsiLength = Integer.parseInt(settings.get("RSI_LENGTH"));
        double rsiOverbought = Double.parseDouble(settings.get("RSI_OVERBOUGHT"));
        double rsiOversold = Double.parseDouble(settings.get("RSI_OVERSOLD"));
        long minOrderIntervalSeconds = Long.parseLong(settings.get("MIN_ORDER_INTERVAL_SECONDS"));
        String quantity = settings.get("TRADE_QUANTITY");

        // Fetch the BarSeries for the given symbol
        BarSeries barSeries = barSeriesHolderService.getBarSeries(symbol);
        if (barSeries == null || barSeries.isEmpty()) {
            symbolPriceMonitorService.registerSymbol(symbol);
            return;
        }

        // Ensure enough bars are available to compute the RSI
        if (barSeries.getBarCount() < rsiLength) {
            return;
        }

        Instant now = Instant.now();
        if (now.minusSeconds(minOrderIntervalSeconds).isBefore(state.lastOrderTime)) {
            return; // Too soon to place another order
        }

        // --- Compute RSI ---
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        RSIIndicator rsi = new RSIIndicator(closePrice, rsiLength);

        int endIndex = barSeries.getEndIndex();
        double rsiValue = rsi.getValue(endIndex).doubleValue();

        // --- Strategy Logic ---
        if (!state.inPosition && rsiValue < rsiOversold) {
            // RSI indicates oversold: Place BUY order
            try {
                Long orderId = orderService.placeMarketOrder(
                        deploymentContext.getDiscordUserId(),
                        "BUY",
                        symbol,
                        quantity,
                        getStrategyName() + "-" + deploymentId
                );
                if (orderId != null) {
                    log.info("Deployment {}: Placed BUY order (id={}) with RSI {}", deploymentId, orderId, rsiValue);
                    state.inPosition = true;
                    state.lastOrderTime = now;
                }
            } catch (Exception e) {
                log.error("Deployment {}: Error placing BUY order", deploymentId, e);
            }
        } else if (state.inPosition && rsiValue > rsiOverbought) {
            // RSI indicates overbought: Place SELL order
            try {
                Long orderId = orderService.placeMarketOrder(
                        deploymentContext.getDiscordUserId(),
                        "SELL",
                        symbol,
                        quantity,
                        getStrategyName() + "-" + deploymentId
                );
                if (orderId != null) {
                    log.info("Deployment {}: Placed SELL order (id={}) with RSI {}", deploymentId, orderId, rsiValue);
                    state.inPosition = false;
                    state.lastOrderTime = now;
                }
            } catch (Exception e) {
                log.error("Deployment {}: Error placing SELL order", deploymentId, e);
            }
        }
    }

    @Override
    public void orderEvent(StrategyDeploymentContext deploymentContext, Order order) {

    }
}
