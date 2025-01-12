package at.discord.bot.persistent;

import at.discord.bot.persistent.model.BinanceCredentialsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BinanceCredentialsRepository extends JpaRepository<BinanceCredentialsEntity, Long> {
    Optional<BinanceCredentialsEntity> findByUserId(Long userId);
}
