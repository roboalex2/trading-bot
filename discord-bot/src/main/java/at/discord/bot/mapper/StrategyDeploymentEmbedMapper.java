package at.discord.bot.mapper;

import at.discord.bot.model.strategy.StrategyDeploymentContext;
import at.discord.bot.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StrategyDeploymentEmbedMapper {

    private final UserService userService;


    public MessageEmbed mapDeploymentToEmbed(StrategyDeploymentContext context, String errorMessage) {
        try {
            EmbedBuilder embedBuilder = new EmbedBuilder();

            // Title: Deployment ID
            embedBuilder.setTitle("Error for Deployment ID: " + context.getDeploymentId());
            embedBuilder.setColor(Color.MAGENTA);

            User user = userService.getUser(context.getDiscordUserId());
            if (user != null) {
                try {
                    String username = user.getName();
                    String avatarUrl = user.getEffectiveAvatarUrl();
                    embedBuilder.setAuthor(username, null, avatarUrl);
                } catch (Exception exception) {
                    embedBuilder.setAuthor(context.getDiscordUserId() + "", null, null);
                }
            } else {
                embedBuilder.setAuthor("Unknown User");
            }

            // Add fields for StrategyDeploymentContext properties
            embedBuilder.addField("Strategy Name", context.getStrategyName(), false);
            embedBuilder.addField("Discord User ID", String.valueOf(context.getDiscordUserId()), false);

            // Add settings (formatted as key-value pairs)
            if (context.getSettings() != null && !context.getSettings().isEmpty()) {
                StringBuilder settingsBuilder = new StringBuilder();
                for (Map.Entry<String, String> entry : context.getSettings().entrySet()) {
                    settingsBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                embedBuilder.addField("Settings", settingsBuilder.toString(), false);
            }

            // Error Message (if provided)
            if (errorMessage != null && !errorMessage.isBlank()) {
                embedBuilder.addField("Error Message", errorMessage, false);
                embedBuilder.setColor(Color.RED); // Change color to indicate error
            }

            // Footer: Timestamp
            embedBuilder.setFooter("Generated on",
                    Instant.now().atZone(ZoneId.of("Europe/Vienna"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")));

            return embedBuilder.build();
        } catch (Exception exception) {
            log.warn("Failed to map deployment to embed. Context: {}", context, exception);
            return new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("Failed to generate embed for the deployment. Please contact support.")
                    .setColor(Color.RED)
                    .build();
        }
    }
}
