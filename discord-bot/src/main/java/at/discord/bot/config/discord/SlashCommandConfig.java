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
    public static final String BINANCE_KEY_COMMAND = "binance-key";
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
                Commands.slash(BINANCE_KEY_COMMAND, "Manage your Binance API key")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("set", "Set your Binance API key")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "api-key", "The Binance API key to set")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("clear", "Clear your Binance API key")
                        ),
                Commands.slash(SlashCommands.Order_Market, "Place a market order on Binance")
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
                        ),
                Commands.slash(SlashCommands.ORDER_LIMIT, "Manage limit orders on Binance")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("create", "Create a limit order")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "buy_or_sell", "Place a limit buy or sell order")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "symbol", "The trading pair symbol (e.g., BTC/USDT)")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "quantity", "The quantity of the asset to buy or sell")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "price", "The price at which to place the order")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("list", "List all open limit orders")
                                        .setDescription("Shows all open limit orders that have not been fulfilled."),
                                new SubcommandData("cancel", "Cancel a limit order")
                                        .addOptions(
                                                new OptionData(OptionType.INTEGER, "id", "The ID of the order to cancel")
                                                        .setRequired(true)
                                        )
                        )
        ).queue();
    }
}