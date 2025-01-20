package at.discord.bot.service.command;

import at.discord.bot.config.discord.SlashCommands;
import at.discord.bot.mapper.OrderEmbedMapper;
import at.discord.bot.model.binance.Order;
import at.discord.bot.service.binance.order.OrderService;
import at.discord.bot.service.binance.symbol.SymbolProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandService implements CommandProcessor {

    private final static String COMMAND_NAME = SlashCommands.ORDER;

    private final OrderService orderService;
    private final SymbolProviderService symbolProviderService;
    private final OrderEmbedMapper orderEmbedMapper;

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
            .setEphemeral(true)
            .queue();

        switch (subcommand) {
            case "limit":
                handleLimitOrder(event);
                break;
            case "market":
                handleMarketOrder(event);
                break;
            case "list":
                handleListOrders(event);
                break;
            case "cancel":
                handleCancelOrder(event);
                break;
            default:
                event.getHook().sendMessage("Unknown subcommand.")
                    .queue();
        }
    }

    private void handleLimitOrder(SlashCommandInteractionEvent event) {
        String type = getValidatedAction(event, "type");
        String symbol = getValidatedSymbol(event, "symbol");
        BigDecimal quantity = getValidatedBigDecimal(event, "quantity", "quantity");
        BigDecimal price = getValidatedBigDecimal(event, "price", "price");

        if (type == null || symbol == null || quantity == null || price == null) {
            return; // Validation error messages are already sent in helper methods
        }

        String source = "MANUAL";
        Long orderId = orderService.placeLimitOrder(event.getUser().getIdLong(), type, symbol, quantity.toString(), price.toString(), source);
        event.getHook().sendMessage(String.format("Limit `%d` order placed successfully.", orderId))
            .queue();
    }

    private void handleMarketOrder(SlashCommandInteractionEvent event) {
        String type = getValidatedAction(event, "type");
        String symbol = getValidatedSymbol(event, "symbol");
        BigDecimal quantity = getValidatedBigDecimal(event, "quantity", "quantity");

        if (type == null || symbol == null || quantity == null) {
            return; // Validation error messages are already sent in helper methods
        }

        String source = "MANUAL"; // Source is set to MANUAL
        Long orderId = orderService.placeMarketOrder(event.getUser().getIdLong(), type, symbol, quantity.toString(), source);
        event.getHook().sendMessage(String.format("Market `%d` order placed successfully.", orderId))
            .queue();
    }

    private void handleListOrders(SlashCommandInteractionEvent event) {
        List<Order> openOrders = orderService.getOpenOrders(event.getUser().getIdLong());

        List<MessageEmbed> currentEmbedBatch = new ArrayList<>();
        List<Button> currentButtons = new ArrayList<>();
        for (Order order : openOrders) {
            MessageEmbed embed = orderEmbedMapper.mapOrderToEmbed(order);
            Button cancelButton = Button.danger("cancel-order:" + order.getOrderId(), "Cancel " + order.getOrderId());

            // Add the embed to the current batch
            currentEmbedBatch.add(embed);

            // Add the button if the order source is known
            if (order.getSource() != null && !"UNKNOWN".equals(order.getSource())) {
                currentButtons.add(cancelButton);
            }

            // Check if adding the next embed or button would exceed limits
            if (currentEmbedBatch.size() == 10 || currentButtons.size() == 5) {
                // Send the current batch as a message
                WebhookMessageCreateAction<Message> messageAction = event.getHook().sendMessageEmbeds(currentEmbedBatch);
                if (!currentButtons.isEmpty()) {
                    messageAction.addActionRow(currentButtons).queue();
                } else {
                    messageAction.queue();
                }

                // Clear the batches for the next set
                currentEmbedBatch.clear();
                currentButtons.clear();
            }
        }

        // Send any remaining embeds/buttons after the loop
        if (!currentEmbedBatch.isEmpty()) {
            WebhookMessageCreateAction<Message> messageAction = event.getHook().sendMessageEmbeds(currentEmbedBatch);
            if (!currentButtons.isEmpty()) {
                messageAction.addActionRow(currentButtons).queue();
            } else {
                messageAction.queue();
            }
        }
    }

    private void handleCancelOrder(SlashCommandInteractionEvent event) {
        Long orderId = getValidatedLong(event, "id", "order ID");
        if (orderId == null) {
            return; // Validation error message is already sent in helper method
        }

        orderService.cancelOrder(event.getUser().getIdLong(), orderId);
        event.getHook().sendMessage("Order canceled successfully.")
            .queue();
    }

    private String getValidatedAction(SlashCommandInteractionEvent event, String optionName) {
        return Optional.ofNullable(event.getOption(optionName))
            .map(OptionMapping::getAsString)
            .filter(action -> action.equalsIgnoreCase("buy") || action.equalsIgnoreCase("sell"))
            .orElseGet(() -> {
                event.getHook().sendMessage("Action must be 'buy' or 'sell'.").queue();
                return null;
            });
    }

    private String getValidatedSymbol(SlashCommandInteractionEvent event, String optionName) {
        String symbol = Optional.ofNullable(event.getOption(optionName))
            .map(OptionMapping::getAsString)
            .map(String::toUpperCase)
            .orElse(null);

        if (symbol == null || !symbolProviderService.getAllAvailableSymbols().contains(symbol)) {
            event.getHook().sendMessage(
                    String.format("The symbol `%s` is not available on Binance.", symbol))
                .queue();
            return null;
        }
        return symbol;
    }

    private BigDecimal getValidatedBigDecimal(SlashCommandInteractionEvent event, String optionName, String displayName) {
        try {
            return Optional.ofNullable(event.getOption(optionName))
                .map(OptionMapping::getAsString)
                .map(value -> value.replace(',', '.'))
                .map(BigDecimal::new)
                .orElseThrow(() -> new IllegalArgumentException("Invalid value for " + displayName));
        } catch (Exception e) {
            event.getHook().sendMessage("The provided " + displayName + " is not valid.").queue();
            return null;
        }
    }

    private Long getValidatedLong(SlashCommandInteractionEvent event, String optionName, String displayName) {
        return Optional.ofNullable(event.getOption(optionName))
            .map(OptionMapping::getAsLong)
            .orElseGet(() -> {
                event.getHook().sendMessage("Please provide a valid " + displayName + ".").queue();
                return null;
            });
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}
