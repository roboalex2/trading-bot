package at.discord.bot.service.command;

import at.discord.bot.config.discord.SlashCommands;
import at.discord.bot.model.binance.BinanceCredentials;
import at.discord.bot.model.strategy.StrategyDeploymentContext;
import at.discord.bot.service.binance.credential.BinanceContextProviderService;
import at.discord.bot.service.binance.credential.CredentialsDataAccessService;
import at.discord.bot.service.binance.order.OrderMonitorService;
import at.discord.bot.service.strategy.ActiveStrategyDeploymentService;
import at.discord.bot.service.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceKeyCommandService implements CommandProcessor {

    private final static String COMMAND_NAME = SlashCommands.BINANCE_KEY;

    private final CredentialsDataAccessService credentialsDataAccessService;
    private final OrderMonitorService orderMonitorService;
    private final ActiveStrategyDeploymentService activeStrategyDeploymentService;

    @Override
    public void processCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null || event.getUser() == null || event.getGuild() == null) {
            event.reply("Command, User or Guild not found.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply()
                .setEphemeral(true)
                .queue();

        switch (subcommand) {
            case "set":
                setBinanceKey(event);
                break;
            case "clear":
                clearBinanceKey(event);
                break;
            default:
                event.getHook().sendMessage("Unknown subcommand.")
                        .queue();
        }
    }

    private void setBinanceKey(SlashCommandInteractionEvent event) {
        // Get the Binance API key from the user input
        byte[] apiKey = Optional.ofNullable(event.getOption("api-key"))
                .map(OptionMapping::getAsString)
                .map(el -> el.getBytes(StandardCharsets.UTF_8))
                .orElse(null);

        if (apiKey == null || apiKey.length == 0) {
            event.getHook().sendMessage("The API key is required.")
                    .queue();
            return;
        }

        byte[] secretApiKey = null;
        try {
            String url = event.getOption("secret-api-key").getAsAttachment().getUrl();
            InputStream inputStream = new URL(url).openStream();
            secretApiKey = inputStream.readAllBytes();
        } catch (RuntimeException | IOException exception) {
            log.warn("Binance Key File Download Failed", exception);
            event.getHook().sendMessage("Failed to download key-file. Details: " + exception.getMessage())
                    .queue();
            return;
        }

        BinanceCredentials credentials = BinanceCredentials.builder()
                .discordUserId(event.getUser().getIdLong())
                .apiKey(apiKey)
                .secretApiKey(secretApiKey)
                .build();

        credentialsDataAccessService.setCredentials(credentials);
        event.getHook().sendMessage("Your Binance API key has been set successfully. Try a command to check validity!")
                .queue();
        orderMonitorService.registerUserMonitor(credentials.getDiscordUserId());
    }

    private void clearBinanceKey(SlashCommandInteractionEvent event) {
        List<StrategyDeploymentContext> activeDeploymentsForUser = activeStrategyDeploymentService.getActiveDeploymentsForUser(event.getUser().getIdLong());

        boolean success = credentialsDataAccessService.clearCredentials(event.getUser().getIdLong());
        if (success) {
            try {
                activeDeploymentsForUser.forEach(deploymentContext ->
                        activeStrategyDeploymentService.makeInactiveDeployment(deploymentContext.getDeploymentId())
                );
                orderMonitorService.unregisterUserMonitor(event.getUser().getIdLong());
            } catch (Exception e) {
                log.warn("Failed to unregister binance context features (StrategyDeployment and OrderMonitor)", e);
            }
            event.getHook().sendMessage("Your Binance API key has been cleared." + (activeDeploymentsForUser.isEmpty() ? "" : " All your deployments have been paused."))
                    .queue();
        } else {
            event.getHook().sendMessage("You don't have a Binance API key set.")
                    .queue();
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}
