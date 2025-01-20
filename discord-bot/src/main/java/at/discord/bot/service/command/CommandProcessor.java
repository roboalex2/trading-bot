package at.discord.bot.service.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;

@Service
public interface CommandProcessor {
    void processCommand(SlashCommandInteractionEvent event);

    String getCommandName();
}
