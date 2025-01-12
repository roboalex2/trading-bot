package at.discord.bot.listener;

import at.discord.bot.config.discord.SlashCommands;
import at.discord.bot.service.binance.SymbolService;
import at.discord.bot.service.command.PriceAlertCommandService;
import at.discord.bot.service.command.BinanceCredentialsCommandService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final PriceAlertCommandService priceAlertCommandService;
    private final BinanceCredentialsCommandService binanceCredentialsCommandService;
    private final SymbolService symbolService;
    //implement new service

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName().toLowerCase();

        switch (commandName) {
            case SlashCommands.ALERT:
                priceAlertCommandService.processCommand(event);
                break;
            case SlashCommands.BINANCE_CREDENTIALS:
                binanceCredentialsCommandService.processCommand(event);
                break;
            default:
                break;
        }

    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        String optionName = event.getFocusedOption().getName().toLowerCase();
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }

        // TODO refactor
        if ("symbol".equals(optionName)) {
            event.replyChoices(symbolService.getAllAvailableSymbols().stream()
                            .limit(25)
                            .map(el ->  new Command.Choice(el, el))
                            .collect(Collectors.toList()))
                    .queue();
        }
    }
}