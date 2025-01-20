package at.discord.bot.model.strategy;

import at.discord.bot.model.binance.BinanceContext;
import lombok.*;

import java.util.Map;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
public class StrategyDeploymentContext {
    private long deploymentId;
    private long discordUserId;
    private String strategyName;
    private BinanceContext binanceContext;
    private Map<String, String> settings;
}
