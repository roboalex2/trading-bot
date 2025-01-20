package at.discord.bot.service.strategy;

import at.discord.bot.model.binance.BinanceContext;
import at.discord.bot.persistent.StrategyDeploymentRepository;
import at.discord.bot.persistent.model.StrategyDeploymentEntity;
import at.discord.bot.service.binance.credential.BinanceContextProviderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyService {

    private final List<BaseStrategy> baseStrategyList;
    private final BinanceContextProviderService binanceContextProviderService;
    private final ObjectMapper objectMapper;
    private final StrategyDeploymentRepository strategyDeploymentRepository;
    private final StrategyDeploymentProcessorService strategyDeploymentProcessorService;

    public List<String> getAvailableStrategyNames() {
        return baseStrategyList.stream()
                .map(BaseStrategy::getStrategyName)
                .toList();
    }

    public BaseStrategy getStrategy(String strategyName) {
        return baseStrategyList.stream()
                .filter(s -> s.getStrategyName().equals(strategyName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Provided Strategy not found: " + strategyName));
    }

    public Long deployStrategy(Long userId, String strategyName) {
        BaseStrategy baseStrategy = getStrategy(strategyName);
        BinanceContext userContext = binanceContextProviderService.getUserContext(userId);
        if (userContext == null) {
            throw new IllegalArgumentException("No Binance Credentials present for userId=`" + userId + "`. Please add them.");
        }

        Map<String, String> defaultSetting = baseStrategy.getDefaultSetting();

        try {
            StrategyDeploymentEntity stratDeploy = new StrategyDeploymentEntity();
            stratDeploy.setStrategyName(strategyName);
            stratDeploy.setDeploymentSettings(objectMapper.writeValueAsString(defaultSetting));
            stratDeploy.setDiscordUserId(userId);
            StrategyDeploymentEntity saveDeployment = strategyDeploymentRepository.save(stratDeploy);

            strategyDeploymentProcessorService.makeActiveDeployment(saveDeployment);

            return saveDeployment.getDeploymentId();
        } catch (Exception e) {
            log.warn("Error while deploying strategy: {}", strategyName, e);
            throw new RuntimeException("Error while deploying strategy=`" + strategyName + "`, userId=`" + userId + "` Details: " + e.getMessage());
        }
    }

    public void undeployStrategy(Long deploymentId, Long userId) {
        StrategyDeploymentEntity byDeploymentId = strategyDeploymentRepository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("No strategy with id=" + deploymentId + " found."));

        if (!Objects.equals(byDeploymentId.getDiscordUserId(), userId)) {
            throw new IllegalArgumentException("No strategy with id=" + deploymentId + " found.");
        }

        strategyDeploymentRepository.delete(byDeploymentId);
        strategyDeploymentProcessorService.removeActiveDeployment(deploymentId);
    }

    public List<StrategyDeploymentEntity> listStrategyDeployments(Long userId) {
        return strategyDeploymentRepository.findAllByDiscordUserId(userId);
    }
}
