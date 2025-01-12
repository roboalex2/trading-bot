package at.discord.bot.service.command;

import at.discord.bot.service.asset.AssetService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AssetCommandService {

    private final AssetService assetService;

    public void processCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null || event.getUser() == null || event.getGuild() == null) {
            event.reply("Command, User, or Guild not found.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply()
                .setEphemeral(true) // Make the response visible only to the user
                .queue();

        if ("list".equals(subcommand)) {
            listAssets(event);
        } else {
            event.getHook().sendMessage("Unknown subcommand.")
                    .queue();
        }
    }

    private void listAssets(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String assetList = assetService.getUserAssets(userId);

        if (assetList == null || assetList.isEmpty()) {
            event.getHook().sendMessage("No assets found or failed to fetch asset data.")
                    .queue();
        } else {
            event.getHook().sendMessage("Your assets:\n```" + assetList + "```")
                    .queue();
        }
    }
}
