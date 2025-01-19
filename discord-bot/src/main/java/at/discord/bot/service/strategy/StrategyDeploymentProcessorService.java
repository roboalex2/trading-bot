package at.discord.bot.service.strategy;

import at.discord.bot.model.binance.BinanceContext;
import at.discord.bot.model.binance.Order;
import at.discord.bot.model.strategy.StrategyDeploymentContext;
import at.discord.bot.persistent.StrategyDeploymentRepository;
import at.discord.bot.persistent.model.StrategyDeploymentEntity;
import at.discord.bot.service.binance.credential.BinanceContextProviderService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyDeploymentProcessorService {
    private final List<BaseStrategy> baseStrategyList;
    private final BinanceContextProviderService binanceContextProviderService;
    private final ObjectMapper objectMapper;
    private final StrategyDeploymentRepository strategyDeploymentRepository;

    private Map<Long, StrategyDeploymentContext> strategyDeploymentContexts = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    private void onApplicationReadyEvent() {
        strategyDeploymentRepository.findAll()
                // TODO only resume active deployments not paused ones
                .forEach(this::makeActiveDeployment);
    }


    public void makeActiveDeployment(StrategyDeploymentEntity strategyDeploymentEntity) {
        try {
            strategyDeploymentContexts.put(strategyDeploymentEntity.getDeploymentId(),
                    StrategyDeploymentContext.builder()
                            .deploymentId(strategyDeploymentEntity.getDeploymentId())
                            .strategyName(strategyDeploymentEntity.getStrategyName())
                            .settings(objectMapper.readValue(strategyDeploymentEntity.getDeploymentSettings(), new TypeReference<Map<String, String>>() {}))
                            .discordUserId(strategyDeploymentEntity.getDiscordUserId())
                            .binanceContext(null) // Loaded on the fly for security
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to add strategy deployment entity", e);
            throw new RuntimeException(e);
        }
    }

    public void removeActiveDeployment(long deploymentId) {
        strategyDeploymentContexts.remove(deploymentId);
    }

    @Async
    public void handleOrderEvent(Order order) {
        strategyDeploymentContexts.values().stream()
                .filter(el -> el.getDiscordUserId() == order.getDiscordUserId())
                .forEach(strategyDeploymentContext -> {
                    try {
                        BaseStrategy strategy = baseStrategyList.stream()
                                .filter(s -> s.getStrategyName().equals(strategyDeploymentContext.getStrategyName()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Provided Strategy not found: " + strategyDeploymentContext.getStrategyName()));
                        BinanceContext userContext = binanceContextProviderService.getUserContext(strategyDeploymentContext.getDiscordUserId());
                        StrategyDeploymentContext fullContext = strategyDeploymentContext.toBuilder()
                                .binanceContext(userContext)
                                .build();

                        strategy.orderEvent(fullContext, order);
                    } catch (Exception exception) {
                        log.error("Failed to order event strategy deployment entity={}", strategyDeploymentContext, exception);
                        // TODO Log in discord?
                    }
                });
    }

    @Scheduled(cron = "*/2 * * * * *")
    private void processDeployments() {
        strategyDeploymentContexts.values().forEach(strategyDeploymentContext -> {
            try {
                BaseStrategy strategy = baseStrategyList.stream()
                        .filter(s -> s.getStrategyName().equals(strategyDeploymentContext.getStrategyName()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Provided Strategy not found: " + strategyDeploymentContext.getStrategyName()));
                BinanceContext userContext = binanceContextProviderService.getUserContext(strategyDeploymentContext.getDiscordUserId());
                StrategyDeploymentContext fullContext = strategyDeploymentContext.toBuilder()
                        .binanceContext(userContext)
                        .build();

                strategy.update(fullContext);
            } catch (Exception exception) {
                log.error("Failed to update strategy deployment entity={}", strategyDeploymentContext, exception);
                // TODO Log in discord?
            }
        });
    }
}
