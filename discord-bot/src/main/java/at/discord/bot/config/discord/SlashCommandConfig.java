package at.discord.bot.config.discord;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class SlashCommandConfig {

    private final JDA jdaInstance;
    private final List<ListenerAdapter> eventListeners;

    @EventListener(ApplicationReadyEvent.class)
    public void configureRuntime() {
        jdaInstance.addEventListener(eventListeners.toArray());

        jdaInstance.updateCommands().addCommands(
                Commands.slash(SlashCommands.ALERT, "Add/List/Remove a role capable of reminder management")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("add", "Create a new price alert")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "symbol", "The symbol (base/quote asset pair) we want an alert for")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "price", "If this price is crossed the alert fires")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("remove", "Remove a price alert")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "symbol", "The symbol (base/quote asset pair) of the alert")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "price", "The trigger price of the alert")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("list", "List currently present price alerts")
                        )
        ).queue();
    }
}