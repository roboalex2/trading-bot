package at.discord.bot.service.command;

import at.discord.bot.service.order.PlaceMarketOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMarketCommandService {

    private final PlaceMarketOrderService placeMarketOrderService;

    public void processCommand(SlashCommandInteractionEvent event) {

        // Retrieve the subcommand (buy or sell)
        String subcommand = event.getSubcommandName();
        if (subcommand == null || event.getUser() == null || event.getGuild() == null) {
            event.reply("Command, User, or Guild not found.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply() // Tell Discord that a reply will come later.
                .setEphemeral(true) // Make it so the response is only seen by the sender
                .queue();

        // Process according to the subcommand
        switch (subcommand) {
            case "buy":
                handleMarketOrder(event, "buy");
                break;
            case "sell":
                handleMarketOrder(event, "sell");
                break;
            default:
                event.reply("Unknown subcommand.")
                        .setEphemeral(true)
                        .queue();
                break;
        }
    }

    private void handleMarketOrder(SlashCommandInteractionEvent event, String action) {
        // Retrieve the options from the slash command
        String symbol = Optional.ofNullable(event.getOption("symbol"))
                .map(OptionMapping::getAsString)
                .map(String::toUpperCase)
                .orElse(null);

        BigDecimal quantity = null;
        try {
            quantity = Optional.ofNullable(event.getOption("quantity"))
                    .map(OptionMapping::getAsString)
                    .map(el -> el.replace(',', '.'))
                    .map(BigDecimal::new)
                    .orElse(null);
        } catch (Exception exception) {
            // No handling needed
        }

        // Validate the input
        if (symbol == null || quantity == null) {
            event.getHook().sendMessage("Please provide both the symbol (e.g., BTC/USDT) and quantity.")
                    .queue();
            return;
        }

        // Check if the action is valid (buy or sell)
        if (!action.equalsIgnoreCase("buy") && !action.equalsIgnoreCase("sell")) {
            event.getHook().sendMessage("Action must be 'buy' or 'sell'.")
                    .queue();
            return;
        }

        // TODO: Call PlaceMarketOrderService to process the market order
        placeMarketOrderService.placeOrder(action, symbol, quantity);

        // Send confirmation to Discord (immediately after passing to service)
        event.getHook().sendMessage(String.format("Market %s order for %s %s has been placed successfully!", action, quantity, symbol))
                .queue();
    }
}
