package at.discord.bot.service.strategy;

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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlexTradingStrategy implements BaseStrategy {

    private static final Map<String, String> DEFAULT_SETTINGS = Map.of(
            "SYMBOL", "PEPEFDUSD"
    );

    // TA4J configuration
    private static final int SHORT_SMA_LENGTH = 5;
    private static final int LONG_SMA_LENGTH = 15;

    // Minimal gap between trades, in seconds (to prevent spam)
    private static final long MIN_ORDER_INTERVAL_SECONDS = 30;

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
        return DEFAULT_SETTINGS;
    }

    @Override
    public String getStrategyName() {
        return "SIMPLE_SMA";
    }

    @Override
    public void update(StrategyDeploymentContext deploymentContext) {
        long deploymentId = deploymentContext.getDeploymentId();
        // Fetch or create state object for this deployment
        DeploymentState state = stateMap.computeIfAbsent(deploymentId, id -> new DeploymentState());

        String symbol = deploymentContext.getSettings().get("SYMBOL");
        // Get the BarSeries for the symbol
        BarSeries barSeries = barSeriesHolderService.getBarSeries(symbol);
        if (barSeries == null || barSeries.isEmpty()) {
            // If no data, register the symbol and exit
            symbolPriceMonitorService.registerSymbol(symbol);
            return;
        }

        // Need enough bars to compute SMA
        if (barSeries.getBarCount() < LONG_SMA_LENGTH) {
            return;
        }

        // Check minimal gap between orders
        Instant now = Instant.now();
        if (now.minusSeconds(MIN_ORDER_INTERVAL_SECONDS).isBefore(state.lastOrderTime)) {
            // Too soon since last order - skip
            return;
        }

        // --- Compute Indicators ---
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        SMAIndicator shortSma = new SMAIndicator(closePrice, SHORT_SMA_LENGTH);
        SMAIndicator longSma = new SMAIndicator(closePrice, LONG_SMA_LENGTH);

        int endIndex = barSeries.getEndIndex();
        double shortValue = shortSma.getValue(endIndex).doubleValue();
        double longValue = longSma.getValue(endIndex).doubleValue();

        // Simple SMA crossing logic
        // If shortSMA > longSMA => bullish => Buy if not in position
        if (!state.inPosition && shortValue > longValue) {
            String quantity = "10000"; // Example
            try {
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
            } catch (Exception e) {
                log.error("Deployment {}: Error placing BUY order", deploymentId, e);
            }
        }
        // If shortSMA < longSMA => bearish => Sell if in position
        else if (state.inPosition && shortValue < longValue) {
            String quantity = "10000";
            try {
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
            } catch (Exception e) {
                log.error("Deployment {}: Error placing SELL order", deploymentId, e);
            }
        }
    }

    @Override
    public void orderEvent(StrategyDeploymentContext deploymentContext, Order order) {

    }
}
