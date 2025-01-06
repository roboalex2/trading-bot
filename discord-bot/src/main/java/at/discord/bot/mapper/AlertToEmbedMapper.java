package at.discord.bot.mapper;

import at.discord.bot.persistent.model.PriceAlertEntity;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Component;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class AlertToEmbedMapper {
    public MessageEmbed mapToEmbed(Num currentPrice, PriceAlertEntity alertEntity) {
        try {
            String direction = (currentPrice.isGreaterThan(DecimalNum.valueOf(alertEntity.getLastCheckedPrice())) ? "above" : "below");
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(alertEntity.getSymbol().toUpperCase() + " Crossed " + direction.toUpperCase() + " " + alertEntity.getPrice());
            eb.setColor(new Color(123, 31, 162));
            eb.setDescription("The symbol `" + alertEntity.getSymbol() + "` crossed " + direction + " `" + alertEntity.getPrice() + "`" +
                    "\n Current Price: `" + currentPrice + "`");
            eb.setFooter(Instant.now().atZone(ZoneId.of("Europe/Vienna")).toOffsetDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")));
            return eb.build();
        } catch (Exception exception) {
            log.warn("Failed to build Embed for DC message for alert: {}", alertEntity);
            log.warn("Detail exception: ", exception);
            return new EmbedBuilder()
                    .setTitle("There was an Exception")
                    .setDescription("Failed to build embed for alert:\n '" + alertEntity + "' \nException: " + exception.getMessage())
                    .build();
        }
    }
}
