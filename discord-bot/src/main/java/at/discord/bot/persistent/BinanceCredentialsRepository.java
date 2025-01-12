package at.discord.bot.persistent;

import at.discord.bot.persistent.model.BinanceCredentialsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinanceCredentialsRepository extends JpaRepository<BinanceCredentialsEntity, UUID> {

    // Find credentials by label
    Optional<BinanceCredentialsEntity> findByLabel(String label);

    // List all credentials
    List<BinanceCredentialsEntity> findAll();
}
