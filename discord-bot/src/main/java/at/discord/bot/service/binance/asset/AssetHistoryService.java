package at.discord.bot.service.binance.asset;

import at.discord.bot.service.binance.credential.CredentialsDataAccessService;
import at.discord.bot.model.asset.UserAsset;
import at.discord.bot.persistent.AssetHistoryRepository;
import at.discord.bot.persistent.model.AssetHistoryEntity;
import at.discord.bot.model.asset.AssetHistoryDTO;  // New model for return type
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetHistoryService {

    private final CredentialsDataAccessService credentialsDataAccessService;
    private final UserAssetInfoProviderService userAssetInfoProviderService;
    private final AssetHistoryRepository assetHistoryRepository;

    /**
     * Scheduled job that runs every hour to fetch user assets and store them in the database
     */
    @Scheduled(cron = "0 0 * * * ?")  // Every hour
    public void fetchAndStoreUserAssetsHistory() {
        List<Long> userIds = credentialsDataAccessService.getAllUserIds();

        // Fetch and save asset history for each user
        userIds.forEach(userId -> {
            List<UserAsset> userAssets = userAssetInfoProviderService.getUserAssets(userId);
            saveUserAssetHistory(userId, userAssets);
        });
    }

    /**
     * Method to save user asset history to the database
     *
     * @param userId     The Discord user ID of the command issuer.
     * @param userAssets The list of assets fetched from Binance for the user.
     */


    private void saveUserAssetHistory(Long userId, List<UserAsset> userAssets) {
        OffsetDateTime currentTime = OffsetDateTime.now(); // Capture current timestamp

        // Persist each asset to the database
        userAssets.forEach(asset -> {
            // Sum up the different types of balance (free, locked, frozen, withdrawing)
            BigDecimal totalBalance = BigDecimal.ZERO;

            if (asset.getFree() != null) {
                totalBalance = totalBalance.add(asset.getFree());
            }
            if (asset.getLocked() != null) {
                totalBalance = totalBalance.add(asset.getLocked());
            }
            if (asset.getFrozen() != null) {
                totalBalance = totalBalance.add(asset.getFrozen());
            }
            if (asset.getWithdrawing() != null) {
                totalBalance = totalBalance.add(asset.getWithdrawing());
            }

            // Create new AssetHistoryEntity for each asset
            AssetHistoryEntity entity = new AssetHistoryEntity();
            entity.setUserId(userId);
            entity.setAssetName(asset.getAsset()); // Get the asset name
            entity.setAssetBalance(totalBalance);  // Store the total balance
            entity.setCaptureTimestamp(currentTime); // Capture the current timestamp

            // Save to the database
            assetHistoryRepository.save(entity);
        });
    }

    public List<AssetHistoryDTO> getAssetHistory(Long userId, String assetName) {
        // This method should fetch asset history for the given user and asset name from the database
        List<AssetHistoryEntity> historyEntities = assetHistoryRepository.findByUserIdAndAssetName(userId, assetName);

        // Convert entities to DTOs using a mapper (e.g., MapStruct or manual mapping)
        return historyEntities.stream()
                .map(entity -> new AssetHistoryDTO(
                        entity.getCaptureTimestamp(),
                        entity.getAssetName(),
                        entity.getAssetBalance()))
                .collect(Collectors.toList());
    }

}
