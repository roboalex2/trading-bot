package at.discord.bot.service.messaging;

import at.discord.bot.persistent.model.PriceAlertEntity;
import at.discord.bot.service.alert.PriceAlertService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.stereotype.Service;
import org.ta4j.core.num.Num;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogMessagingService {

    public static final String ALERT_MESSAGE_CHANNEL = "ALERT_MESSAGE_CHANNEL";
    private final JDA jdaInstance;
    private final MessageChannelService messageChannelService;

    public void sendPublicLogMessage(MessageEmbed message) {
        Optional.ofNullable(jdaInstance.getTextChannelById(messageChannelService.getChannel(ALERT_MESSAGE_CHANNEL)))
                .ifPresent(txt -> txt.sendMessageEmbeds(message).queue());
    }
}
