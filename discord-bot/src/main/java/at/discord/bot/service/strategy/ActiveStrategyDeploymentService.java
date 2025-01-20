package at.discord.bot.service.strategy;

import at.discord.bot.model.binance.Order;
import at.discord.bot.model.strategy.StrategyDeploymentContext;
import at.discord.bot.persistent.StrategyDeploymentRepository;
import at.discord.bot.persistent.model.StrategyDeploymentEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveStrategyDeploymentService {
    private final List<BaseStrategy> baseStrategyList;
    private final ObjectMapper objectMapper;
    private final StrategyDeploymentRepository strategyDeploymentRepository;

    private Map<Long, StrategyDeploymentContext> strategyDeploymentContexts = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    private void onApplicationReadyEvent() {
        strategyDeploymentRepository.findAll()
                .stream().filter(StrategyDeploymentEntity::getActive)
                .forEach(this::makeActiveDeployment);
    }

    public void makeActiveDeployment(StrategyDeploymentEntity strategyDeploymentEntity) {
        try {
            if (!strategyDeploymentEntity.getActive()) {
                strategyDeploymentEntity.setActive(true);
                strategyDeploymentRepository.save(strategyDeploymentEntity); // I do not trust entity management
            }
            strategyDeploymentContexts.put(strategyDeploymentEntity.getDeploymentId(),
                    StrategyDeploymentContext.builder()
                            .deploymentId(strategyDeploymentEntity.getDeploymentId())
                            .strategyName(strategyDeploymentEntity.getStrategyName())
                            .settings(objectMapper.readValue(strategyDeploymentEntity.getDeploymentSettings(), new TypeReference<ConcurrentHashMap<String, String>>() {}))
                            .discordUserId(strategyDeploymentEntity.getDiscordUserId())
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to add strategy deployment entity", e);
            throw new RuntimeException(e);
        }
    }

    public void makeInactiveDeployment(long deploymentId) {
        strategyDeploymentRepository.findByDeploymentId(deploymentId)
                .ifPresent(deploy -> {
                    deploy.setActive(false);
                    strategyDeploymentRepository.save(deploy); // I do not trust entity management
                });
        strategyDeploymentContexts.remove(deploymentId);
    }

    public void updateActiveDeploymentSetting(long deploymentId, String settingKey, String settingValue) {
        StrategyDeploymentContext context = strategyDeploymentContexts.get(deploymentId);
        if (context != null) {
            context.getSettings().put(settingKey, settingValue);
            log.info("Updated in-memory setting for deploymentId={}, key={}, value={}", deploymentId, settingKey, settingValue);
        }
    }

    public List<StrategyDeploymentContext> getActiveDeploymentsForUser(long discordUserId) {
        return strategyDeploymentContexts.values().stream()
                .filter(context -> context.getDiscordUserId() == discordUserId)
                .toList();
    }

    public synchronized void handleOrderEvent(Order order) {
        strategyDeploymentContexts.values().stream()
                .filter(el -> el.getDiscordUserId() == order.getDiscordUserId())
                .parallel()
                .forEach(strategyDeploymentContext -> {
                    try {
                        BaseStrategy strategy = baseStrategyList.stream()
                                .filter(s -> s.getStrategyName().equals(strategyDeploymentContext.getStrategyName()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Provided Strategy not found: " + strategyDeploymentContext.getStrategyName()));
                        StrategyDeploymentContext fullContext = strategyDeploymentContext.toBuilder()
                                .build();

                        strategy.orderEvent(fullContext, order);
                    } catch (Exception exception) {
                        log.error("Failed to order event strategy deployment entity={}", strategyDeploymentContext, exception);
                        // TODO Log in discord?
                    }
                });
    }

    @Scheduled(cron = "*/2 * * * * *")
    private synchronized void processDeployments() {
        strategyDeploymentContexts.values().stream()
                .parallel()
                .forEach(strategyDeploymentContext -> {
                    try {
                        BaseStrategy strategy = baseStrategyList.stream()
                                .filter(s -> s.getStrategyName().equals(strategyDeploymentContext.getStrategyName()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException("Provided Strategy not found: " + strategyDeploymentContext.getStrategyName()));
                        StrategyDeploymentContext fullContext = strategyDeploymentContext.toBuilder()
                                .build();

                        strategy.update(fullContext);
                    } catch (Exception exception) {
                        log.error("Failed to update strategy deployment entity={}", strategyDeploymentContext, exception);
                        // TODO Log in discord?
                    }
                });
    }
}
