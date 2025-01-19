
package at.discord.bot.persistent;

import at.discord.bot.persistent.model.GlobalSettingsEntity;
import at.discord.bot.persistent.model.StrategyDeploymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StrategyDeploymentRepository extends JpaRepository<StrategyDeploymentEntity, Long> {
    Optional<StrategyDeploymentEntity> findByDeploymentId(Long setting);
    List<StrategyDeploymentEntity> findAllByDiscordUserId(Long setting);
}
