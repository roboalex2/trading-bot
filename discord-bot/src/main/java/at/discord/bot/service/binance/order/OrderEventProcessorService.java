package at.discord.bot.service.binance.order;

import at.discord.bot.model.binance.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventProcessorService {

    private final OrderMessagingService orderMessagingService;

    @Async
    public void processOrder(Order order) {
        orderMessagingService.sendOrderMonitoringMessage(order);
    }
}
