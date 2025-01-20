package at.discord.bot.mapper;


import at.discord.bot.model.binance.Order;
import at.discord.bot.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEmbedMapper {

    private final UserService userService;

    public MessageEmbed mapOrderToEmbed(Order order) {
        return mapOrderToEmbed(order, false);
    }

    public MessageEmbed mapOrderToEmbed(Order order, boolean includeUser) {
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder();

            // Title: Order ID
            if (order.getOrderId() != null) {
                embedBuilder.setTitle("Order ID: " + order.getOrderId());
            }
            embedBuilder.setColor(order.getSide() != null && order.getSide().equalsIgnoreCase("BUY") ? Color.GREEN : Color.RED);

            // User Information: Username and Profile Image
            if (includeUser && order.getDiscordUserId() != null) {
                User user = userService.getUser(order.getDiscordUserId());
                if (user != null) {
                    String username = user.getName();
                    String avatarUrl = user.getEffectiveAvatarUrl();
                    embedBuilder.setAuthor(username, null, avatarUrl);
                }
            }

            // Description: General Order Info
            StringBuilder description = new StringBuilder();
            if (order.getSymbol() != null) {
                description.append("Symbol: `").append(order.getSymbol()).append("`\n");
            }
            if (order.getType() != null) {
                description.append("Type: `").append(order.getType()).append("`\n");
            }
            if (order.getStatus() != null) {
                description.append("Status: `").append(order.getStatus()).append("`");
            }
            if (description.length() > 0) {
                embedBuilder.setDescription(description.toString());
            }

            // Executed Quantity
            if (order.getExecutedQty() != null && order.getOrigQty() != null &&
                order.getExecutedQty().compareTo(order.getOrigQty()) < 0) {
                embedBuilder.addField("Executed Quantity", order.getExecutedQty().toPlainString(), true);
            }

            // Average Price
            if (order.getAvgPrice() != null && order.getAvgPrice().compareTo(BigDecimal.ZERO) > 0) {
                embedBuilder.addField("Average Price", order.getAvgPrice().toPlainString(), true);
            }

            // Price
            if (order.getPrice() != null &&
                ("LIMIT".equalsIgnoreCase(order.getType()) || "MARKET".equalsIgnoreCase(order.getType()))) {
                embedBuilder.addField("Price", order.getPrice().toPlainString(), true);
            }

            // Stop Price
            if (order.getStopPrice() != null && order.getStopPrice().compareTo(BigDecimal.ZERO) > 0) {
                embedBuilder.addField("Stop Price", order.getStopPrice().toPlainString(), true);
            }

            // Callback Rate
            if ("TRAILING_STOP_MARKET".equalsIgnoreCase(order.getType()) && order.getPriceRate() != null) {
                embedBuilder.addField("Callback Rate", order.getPriceRate().toPlainString(), true);
            }

            // Side
            if (order.getSide() != null) {
                embedBuilder.addField("Side", order.getSide(), true);
            }

            // Position Side
            if (order.getPositionSide() != null) {
                embedBuilder.addField("Position Side", order.getPositionSide(), true);
            }

            // Original Quantity
            if (order.getOrigQty() != null) {
                embedBuilder.addField("Original Quantity", order.getOrigQty().toPlainString(), true);
            }

            // Transaction Fees
            if (order.getCommissionAmount() != null && order.getCommissionAsset() != null) {
                embedBuilder.addField("Transaction Fees",
                    order.getCommissionAmount().toPlainString() + " " + order.getCommissionAsset(),
                    true);
            }

            // Bot-Managed
            embedBuilder.addField("Bot-Managed", Boolean.toString(isBotManaged(order)), false);

            if (isBotManaged(order)) {
                embedBuilder.addField("Creation Reason", order.getSource(), false);
            }

            // Timestamp (Order Creation Time)
            if (order.getTime() != null) {
                String formattedTime = Instant.ofEpochMilli(order.getTime())
                    .atZone(ZoneId.of("Europe/Vienna"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"));
                embedBuilder.addField("Order Creation Time", formattedTime, false);
            }

            embedBuilder.setFooter(Instant.now()
                .atZone(ZoneId.of("Europe/Vienna"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")));

            return embedBuilder.build();
        } catch (Exception exception) {
            log.warn("Failed to map order to embed. Order: {}", order, exception);
            return new EmbedBuilder()
                .setTitle("Error")
                .setDescription("Failed to generate embed for the order. Please contact support.")
                .setColor(Color.RED)
                .build();
        }
    }

    private boolean isBotManaged(Order order) {
        if (order.getSource() == null || "UNKNOWN".equals(order.getSource())) {
            return false;
        } else {
            return true;
        }
    }
}
