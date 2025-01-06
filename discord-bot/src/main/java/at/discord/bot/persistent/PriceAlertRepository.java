package at.discord.bot.persistent;

import at.discord.bot.persistent.model.PriceAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PriceAlertRepository extends JpaRepository<PriceAlertEntity, UUID> {
    List<PriceAlertEntity> findBySymbolAndPrice(String symbol, String price);
}
