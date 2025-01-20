package at.discord.bot.service.binance.order;

import at.discord.bot.mapper.OrderEmbedMapper;
import at.discord.bot.model.binance.Order;
import at.discord.bot.service.channel.MessageChannelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMessagingService {

    public static final String ORDER_MONITORING_CHANNEL = "ORDER_MONITORING_CHANNEL";
    private final JDA jdaInstance;
    private final MessageChannelService messageChannelService;
    private final OrderEmbedMapper orderEmbedMapper;


    public void sendOrderMonitoringMessage(Order order) {
        Optional.ofNullable(jdaInstance.getTextChannelById(messageChannelService.getChannel(ORDER_MONITORING_CHANNEL)))
            .ifPresent(txt -> txt.sendMessageEmbeds(orderEmbedMapper.mapOrderToEmbed(order, true))
                .queue()
            );
    }
}
