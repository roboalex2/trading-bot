package at.discord.bot.listener;

import at.discord.bot.persistent.StrategyDeploymentRepository;
import at.discord.bot.persistent.model.StrategyDeploymentEntity;
import at.discord.bot.service.binance.symbol.SymbolProviderService;
import at.discord.bot.service.command.*;
import at.discord.bot.service.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final SymbolProviderService symbolProviderService;
    private final List<CommandProcessor> botCommands;
    private final StrategyDeploymentRepository strategyDeploymentRepository;
    private final StrategyService strategyService;

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

        if ("symbol".equals(optionName)) {
            event.replyChoices(symbolProviderService.getAllAvailableSymbols().stream()
                            .limit(25)
                            .map(el -> new Command.Choice(el, el))
                            .collect(Collectors.toList()))
                    .queue();
            return;
        }

        if ("deployment-id".equals(optionName)) {
            List<StrategyDeploymentEntity> allByDiscordUserId = strategyDeploymentRepository.findAllByDiscordUserId(event.getUser().getIdLong());

            event.replyChoices(allByDiscordUserId.stream()
                            .limit(25)
                            .map(el -> new Command.Choice(""+el.getDeploymentId(), el.getDeploymentId()))
                            .collect(Collectors.toList()))
                    .queue();
            return;
        }

        if ("setting-key".equals(optionName)) {
            int deployId = event.getOptions().stream()
                    .filter(el -> "deployment-id".equals(el.getName()))
                    .findFirst()
                    .map(OptionMapping::getAsInt)
                    .orElse(0);

            String stratName = strategyDeploymentRepository.findByDeploymentId((long) deployId)
                    .map(StrategyDeploymentEntity::getStrategyName)
                    .orElse(null);
            Map<String, String> defaultSetting = strategyService.getStrategy(stratName).getDefaultSetting();

            event.replyChoices(defaultSetting.keySet().stream()
                            .limit(25)
                            .map(el -> new Command.Choice(el, el))
                            .collect(Collectors.toList()))
                    .queue();
            return;
        }

        if ("setting-value".equals(optionName)) {
            int deployId = event.getOptions().stream()
                    .filter(el -> "deployment-id".equals(el.getName()))
                    .findFirst()
                    .map(OptionMapping::getAsInt)
                    .orElse(0);
            String settingKey = event.getOptions().stream()
                    .filter(el -> "setting-key".equals(el.getName()))
                    .findFirst()
                    .map(OptionMapping::getAsString)
                    .orElse("");

            String stratName = strategyDeploymentRepository.findByDeploymentId((long) deployId)
                    .map(StrategyDeploymentEntity::getStrategyName)
                    .orElse(null);
            Map<String, String> defaultSetting = strategyService.getStrategy(stratName).getDefaultSetting();

            event.replyChoices(new Command.Choice(defaultSetting.get(settingKey), defaultSetting.get(settingKey)))
                    .queue();
            return;
        }
    }
}