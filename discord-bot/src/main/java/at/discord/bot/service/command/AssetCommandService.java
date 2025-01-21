package at.discord.bot.service.command;

import at.discord.bot.config.discord.SlashCommands;
import at.discord.bot.model.asset.AssetHistoryDTO;
import at.discord.bot.service.binance.asset.AssetHistoryService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetCommandService implements CommandProcessor {

    private final static String COMMAND_NAME = SlashCommands.ASSET;

    private final AssetHistoryService assetHistoryService;

    @Override
    public void processCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null || event.getUser() == null || event.getGuild() == null) {
            event.reply("Command, User, or Guild not found.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply()
                .setEphemeral(true)
                .queue();

        switch (subcommand) {
            case "list":
                listAssets(event);
                break;
            case "history":
                fetchAssetHistory(event);
                break;
            default:
                event.getHook().sendMessage("Unknown subcommand.")
                        .queue();
        }
    }

    private void listAssets(SlashCommandInteractionEvent event) {
        // (Existing code to list assets)
    }

    private void fetchAssetHistory(SlashCommandInteractionEvent event) {
        // Retrieve the asset name and user ID from the command options
        String assetName = event.getOption("assetName").getAsString();
        Long userId = event.getUser().getIdLong();

        try {
            // Retrieve the asset history from the service
            List<AssetHistoryDTO> assetHistory = assetHistoryService.getAssetHistory(userId, assetName);

            if (assetHistory == null || assetHistory.isEmpty()) {
                // Send a message if no history data is found for the user and asset
                event.getHook().sendMessage("No asset history found for " + assetName + " for your account.")
                        .queue();
            } else {
                // Format the asset history data into a readable string
                String historyMessage = assetHistory.stream()
                        .map(item -> String.format("Timestamp: %s | Asset: %s | Balance: %s",
                                item.getTimestamp(), item.getAssetName(), item.getAssetBalance()))
                        .collect(Collectors.joining("\n"));

                // Send the formatted history message to the user
                event.getHook().sendMessage("Your asset history for **" + assetName + "**:\n```" + historyMessage + "```")
                        .queue();
            }
        } catch (Exception e) {
            // Handle potential errors
            event.getHook().sendMessage("An error occurred while fetching your asset history. Please try again later.")
                    .queue();
            e.printStackTrace(); // Log the error for further inspection (optional)
        }
    }


    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}
