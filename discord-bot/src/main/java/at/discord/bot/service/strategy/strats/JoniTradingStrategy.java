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
            "SYMBOL", "BTCUSDT"
    );

    private static final int RSI_LENGTH = 14; // RSI look-back period
    private static final double RSI_OVERBOUGHT = 70.0;
    private static final double RSI_OVERSOLD = 30.0;
    private static final long MIN_ORDER_INTERVAL_SECONDS = 30; // Minimum time gap between orders

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
        return "RSI_STRATEGY";
    }

    @Override
    public void update(StrategyDeploymentContext deploymentContext) {
        long deploymentId = deploymentContext.getDeploymentId();
        DeploymentState state = stateMap.computeIfAbsent(deploymentId, id -> new DeploymentState());

        String symbol = deploymentContext.getSettings().get("SYMBOL");
        BarSeries barSeries = barSeriesHolderService.getBarSeries(symbol);
        if (barSeries == null || barSeries.isEmpty()) {
            symbolPriceMonitorService.registerSymbol(symbol);
            return;
        }

        if (barSeries.getBarCount() < RSI_LENGTH) {
            return; // Not enough data to calculate RSI
        }

        Instant now = Instant.now();
        if (now.minusSeconds(MIN_ORDER_INTERVAL_SECONDS).isBefore(state.lastOrderTime)) {
            return; // Too soon to place another order
        }

        // Compute RSI
        ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_LENGTH);

        int endIndex = barSeries.getEndIndex();
        double rsiValue = rsi.getValue(endIndex).doubleValue();

        // Strategy logic: Buy/Sell based on RSI thresholds
        if (!state.inPosition && rsiValue < RSI_OVERSOLD) {
            // Oversold: Place a BUY order
            String quantity = "0.001"; // Example quantity
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
        } else if (state.inPosition && rsiValue > RSI_OVERBOUGHT) {
            // Overbought: Place a SELL order
            String quantity = "0.001"; // Example quantity
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
