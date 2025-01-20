package at.discord.bot.service.command;

import at.discord.bot.config.discord.SlashCommands;
import at.discord.bot.persistent.StrategyDeploymentRepository;
import at.discord.bot.persistent.model.StrategyDeploymentEntity;
import at.discord.bot.service.binance.credential.BinanceContextProviderService;
import at.discord.bot.service.strategy.ActiveStrategyDeploymentService;
import at.discord.bot.service.strategy.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyCommandService implements CommandProcessor {

    private final static String COMMAND_NAME = SlashCommands.STRATEGY;

    private final StrategyService strategyService;
    private final ActiveStrategyDeploymentService activeStrategyDeploymentService;
    private final StrategyDeploymentRepository strategyDeploymentRepository;
    private final BinanceContextProviderService binanceContextProviderService;

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
            case "pause":
                handlePauseStrategy(event);
                break;
            case "start":
                handleStartStrategy(event);
                break;
            case "show":
                handleShowStrategy(event);
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

        Boolean strategyActive = getValidatedBoolean(event, "strategy-active", "strategy active status");
        if (strategyActive == null) {
            return; // Validation error message already sent
        }

        try {
            Long userId = event.getUser().getIdLong();
            Long deploymentId = strategyService.deployStrategy(userId, strategyName, strategyActive);

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

    private void handlePauseStrategy(SlashCommandInteractionEvent event) {
        Long deploymentId = getValidatedLong(event, "deployment-id", "deployment ID");
        if (deploymentId == null) {
            return; // Validation error message already sent
        }

        try {
            Long userId = event.getUser().getIdLong();
            // Fetch the deployment entity to ensure ownership
            StrategyDeploymentEntity deploymentEntity = strategyDeploymentRepository.findByDeploymentId(deploymentId)
                    .orElseThrow(() -> new IllegalArgumentException("No strategy with ID `" + deploymentId + "` found."));

            if (!deploymentEntity.getDiscordUserId().equals(userId)) {
                throw new IllegalArgumentException("No strategy with ID `" + deploymentId + "` found.");
            }
            boolean active = deploymentEntity.getActive();

            activeStrategyDeploymentService.makeInactiveDeployment(deploymentId);
            event.getHook().sendMessage(String.format("Strategy deployment `%d` set to paused (active=`false`) successfully. Previous active=`%b`", deploymentId, active))
                    .queue();
        } catch (Exception e) {
            log.warn("Error pausing strategy with ID {}: {}", deploymentId, e.getMessage());
            event.getHook().sendMessage(String.format("Failed to pause strategy deployment `%d`. Reason: %s", deploymentId, e.getMessage()))
                    .queue();
        }
    }

    private void handleStartStrategy(SlashCommandInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        Long deploymentId = getValidatedLong(event, "deployment-id", "deployment ID");
        if (deploymentId == null) {
            return; // Validation error message already sent
        }

        if (binanceContextProviderService.getUserContext(userId) == null) {
            event.getHook().sendMessage("Cannot start the strategy because Binance credentials are not set for your account.")
                    .queue();
            return;
        }

        try {
            StrategyDeploymentEntity deploymentEntity = strategyDeploymentRepository.findByDeploymentId(deploymentId)
                    .orElseThrow(() -> new IllegalArgumentException("No strategy with ID `" + deploymentId + "` found."));

            if (!deploymentEntity.getDiscordUserId().equals(userId)) {
                throw new IllegalArgumentException("No strategy with ID `" + deploymentId + "` found.");
            }
            boolean active = deploymentEntity.getActive();

            activeStrategyDeploymentService.makeActiveDeployment(deploymentEntity);

            event.getHook().sendMessage(String.format("Strategy deployment `%d` set started/resumed (active=`true`) successfully. Previous active=`%b`", deploymentId, active))
                    .queue();
        } catch (Exception e) {
            log.warn("Error starting strategy with ID {}: {}", deploymentId, e.getMessage());
            event.getHook().sendMessage(String.format("Failed to start/resume strategy deployment `%d`. Reason: %s", deploymentId, e.getMessage()))
                    .queue();
        }
    }

    private void handleShowStrategy(SlashCommandInteractionEvent event) {
        Long deploymentId = getValidatedLong(event, "deployment-id", "deployment ID");
        if (deploymentId == null) {
            return; // Validation error message already sent
        }

        Long userId = event.getUser().getIdLong();
        StrategyDeploymentEntity deploymentEntity = strategyDeploymentRepository.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No strategy deployment found with ID `" + deploymentId + "`."
                ));

        // Validate ownership
        if (!deploymentEntity.getDiscordUserId().equals(userId)) {
            throw new IllegalArgumentException("No strategy deployment found with ID `" + deploymentId + "`.");
        }

        // Build a detailed message with all fields of the entity.
        // Make sure to handle any null checks if needed.
        StringBuilder sb = new StringBuilder("```\n");
        sb.append("Deployment ID: ").append(deploymentEntity.getDeploymentId()).append("\n");
        sb.append("Strategy Name: ").append(deploymentEntity.getStrategyName()).append("\n");
        sb.append("Discord User ID: ").append(deploymentEntity.getDiscordUserId()).append("\n");
        sb.append("Active: ").append(deploymentEntity.getActive()).append("\n");
        sb.append("Created At: ").append(deploymentEntity.getCreatedAt()).append("\n");
        if (deploymentEntity.getUpdatedAt() != null) {
            sb.append("Updated At: ").append(deploymentEntity.getUpdatedAt()).append("\n");
        }
        sb.append("Deployment Settings: ").append(deploymentEntity.getDeploymentSettings()).append("\n");

        sb.append("```");

        event.getHook().sendMessage(sb.toString()).queue();
    }

    private void handleListStrategies(SlashCommandInteractionEvent event) {
        Long userId = event.getUser().getIdLong();
        List<StrategyDeploymentEntity> deployments = strategyService.listStrategyDeployments(userId);

        if (deployments.isEmpty()) {
            event.getHook().sendMessage("You have no strategy deployments.")
                    .queue();
            return;
        }

        StringBuilder response = new StringBuilder("**Your Strategy Deployments:**\n");
        for (StrategyDeploymentEntity deployment : deployments) {
            String status = deployment.getActive() ? "✅ Active" : "⏸️ Paused";
            String createdAtFormatted = deployment.getCreatedAt().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            response.append(String.format("- **ID:** %d | **Strategy:** %s | **Status:** %s | **Created:** %s\n",
                    deployment.getDeploymentId(),
                    deployment.getStrategyName(),
                    status,
                    createdAtFormatted));
        }

        event.getHook().sendMessage(response.toString()).queue();
    }

    private Boolean getValidatedBoolean(SlashCommandInteractionEvent event, String optionName, String displayName) {
        return Optional.ofNullable(event.getOption(optionName))
                .map(OptionMapping::getAsBoolean)
                .orElseGet(() -> {
                    event.getHook().sendMessage(String.format("Please provide a valid %s.", displayName)).queue();
                    return null;
                });
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