package at.discord.bot.listener;

import at.discord.bot.service.binance.symbol.SymbolProviderService;
import at.discord.bot.service.command.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final SymbolProviderService symbolProviderService;
    private final List<CommandProcessor> botCommands;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName().toLowerCase();
        try {
            botCommands.stream()
                    .filter(el -> el.getCommandName().equalsIgnoreCase(commandName))
                    .findFirst()
                    .ifPresent(el -> el.processCommand(event));
        } catch (RuntimeException exception) {
            log.warn("Command Exception: ", exception);
            event.getHook().sendMessage(exception.getMessage()).queue();
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
            event.replyChoices(symbolProviderService.getAllAvailableSymbols().stream()
                            .limit(25)
                            .map(el -> new Command.Choice(el, el))
                            .collect(Collectors.toList()))
                    .queue();
        }
    }
}