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
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlexTradingStrategy implements BaseStrategy {

    private static final Map<String, String> DEFAULT_SETTINGS = Map.of(
            "SYMBOL", "PEPEFDUSD",
            "SHORT_SMA_LENGTH", "50",
            "LONG_SMA_LENGTH", "150",
            "MIN_ORDER_INTERVAL_SECONDS", "30",
            "TRADE_QUANTITY", "156524"  // Example default quantity
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
        return "SIMPLE_SMA";
    }

    @Override
    public void update(StrategyDeploymentContext deploymentContext) {
        long deploymentId = deploymentContext.getDeploymentId();

        // Obtain or create a DeploymentState for this particular deployment
        DeploymentState state = stateMap.computeIfAbsent(deploymentId, id -> new DeploymentState());

        // Fetch the strategy settings from the deployment context
        Map<String, String> settings = deploymentContext.getSettings();

        // Parse each setting
        String symbol = settings.get("SYMBOL");
        int shortSmaLength = Integer.parseInt(settings.get("SHORT_SMA_LENGTH"));
        int longSmaLength = Integer.parseInt(settings.get("LONG_SMA_LENGTH"));
        long minOrderIntervalSeconds = Long.parseLong(settings.get("MIN_ORDER_INTERVAL_SECONDS"));
        String quantity = settings.get("TRADE_QUANTITY"); // e.g., "156524"

        // Get the BarSeries for the symbol
        BarSeries barSeries = barSeriesHolderService.getBarSeries(symbol);
        if (barSeries == null || barSeries.isEmpty()) {
            // If no data, ensure we monitor the symbol; no further logic
            symbolPriceMonitorService.registerSymbol(symbol);
            return;
        }

        // Need enough bars to compute the long SMA
        if (barSeries.getBarCount() < longSmaLength) {
            return;
        }

        // Check minimal gap between orders
        Instant now = Instant.now();
        if (now.minusSeconds(minOrderIntervalSeconds).isBefore(state.lastOrderTime)) {
            // Too soon since last order - skip
            return;
        }

        // --- Compute Indicators ---
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator shortSma = new SMAIndicator(closePrice, shortSmaLength);
        SMAIndicator longSma = new SMAIndicator(closePrice, longSmaLength);

        int endIndex = barSeries.getEndIndex();
        double shortValue = shortSma.getValue(endIndex).doubleValue();
        double longValue = longSma.getValue(endIndex).doubleValue();

        // --- Simple SMA crossing logic ---
        // If shortSMA > longSMA => bullish => Buy if not in position
        if (!state.inPosition && shortValue > longValue) {
            Long orderId = orderService.placeMarketOrder(
                    deploymentContext.getDiscordUserId(),
                    "BUY",
                    symbol,
                    quantity,
                    getStrategyName() + "-" + deploymentId
            );
            if (orderId != null) {
                log.info("Deployment {}: Placed BUY order (id={})", deploymentId, orderId);
                state.inPosition = true;
                state.lastOrderTime = now;
            }
        }
        // If shortSMA < longSMA => bearish => Sell if in position
        else if (state.inPosition && shortValue < longValue) {
            Long orderId = orderService.placeMarketOrder(
                    deploymentContext.getDiscordUserId(),
                    "SELL",
                    symbol,
                    quantity,
                    getStrategyName() + "-" + deploymentId
            );
            if (orderId != null) {
                log.info("Deployment {}: Placed SELL order (id={})", deploymentId, orderId);
                state.inPosition = false;
                state.lastOrderTime = now;
            }
        }
    }

    @Override
    public void orderEvent(StrategyDeploymentContext deploymentContext, Order order) {

    }
}
