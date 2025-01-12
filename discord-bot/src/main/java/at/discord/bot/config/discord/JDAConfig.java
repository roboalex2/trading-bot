package at.discord.bot.config.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JDAConfig {

    private final Environment environment;

    @Bean
    public JDABuilder jdaBuilder(DiscordConfigProperties discordConfigProperties) {
        var builder = JDABuilder.createDefault(discordConfigProperties.getToken());

        // Disable parts of the cache
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Set activity (like "playing Something")
        builder.setActivity(Activity.competing("INITIALIZING"));

        return builder;
    }

    @Bean
    public JDA jdaInstance(JDABuilder jdaBuilder) {
        try {
            return jdaBuilder.build();
        } catch (InvalidTokenException exception) {
            if (List.of(environment.getActiveProfiles()).contains("test")) {
                log.warn("No TOKEN provided. Running in TEST mode.");
                return new DummyJDAImpl();
            }
            throw exception;
        }
    }
}