package at.discord.bot.service.alert;

import at.discord.bot.config.discord.DiscordConfigProperties;
import at.discord.bot.mapper.AlertToEmbedMapper;
import at.discord.bot.persistent.model.PriceAlertEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.ta4j.core.num.Num;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertMessagingService {

    private final JDA jdaInstance;
    private final DiscordConfigProperties discordConfigProperties;
    private final AlertToEmbedMapper alertToEmbedMapper;

    public void sendCrossAboveAlert(Num currentPrice, PriceAlertEntity priceAlertEntity) {
        // TODO Dynamic source for target channel
        Optional.ofNullable(jdaInstance.getTextChannelById(discordConfigProperties.getAlertChannel()))
                .ifPresent(txt -> txt.sendMessageEmbeds(alertToEmbedMapper.mapToEmbed(currentPrice, priceAlertEntity))
                        .queue()
                );
    }

    public void sendCrossBelowAlert(Num currentPrice, PriceAlertEntity priceAlertEntity) {
        // TODO Dynamic source for target channel
        Optional.ofNullable(jdaInstance.getTextChannelById(discordConfigProperties.getAlertChannel()))
                .ifPresent(txt -> txt.sendMessageEmbeds(alertToEmbedMapper.mapToEmbed(currentPrice, priceAlertEntity))
                        .queue()
                );
    }
}
