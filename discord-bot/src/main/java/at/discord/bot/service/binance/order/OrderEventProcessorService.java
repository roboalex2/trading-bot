package at.discord.bot.service.binance.order;

import at.discord.bot.model.binance.Order;
import at.discord.bot.service.strategy.ActiveStrategyDeploymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderEventProcessorService {

    private final OrderMessagingService orderMessagingService;
    private final ActiveStrategyDeploymentService activeStrategyDeploymentService;

    @Async
    public void processOrder(Order order) {
        orderMessagingService.sendOrderMonitoringMessage(order);
        activeStrategyDeploymentService.handleOrderEvent(order);
    }
}
