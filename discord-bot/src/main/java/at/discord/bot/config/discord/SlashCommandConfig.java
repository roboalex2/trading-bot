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
                        ),
                Commands.slash(SlashCommands.BINANCE_CREDENTIALS, "Manage Binance API credentials")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("add", "Add a new Binance API key")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "apiKey", "Your Binance API key")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "apiSecret", "Your Binance API secret key")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "label", "A label to identify your API key")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("delete", "Delete an existing Binance API key")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "label", "Label of the API key to delete")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("list", "List all stored Binance API keys")
                        ),
                Commands.slash(SlashCommands.PLACE_MARKET_ORDER, "Place a market order on Binance")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("buy", "Place a market buy order")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "symbol", "The trading pair symbol (e.g., BTC/USDT)")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "quantity", "The quantity of the asset to buy")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("sell", "Place a market sell order")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "symbol", "The trading pair symbol (e.g., BTC/USDT)")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "quantity", "The quantity of the asset to sell")
                                                        .setRequired(true)
                                        )
                        )
        ).queue();
    }
}