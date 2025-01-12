package at.discord.bot.service.command;

import at.discord.bot.persistent.BinanceCredentialsRepository;
import at.discord.bot.persistent.model.BinanceCredentialsEntity;
import at.discord.bot.service.credentials.BinanceCredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceCredentialsCommandService {

    private final BinanceCredentialsService binanceCredentialsService;

    public void processCommand(SlashCommandInteractionEvent event) {

        String subcommand = event.getSubcommandName();
        if (subcommand == null || event.getUser() == null || event.getGuild() == null) {
            event.reply("Command, User or Guild not found.")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.deferReply() // Tell discord that a reply will come later.
                .setEphemeral(true) // Make it so the response is only seen by the sender
                .queue();

        switch (subcommand) {
            case "add":
                addBinanceCredentials(event);
                break;
            case "delete":
                deleteBinanceCredentials(event);
                break;
            default:
                break;
        }
    }

    private void addBinanceCredentials(SlashCommandInteractionEvent event) {
        String apiKey = Optional.ofNullable(event.getOption("apiKey"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        String apiSecret = Optional.ofNullable(event.getOption("apiSecret"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        String label = Optional.ofNullable(event.getOption("label"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        if (apiKey == null || apiSecret == null || label == null) {
            event.getHook().sendMessage("API Key, API Secret, and Label are required to add Binance credentials.")
                    .queue();
            return;
        }

        boolean success = binanceCredentialsService.addCredentials(apiKey, apiSecret, label);
        if (success) {
            event.getHook().sendMessage(String.format(
                    "Successfully added Binance credentials for label `%s`.",
                    label
            )).queue();
        } else {
            event.getHook().sendMessage(String.format(
                    "There was an error adding credentials for label `%s`.",
                    label
            )).queue();
        }
    }

    private void deleteBinanceCredentials(SlashCommandInteractionEvent event) {
        String label = Optional.ofNullable(event.getOption("label"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        if (label == null) {
            event.getHook().sendMessage("Label is required to delete Binance credentials.")
                    .queue();
            return;
        }

        boolean success = binanceCredentialsService.deleteCredentials(label);
        if (success) {
            event.getHook().sendMessage(String.format(
                    "Successfully deleted Binance credentials for label `%s`.",
                    label
            )).queue();
        } else {
            event.getHook().sendMessage(String.format(
                    "No Binance credentials found for label `%s`.",
                    label
            )).queue();
        }
    }
}
