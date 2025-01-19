package at.discord.bot.service.command;

import at.discord.bot.config.discord.SlashCommands;
import at.discord.bot.persistent.model.StrategyDeploymentEntity;
import at.discord.bot.service.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyCommandService implements CommandProcessor {

    private final static String COMMAND_NAME = SlashCommands.STRATEGY;

    private final StrategyService strategyService;

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
            case "deploy":
                handleDeployStrategy(event);
                break;
            case "undeploy":
                handleUndeployStrategy(event);
                break;
            case "list":
                handleListStrategies(event);
                break;
            default:
                event.getHook().sendMessage("Unknown subcommand.")
                        .queue();
        }
    }

    private void handleDeployStrategy(SlashCommandInteractionEvent event) {
        String strategyName = getValidatedString(event, "strategy-name", "strategy name");
        if (strategyName == null) {
            return; // Validation error message already sent
        }

        try {
            Long userId = event.getUser().getIdLong();
            Long deploymentId = strategyService.deployStrategy(userId, strategyName);

            event.getHook().sendMessage(String.format("Strategy `%s` deployed successfully with ID `%d`.", strategyName, deploymentId))
                    .queue();
        } catch (Exception e) {
            log.warn("Error deploying strategy: {}", strategyName, e);
            event.getHook().sendMessage(String.format("Failed to deploy strategy `%s`. Reason: %s", strategyName, e.getMessage()))
                    .queue();
        }
    }

    private void handleUndeployStrategy(SlashCommandInteractionEvent event) {
        Long deploymentId = getValidatedLong(event, "deployment-id", "deployment ID");
        if (deploymentId == null) {
            return; // Validation error message already sent
        }

        try {
            Long userId = event.getUser().getIdLong();
            strategyService.undeployStrategy(deploymentId, userId);

            event.getHook().sendMessage(String.format("Strategy deployment `%d` undeployed successfully.", deploymentId))
                    .queue();
        } catch (Exception e) {
            log.warn("Error undeploying strategy with ID {}: {}", deploymentId, e.getMessage());
            event.getHook().sendMessage(String.format("Failed to undeploy strategy deployment `%d`. Reason: %s", deploymentId, e.getMessage()))
                    .queue();
        }
    }

    private void handleListStrategies(SlashCommandInteractionEvent event) {
        Long userId = event.getUser().getIdLong();
        List<StrategyDeploymentEntity> deployments = strategyService.listStrategyDeployments(userId);

        if (deployments.isEmpty()) {
            event.getHook().sendMessage("You have no strategy deployments.")
                    .queue();
            return;
        }

        StringBuilder response = new StringBuilder("```\n");
        for (StrategyDeploymentEntity deployment : deployments) {
            response.append(String.format("Strategy: %s | ID: %d | Created At: %s\n",
                    deployment.getStrategyName(),
                    deployment.getDeploymentId(),
                    deployment.getCreatedAt().toString()));
        }
        response.append("```");

        event.getHook().sendMessage(response.toString()).queue();
    }

    private String getValidatedString(SlashCommandInteractionEvent event, String optionName, String displayName) {
        return Optional.ofNullable(event.getOption(optionName))
                .map(OptionMapping::getAsString)
                .filter(value -> !value.isBlank())
                .orElseGet(() -> {
                    event.getHook().sendMessage(String.format("Please provide a valid %s.", displayName)).queue();
                    return null;
                });
    }

    private Long getValidatedLong(SlashCommandInteractionEvent event, String optionName, String displayName) {
        return Optional.ofNullable(event.getOption(optionName))
                .map(OptionMapping::getAsLong)
                .orElseGet(() -> {
                    event.getHook().sendMessage(String.format("Please provide a valid %s.", displayName)).queue();
                    return null;
                });
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }
}