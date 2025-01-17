
package at.discord.bot.persistent;

import at.discord.bot.persistent.model.GlobalSettingsEntity;
import at.discord.bot.persistent.model.PriceAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GlobalSettingsRepository extends JpaRepository<GlobalSettingsEntity, String> {
    List<GlobalSettingsEntity> findBySetting(String setting);
}
