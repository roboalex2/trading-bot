package at.discord.bot.persistent;

import at.discord.bot.persistent.model.AssetHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssetHistoryRepository extends JpaRepository<AssetHistoryEntity, UUID> {
    /**
     * Find all asset history entries for a specific user and asset name, sorted by capture timestamp.
     *
     * @param userId The Discord user ID.
     * @param assetName     The name of the asset.
     * @return List of AssetHistoryEntity objects sorted by capture timestamp.
     */
    List<AssetHistoryEntity> findByUserIdAndAssetName(Long userId, String assetName);}
