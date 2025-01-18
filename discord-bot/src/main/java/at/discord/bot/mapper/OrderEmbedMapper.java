package at.discord.bot.mapper;


import at.discord.bot.model.binance.Order;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class OrderEmbedMapper {

    public MessageEmbed mapOrderToEmbed(Order order) {
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder();

            // Title: Order ID
            embedBuilder.setTitle("Order ID: " + order.getOrderId());
            embedBuilder.setColor(order.getSide().equalsIgnoreCase("BUY") ? Color.GREEN : Color.RED);

            // Description: General Order Info
            embedBuilder.setDescription(String.format("Symbol: `%s`\nType: `%s`\nStatus: `%s`",
                order.getSymbol(), order.getType(), order.getStatus()));

            // Only display executed quantity if it's relevant (not fully executed immediately)
            if (order.getExecutedQty() != null && order.getExecutedQty().compareTo(order.getOrigQty()) < 0) {
                embedBuilder.addField("Executed Quantity", order.getExecutedQty().toPlainString(), true);
            }

            // Only show the average price if it exists and is relevant
            if (order.getAvgPrice() != null && order.getAvgPrice().compareTo(BigDecimal.ZERO) > 0) {
                embedBuilder.addField("Average Price", order.getAvgPrice().toPlainString(), true);
            }

            // Price is relevant for:
            // - Limit orders
            // - Market orders that couldn't be filled immediately
            if (("LIMIT".equalsIgnoreCase(order.getType()) || "MARKET".equalsIgnoreCase(order.getType()))
                && order.getPrice() != null) {
                embedBuilder.addField("Price", order.getPrice().toPlainString(), true);
            }

            // Stop price is relevant for stop or trailing stop orders
            if (order.getStopPrice() != null && order.getStopPrice().compareTo(BigDecimal.ZERO) > 0) {
                embedBuilder.addField("Stop Price", order.getStopPrice().toPlainString(), true);
            }

            // Callback rate is relevant for trailing stop orders
            if ("TRAILING_STOP_MARKET".equalsIgnoreCase(order.getType()) && order.getPriceRate() != null) {
                embedBuilder.addField("Callback Rate", order.getPriceRate().toPlainString(), true);
            }

            // Add additional fields where relevant
            embedBuilder.addField("Side", order.getSide(), true);
            embedBuilder.addField("Position Side", order.getPositionSide(), true);
            embedBuilder.addField("Original Quantity", order.getOrigQty().toPlainString(), true);
            embedBuilder.addField("Bot-Managed", Boolean.toString(isBotManaged(order)), true);

            // Include timestamp for the order creation time
            if (order.getTime() != null) {
                String formattedTime = Instant.ofEpochMilli(order.getTime())
                    .atZone(ZoneId.of("Europe/Vienna"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"));
                embedBuilder.setFooter(formattedTime);
            }

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