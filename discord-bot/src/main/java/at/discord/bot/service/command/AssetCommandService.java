package at.discord.bot.service.command;

import at.discord.bot.config.discord.SlashCommands;
import at.discord.bot.model.asset.UserAsset;
import at.discord.bot.service.asset.UserAssetInfoProviderService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetCommandService implements CommandProcessor {

    private final static String COMMAND_NAME = SlashCommands.ASSET;

    private final UserAssetInfoProviderService userAssetInfoProviderService;

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
        Long userId = event.getUser().getIdLong();
        List<UserAsset> assetList = userAssetInfoProviderService.getUserAssets(userId);

        if (assetList == null || assetList.isEmpty()) {
            event.getHook().sendMessage("No assets found or failed to fetch asset data.")
                    .queue();
        } else {
            String assets = assetList.stream()
                    .map(UserAsset::toString)
                    .collect(Collectors.joining("\n"));

            event.getHook().sendMessage("Your assets:\n```" + assets + "```")
                    .queue();
        }
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}
