package at.discord.bot.service.strategy.strats;

import at.discord.bot.model.binance.Order;
import at.discord.bot.model.strategy.StrategyDeploymentContext;

import java.util.Map;

public interface BaseStrategy {
    Map<String, String> getDefaultSetting();
    String getStrategyName();

    // Update is called every second
    void update(StrategyDeploymentContext deploymentContext);

    // Called upon binance order event
    void orderEvent(StrategyDeploymentContext deploymentContext, Order order);
}
