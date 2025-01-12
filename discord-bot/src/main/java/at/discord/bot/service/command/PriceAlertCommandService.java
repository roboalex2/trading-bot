package at.discord.bot.service.command;

import at.discord.bot.persistent.PriceAlertRepository;
import at.discord.bot.persistent.model.PriceAlertEntity;
import at.discord.bot.service.alert.PriceAlertService;
import at.discord.bot.service.binance.SymbolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceAlertCommandService {

    private final SymbolService symbolService;
    private final PriceAlertService priceAlertService;

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
                addPriceAlert(event);
                break;
            case "remove":
                removePriceAlert(event);
                break;
            case "list":
            default:
                listPriceAlert(event);
                break;
        }
    }

    private void addPriceAlert(SlashCommandInteractionEvent event) {
        String symbol = Optional.ofNullable(event.getOption("symbol"))
            .map(OptionMapping::getAsString)
            .map(String::toUpperCase)
            .orElse(null);

        if (symbol == null || symbolService.getAllAvailableSymbols().stream().noneMatch(symbol::equalsIgnoreCase)) {
            event.getHook().sendMessage(String.format(
                "The symbol `%s` is not available at binance.",
                symbol
            )).queue();
            return;
        }

        BigDecimal price = null;
        try {
            price = Optional.ofNullable(event.getOption("price"))
                .map(OptionMapping::getAsString)
                .map(el -> el.replace(',', '.'))
                .map(BigDecimal::new)
                .orElse(null);
        } catch (Exception exception) {
            // No handling needed
        }

        if (price == null) {
            event.getHook().sendMessage(
                "The provided price is not valid."
            ).queue();
            return;
        }

        boolean success = priceAlertService.registerAlert(symbol, price);
        if (success) {
            event.getHook().sendMessage(String.format(
                "From now on there will be alerts for `%s` at `%s`. :smile:",
                symbol,
                price
            )).queue();
        } else {
            event.getHook().sendMessage(String.format(
                "There already is an alert for `%s` at `%s`.",
                symbol,
                price
            )).queue();
        }
    }

    private void removePriceAlert(SlashCommandInteractionEvent event) {
        String symbol = Optional.ofNullable(event.getOption("symbol"))
            .map(OptionMapping::getAsString)
            .map(String::toUpperCase)
            .orElse(null);

        if (symbol == null) {
            event.getHook().sendMessage(String.format(
                "The symbol `%s` is not allowed.",
                symbol
            )).queue();
            return;
        }

        BigDecimal price = null;
        try {
            price = Optional.ofNullable(event.getOption("price"))
                .map(OptionMapping::getAsString)
                .map(el -> el.replace(',', '.'))
                .map(BigDecimal::new)
                .orElse(null);
        } catch (Exception exception) {
            // No handling needed
        }

        if (price == null) {
            event.getHook().sendMessage(String.format(
                "The price `%s` is not valid.",
                price
            )).queue();
            return;
        }

        boolean success = priceAlertService.removeAlert(symbol, price);
        if (success) {
            event.getHook().sendMessage(String.format(
                "The alert for `%s` at `%s` has been removed. :smile:",
                symbol,
                price
            )).queue();
        } else {
            event.getHook().sendMessage(String.format(
                "There is no alert for `%s` at `%s`.",
                symbol,
                price
            )).queue();
        }
    }

    private void listPriceAlert(SlashCommandInteractionEvent event) {
        List<PriceAlertEntity> priceAlertEntities = priceAlertService.listAlerts();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The following alerts are present: \n```");
        priceAlertEntities.forEach(el -> stringBuilder.append(el.getSymbol() + " at " + el.getPrice() + "\n"));
        stringBuilder.append("```");

        event.getHook().sendMessage(stringBuilder.toString()).queue();
    }
}
