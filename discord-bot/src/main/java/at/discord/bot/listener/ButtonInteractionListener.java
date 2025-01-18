package at.discord.bot.listener;

import at.discord.bot.service.binance.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ButtonInteractionListener extends ListenerAdapter {

    private final OrderService orderService;

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (componentId.startsWith("cancel-order")) {
            String[] split = event.getComponentId().split(":");
            if (split.length > 1) {
                String order = split[1];
                event.deferReply(true).queue();
                try {
                    Long orderId = Long.parseLong(order);
                    List<ActionRow> updatedRows = new ArrayList<>();
                    for (ActionRow row : event.getMessage().getActionRows()) {
                        List<Button> updatedButtons = row.getButtons()
                            .stream()
                            .map(button -> componentId.equals(button.getId()) ? button.asDisabled() : button)
                            .toList();
                        updatedRows.add(ActionRow.of(updatedButtons));
                    }

                    // Edit the message with the updated action rows
                    event.getMessage()
                        .editMessageComponents(updatedRows)
                        .queue();

                    orderService.cancelOrder(event.getUser().getIdLong(), orderId);
                    event.getHook().sendMessage("Order `" + orderId + "` canceled.").queue();
                } catch (Exception exception) {
                    log.warn("Error on Cancel Button", exception);
                    event.getHook().sendMessage("Failed to cancel order. Details: " + exception.getMessage()).queue();
                }
            }
        }
    }
}