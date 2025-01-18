package at.discord.bot.persistent;

import at.discord.bot.persistent.model.OrderEntity;
import at.discord.bot.persistent.model.PriceAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    // Find a single order by its orderId
    Optional<OrderEntity> findByOrderId(Long orderId);

    // Find all orders for a given orderId
    List<OrderEntity> findAllByOrderIdIn(List<Long> orderId);

    // Find all orders by symbol
    List<OrderEntity> findAllBySymbol(String symbol);

    // Find all orders for a specific discordUserId
    List<OrderEntity> findAllByDiscordUserId(Long discordUserId);

    // Find all orders by source
    List<OrderEntity> findAllBySource(String source);
}
