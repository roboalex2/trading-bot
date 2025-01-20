package at.discord.bot.service.strategy;

import at.discord.bot.persistent.StrategyDeploymentRepository;
import at.discord.bot.persistent.model.StrategyDeploymentEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategySettingService {

    private final StrategyService strategyService;
    private final StrategyDeploymentRepository strategyDeploymentRepository;
    private final ActiveStrategyDeploymentService activeStrategyDeploymentService;
    private final ObjectMapper objectMapper;

    public void updateDeploymentSetting(Long userId, Long deploymentId, String settingKey, String settingValue) {
        StrategyDeploymentEntity deploymentEntity = strategyDeploymentRepository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException("No deployment found for ID: " + deploymentId));

        if (!deploymentEntity.getDiscordUserId().equals(userId)) {
            throw new IllegalArgumentException("No deployment found for ID: " + deploymentId);
        }

        String strategyName = deploymentEntity.getStrategyName();
        Map<String, String> defaultSettings;
        try {
            defaultSettings = strategyService.getStrategy(strategyName).getDefaultSetting();
        } catch (Exception e) {
            log.warn("Failed to retrieve default settings for strategy: {}", strategyName, e);
            throw new RuntimeException("Failed to retrieve default settings for strategy: " + strategyName);
        }

        // Validate that the setting key exists in the default settings
        if (!defaultSettings.containsKey(settingKey)) {
            throw new IllegalArgumentException("Invalid setting key: `" + settingKey + "`. Available settings: " + defaultSettings.keySet());
        }

        try {
            Map<String, String> settingsMap = objectMapper.readValue(
                    deploymentEntity.getDeploymentSettings(),
                    new TypeReference<ConcurrentHashMap<String, String>>() {});

            settingsMap.put(settingKey, settingValue);
            deploymentEntity.setDeploymentSettings(objectMapper.writeValueAsString(settingsMap));
            strategyDeploymentRepository.save(deploymentEntity);

            // If this deployment is active, update the in-memory context as well
            if (Boolean.TRUE.equals(deploymentEntity.getActive())) {
                activeStrategyDeploymentService.updateActiveDeploymentSetting(deploymentId, settingKey, settingValue);
            }

            log.info("Updated setting in deploymentId={}, key={}, value={}", deploymentId, settingKey, settingValue);
        } catch (Exception e) {
            log.warn("Failed to update setting for deploymentId={}, key={}, value={}", deploymentId, settingKey, settingValue, e);
            throw new RuntimeException("Failed to update setting: " + e.getMessage(), e);
        }
    }
}