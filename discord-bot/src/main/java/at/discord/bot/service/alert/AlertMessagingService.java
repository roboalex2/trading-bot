package at.discord.bot.service.alert;

import at.discord.bot.mapper.AlertToEmbedMapper;
import at.discord.bot.persistent.model.PriceAlertEntity;
import at.discord.bot.service.messaging.MessageChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;
import org.ta4j.core.num.Num;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertMessagingService {

    public static final String ALERT_MESSAGE_CHANNEL = "ALERT_MESSAGE_CHANNEL";
    private final JDA jdaInstance;
    private final MessageChannelService messageChannelService;
    private final AlertToEmbedMapper alertToEmbedMapper;

    public void sendCrossAboveAlert(Num currentPrice, PriceAlertEntity priceAlertEntity) {
        Optional.ofNullable(jdaInstance.getTextChannelById(messageChannelService.getChannel(ALERT_MESSAGE_CHANNEL)))
                .ifPresent(txt -> txt.sendMessageEmbeds(alertToEmbedMapper.mapToEmbed(currentPrice, priceAlertEntity))
                        .queue()
                );
    }

    public void sendCrossBelowAlert(Num currentPrice, PriceAlertEntity priceAlertEntity) {
        Optional.ofNullable(jdaInstance.getTextChannelById(messageChannelService.getChannel(ALERT_MESSAGE_CHANNEL)))
                .ifPresent(txt -> txt.sendMessageEmbeds(alertToEmbedMapper.mapToEmbed(currentPrice, priceAlertEntity))
                        .queue()
                );
    }
}
