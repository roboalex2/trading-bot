package at.discord.bot.service.command;

import at.discord.bot.service.binance.SymbolService;
import at.discord.bot.service.order.OrderLimitService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderLimitCommandService {

    private final SymbolService symbolService;
    private final OrderLimitService orderLimitService;

    public void processCommand(SlashCommandInteractionEvent event) {

        String subcommand = event.getSubcommandName();
        if (subcommand == null || event.getUser() == null || event.getGuild() == null) {
            event.reply("Command, User or Guild not found.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply() // Tell Discord that a reply will come later
                .setEphemeral(true) // Response is only visible to the user
                .queue();

        switch (subcommand) {
            case "create":
                createLimitOrder(event);
                break;
            case "list":
                // TODO: Handle listing orders (fetch from service and display)
                break;
            case "cancel":
                // TODO: Handle canceling an order (pass ID to service)
                break;
            default:
                event.getHook().sendMessage("Unknown subcommand.")
                        .queue();
        }
    }

    private void createLimitOrder(SlashCommandInteractionEvent event) {
        // Extracting options
        String buyOrSell = Optional.ofNullable(event.getOption("buy_or_sell"))
                .map(OptionMapping::getAsString)
                .map(String::toUpperCase)
                .orElse(null);

        String symbol = Optional.ofNullable(event.getOption("symbol"))
                .map(OptionMapping::getAsString)
                .map(String::toUpperCase)
                .orElse(null);

        if (symbol == null || symbolService.getAllAvailableSymbols().stream().noneMatch(symbol::equalsIgnoreCase)) {
            event.getHook().sendMessage(String.format(
                    "The symbol `%s` is not available.",
                    symbol
            )).queue();
            return;
        }

        // Extract quantity and price
        String quantityStr = Optional.ofNullable(event.getOption("quantity"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        String priceStr = Optional.ofNullable(event.getOption("price"))
                .map(OptionMapping::getAsString)
                .orElse(null);

        if (quantityStr == null || priceStr == null) {
            event.getHook().sendMessage("Both quantity and price are required.")
                    .queue();
            return;
        }

        BigDecimal quantity;
        BigDecimal price;
        try {
            quantity = new BigDecimal(quantityStr);
            price = new BigDecimal(priceStr);
        } catch (NumberFormatException ex) {
            event.getHook().sendMessage("Invalid format for quantity or price.")
                    .queue();
            return;
        }

        // Pass validated data to OrderLimitService
        // TODO: Implement actual order creation logic in the OrderLimitService
        orderLimitService.createLimitOrder(buyOrSell, symbol, quantity, price);
    }
}
