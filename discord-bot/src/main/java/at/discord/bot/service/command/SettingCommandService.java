package at.discord.bot.service.command;

import at.discord.bot.config.discord.SlashCommands;
import at.discord.bot.service.messaging.MessageChannelService;
import at.discord.bot.service.strategy.StrategySettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingCommandService implements CommandProcessor {

    private final static String COMMAND_NAME = SlashCommands.SETTING;

    private final MessageChannelService messageChannelService;
    private final StrategySettingService strategySettingService;

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public void processCommand(SlashCommandInteractionEvent event) {
        String subcommand = event.getSubcommandName();
        if (subcommand == null || event.getUser() == null || event.getGuild() == null) {
            event.reply("Command, User, or Guild not found.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        // Defer the reply to allow for async processing (and ephemeral messages)
        event.deferReply()
                .setEphemeral(true)
                .queue();

        switch (subcommand) {
            case "global":
                handleGlobalSettings(event);
                break;
            case "deployment":
                handleDeploymentSettings(event);
                break;
            default:
                event.getHook().sendMessage("Unknown subcommand.")
                        .queue();
        }
    }

    private void handleGlobalSettings(SlashCommandInteractionEvent event) {
        // Validate the setting id
        String settingId = getValidatedStringOption(event, "setting-id", "Global setting ID");
        if (settingId == null) {
            return;
        }

        // Retrieve the channel from the user input
        GuildChannel channel = getValidatedChannelOption(event, "channel");
        if (channel == null) {
            return;
        }

        try {
            // Here you'd implement how your setting service saves or updates the setting
            messageChannelService.setChannel(settingId, channel.getIdLong());

            // Acknowledge success
            event.getHook().sendMessage(String.format(
                    "Global setting `%s` updated to channel <#%d>.",
                    settingId,
                    channel.getIdLong())
            ).queue();
        } catch (Exception e) {
            log.warn("Failed to update global setting {}: {}", settingId, e.getMessage());
            event.getHook().sendMessage(String.format(
                    "Failed to update global setting `%s`. Reason: %s", settingId, e.getMessage())
            ).queue();
        }
    }

    private void handleDeploymentSettings(SlashCommandInteractionEvent event) {
        Long deploymentId = getValidatedLongOption(event, "deployment-id", "deployment ID");
        String settingId = getValidatedStringOption(event, "setting-key", "deployment setting key");
        String settingValue = getValidatedStringOption(event, "setting-value", "deployment setting value");

        if (deploymentId == null || settingId == null || settingValue == null) {
            return; // If any of them is invalid, we already sent an error message
        }

        Long userId = event.getUser().getIdLong();
        strategySettingService.updateDeploymentSetting(userId, deploymentId, settingId, settingValue);

        event.getHook().sendMessage(String.format(
                "Deployment `%d` setting `%s` updated to `%s`.",
                deploymentId,
                settingId,
                settingValue)
        ).queue();
    }

    private String getValidatedStringOption(SlashCommandInteractionEvent event, String optionName, String displayName) {
        return Optional.ofNullable(event.getOption(optionName))
                .map(OptionMapping::getAsString)
                .filter(value -> !value.isBlank())
                .orElseGet(() -> {
                    event.getHook().sendMessage(String.format("Please provide a valid %s.", displayName)).queue();
                    return null;
                });
    }

    private Long getValidatedLongOption(SlashCommandInteractionEvent event, String optionName, String displayName) {
        return Optional.ofNullable(event.getOption(optionName))
                .map(OptionMapping::getAsLong)
                .orElseGet(() -> {
                    event.getHook().sendMessage(String.format("Please provide a valid %s.", displayName)).queue();
                    return null;
                });
    }

    private GuildChannel getValidatedChannelOption(SlashCommandInteractionEvent event, String optionName) {
        OptionMapping channelOption = event.getOption(optionName);
        if (channelOption == null) {
            event.getHook().sendMessage("Please provide a valid channel.").queue();
            return null;
        }
        // Make sure the provided option is actually a GuildChannel
        if (!(channelOption.getAsChannel() instanceof GuildChannel)) {
            event.getHook().sendMessage("Please provide a valid guild channel.").queue();
            return null;
        }
        return (GuildChannel) channelOption.getAsChannel();
    }
}
