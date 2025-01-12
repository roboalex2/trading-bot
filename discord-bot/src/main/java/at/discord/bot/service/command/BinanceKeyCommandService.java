package at.discord.bot.service.command;

import at.discord.bot.service.binance.credential.BinanceKeyService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BinanceKeyCommandService {

    private final BinanceKeyService binanceKeyService;

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
        String apiKey = Optional.ofNullable(event.getOption("api-key"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        if (apiKey == null || apiKey.isEmpty()) {
            event.getHook().sendMessage("The API key is required.")
                    .queue();
            return;
        }

        // Validate the API key by attempting to use it with the Binance API (or mock this process)
        boolean isValid = binanceKeyService.validateApiKey(apiKey);

        if (!isValid) {
            event.getHook().sendMessage("The provided API key is invalid.")
                    .queue();
            return;
        }

        // Store the valid API key for the user
        boolean success = binanceKeyService.setApiKey(event.getUser().getId(), apiKey);
        if (success) {
            event.getHook().sendMessage("Your Binance API key has been set successfully!")
                    .queue();
        } else {
            event.getHook().sendMessage("An error occurred while setting your Binance API key.")
                    .queue();
        }
    }

    private void clearBinanceKey(SlashCommandInteractionEvent event) {
        boolean success = binanceKeyService.clearApiKey(event.getUser().getId());
        if (success) {
            event.getHook().sendMessage("Your Binance API key has been cleared.")
                    .queue();
        } else {
            event.getHook().sendMessage("You don't have a Binance API key set.")
                    .queue();
        }
    }
}
