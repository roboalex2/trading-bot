package at.discord.bot.config.discord;

import at.discord.bot.service.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
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
    private final StrategyService strategyService;

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
                Commands.slash(SlashCommands.BINANCE_KEY, "Manage your Binance API key")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("set", "Set your Binance API key")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "api-key", "Your Binance API key")
                                                        .setRequired(true),
                                                new OptionData(OptionType.ATTACHMENT, "secret-api-key", "Upload your secret Binance API key as a file")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("clear", "Clear your Binance API key")
                        ),
                Commands.slash(SlashCommands.ORDER, "Place a market order on Binance")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("limit", "Place a limit order")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "type", "The order direction BUY or SELL")
                                                        .addChoice("buy", "buy")
                                                        .addChoice("sell", "sell")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "symbol", "The trading pair symbol (e.g., BTC/USDT)")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "quantity", "The quantity of the asset to buy/sell")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "price", "The price at which to place the order")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("market", "Place a market order")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "type", "The order direction BUY or SELL")
                                                        .addChoice("buy", "buy")
                                                        .addChoice("sell", "sell")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "symbol", "The trading pair symbol (e.g., BTC/USDT)")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "quantity", "The quantity of the asset to buy/sell")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("list", "List still open orders"),
                                new SubcommandData("cancel", "Cancel a open order")
                                        .addOptions(
                                                new OptionData(OptionType.INTEGER, "id", "The ID of the order to cancel")
                                                        .setRequired(true)
                                        )
                        ),
                Commands.slash(SlashCommands.ASSET, "View your asset balances")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("list", "View all assets and their balances")
                        ),
                Commands.slash(SlashCommands.SETTING, "Change bot settings")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("global", "A setting that affects the entire bot")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "setting-id", "The setting identifier")
                                                        .addChoice("ALERT_MESSAGE_CHANNEL", "ALERT_MESSAGE_CHANNEL")
                                                        .addChoice("ORDER_MONITORING_CHANNEL", "ORDER_MONITORING_CHANNEL")
                                                        .setRequired(true),
                                                new OptionData(OptionType.CHANNEL, "channel", "The target channel")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("deployment", "Change settings of strategy deployments")
                                        .addOptions(
                                                new OptionData(OptionType.INTEGER, "deployment-id", "The deployment you want to reconfigure")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "setting-key", "The setting you want to change")
                                                        .setRequired(true),
                                                new OptionData(OptionType.STRING, "setting-value", "The value you want to apply to the setting")
                                                        .setRequired(true)
                                        )
                        ),
                Commands.slash(SlashCommands.STRATEGY, "Deploy and undeploy automated trading strategies")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.EMPTY_PERMISSIONS))
                        .addSubcommands(
                                new SubcommandData("deploy", "Deploy a new trading strategy")
                                        .addOptions(
                                                new OptionData(OptionType.STRING, "strategy-name", "The base-strategy identifier")
                                                        .addChoices(strategyChoices())
                                                        .setRequired(true),
                                                new OptionData(OptionType.BOOLEAN, "strategy-active", "Whether the strategy is active from the start or needs to be resumed.")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("undeploy", "Undeploy a trading strategy deployment")
                                        .addOptions(
                                                new OptionData(OptionType.INTEGER, "deployment-id", "The strategy deployment id")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("pause", "Pause a strategy deployment")
                                        .addOptions(
                                                new OptionData(OptionType.INTEGER, "deployment-id", "The strategy deployment id")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("start", "Start/Resume a strategy deployment's operations")
                                        .addOptions(
                                                new OptionData(OptionType.INTEGER, "deployment-id", "The strategy deployment to be resumed")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("show", "Show details about a strategy deployment")
                                        .addOptions(
                                                new OptionData(OptionType.INTEGER, "deployment-id", "The strategy deployment to show")
                                                        .setRequired(true)
                                        ),
                                new SubcommandData("list", "List your deployed trading strategies")
                        )
        ).queue();
    }

    private Command.Choice[] strategyChoices() {
        return strategyService.getAvailableStrategyNames().stream()
                .map(strategyName -> new Command.Choice(strategyName, strategyName))
                .toArray(Command.Choice[]::new);
    }
}