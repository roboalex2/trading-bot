package at.discord.bot.service.binance.order;

import at.discord.bot.mapper.OrderMapper;
import at.discord.bot.model.binance.BinanceContext;
import at.discord.bot.model.binance.Order;
import at.discord.bot.persistent.OrderRepository;
import at.discord.bot.persistent.model.OrderEntity;
import at.discord.bot.service.binance.credential.BinanceContextProviderService;
import at.discord.bot.service.user.UserService;
import com.binance.connector.client.WebSocketStreamClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderMonitorService {

    private final WebSocketStreamClient webSocketStreamClient;
    private final UserService userService;
    private final OrderMapper orderMapper;
    private final BinanceContextProviderService binanceContextProviderService;
    private final OrderRepository orderRepository;
    private final OrderEventProcessorService orderEventProcessorService;

    private Map<Long, String> activeListenKeys = new HashMap<>();
    private Map<Long, Integer> openWebSockets = new HashMap<>();
    @EventListener(ApplicationReadyEvent.class)
    private void openWebsocketStream() {
        userService.getActiveUsers().stream()
            .map(binanceContextProviderService::getUserContext)
            .filter(Objects::nonNull)
            .forEach(this::registerUserMonitor);
    }

    public synchronized void registerUserMonitor(BinanceContext context) {
        if (activeListenKeys.get(context.getDiscordUserId()) != null) {
            invalidateListenKey(context);
            if (openWebSockets.get(context.getDiscordUserId()) != null) {
                webSocketStreamClient.closeConnection(openWebSockets.get(context.getDiscordUserId()));
            }
            activeListenKeys.remove(context.getDiscordUserId());
            openWebSockets.remove(context.getDiscordUserId());
        }

        String listenKey = new JSONObject(context.getSpotClient().createUserData().createListenKey()).getString("listenKey");
        int streamId = webSocketStreamClient.listenUserStream(
            listenKey,
            message -> {
            },
            message -> userDataUpdateEvent(context, message),
            (i, m) -> {
            },
            (i, m) -> websocketClosureEvent(context, i, m),
            (t, r) -> websocketFailureEvent(context, t, r)
        );

        openWebSockets.put(context.getDiscordUserId(), streamId);
        activeListenKeys.put(context.getDiscordUserId(), listenKey);
    }

    private void userDataUpdateEvent(BinanceContext context, String message) {
        JSONObject jsonObject = new JSONObject(message);
        switch (jsonObject.getString("e")) {
            case "outboundAccountPosition":
                break;
            case "executionReport":
                Order order = orderMapper.mapFromStream(jsonObject);
                order.setDiscordUserId(context.getDiscordUserId());
                Optional<OrderEntity> byOrderId = orderRepository.findByOrderId(order.getOrderId());
                byOrderId.ifPresent(orderEntity -> {
                    order.setSource(orderEntity.getSource());
                });
                orderEventProcessorService.processOrder(order);
                break;
        }
    }

    @Scheduled(cron = "1 */20 * * * *")
    private void sendKeepAlive() {
        userService.getActiveUsers().stream()
            .map(binanceContextProviderService::getUserContext)
            .filter(Objects::nonNull)
            .forEach(context -> {
                try {
                    context.getSpotClient().createUserData().extendListenKey(
                        new HashMap<>(Map.of("listenKey", activeListenKeys.get(context.getDiscordUserId())))
                    );
                } catch (Exception exception) {
                    log.warn("Faild to renew listen-key for user " + context.getDiscordUserId() + " Details: " + exception.getMessage(), exception);
                }
            });

    }

    private void invalidateListenKey(BinanceContext context) {
        try {
            context.getSpotClient().createUserData().closeListenKey(
                new HashMap<>(Map.of("listenKey", activeListenKeys.get(context.getDiscordUserId())))
            );
        } catch (RuntimeException exception) {
            log.warn("Failed to delete userData listenKey. For user " + context.getDiscordUserId(), exception);
        }
    }

    private void websocketFailureEvent(BinanceContext context, Throwable throwable, Response response) {
        log.warn(response.message(), throwable);
        invalidateListenKey(context);
        registerUserMonitor(context);
    }

    private void websocketClosureEvent(BinanceContext context, int i, String message) {
        log.warn(message);
        invalidateListenKey(context);
        registerUserMonitor(context);
    }
}
