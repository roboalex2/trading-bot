package at.discord.bot.service.binance.order;

import at.discord.bot.model.binance.BinanceContext;
import at.discord.bot.model.binance.Order;
import at.discord.bot.persistent.OrderRepository;
import at.discord.bot.persistent.model.OrderEntity;
import at.discord.bot.service.binance.credential.BinanceContextProviderService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final BinanceContextProviderService binanceContextProviderService;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public List<Order> getOpenOrders(long userId) {
        BinanceContext userContext = binanceContextProviderService.getUserContext(userId);
        if (userContext == null) {
            throw new RuntimeException("Cannot load open orders for user " + userId + " as no Binance credentials are present.");
        }

        String openOrdersResponse = userContext.getSpotClient().createTrade().getOpenOrders(new HashMap<>(Map.of(
            "timestamp", Instant.now().toEpochMilli()
        )));

        try {
            // Deserialize the Binance API response into a list of Order objects
            List<Order> openOrders = objectMapper.readValue(openOrdersResponse, new TypeReference<List<Order>>() {});

            List<Long> orderIds = openOrders.stream()
                .map(Order::getOrderId)
                .filter(Objects::nonNull) // Exclude null IDs if any
                .toList();

            // Fetch corresponding orders from the database
            List<OrderEntity> orderEntities = orderRepository.findAllByOrderIdIn(orderIds);

            // Map the source field from the database to the Order objects
            Map<Long, String> orderIdToSourceMap = orderEntities.stream()
                .collect(Collectors.toMap(OrderEntity::getOrderId, OrderEntity::getSource));

            openOrders.forEach(order -> {
                String source = orderIdToSourceMap.get(order.getOrderId());
                if (source != null) {
                    order.setSource(source); // Set the source field
                } else {
                    order.setSource("UNKNOWN");
                }
            });

            return openOrders;
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load orders from Binance for user " + userId + ". Details: " + exception.getMessage());
        }
    }

    public Long placeMarketOrder(long userId, String side, String symbol, String quantity, String source) {
        return placeOrder(userId, side, symbol, quantity, null, source, "MARKET");
    }

    public Long placeLimitOrder(long userId, String side, String symbol, String quantity, String price, String source) {
        if (price == null || price.isBlank()) {
            throw new IllegalArgumentException("Price is required for a LIMIT order.");
        }
        return placeOrder(userId, side, symbol, quantity, price, source, "LIMIT");
    }

    private Long placeOrder(long userId, String side, String symbol, String quantity, String price, String source, String type) {
        BinanceContext userContext = binanceContextProviderService.getUserContext(userId);
        if (userContext == null) {
            throw new RuntimeException("Cannot place " + type + " order for user " + userId + " as no Binance credentials are present.");
        }

        // Generate a unique client order ID
        String clientOrderId = generateClientOrderId();

        // Prepare the parameters for placing the order
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("side", side.toUpperCase()); // Binance API expects uppercase (BUY/SELL)
        parameters.put("type", type);
        parameters.put("quantity", quantity);
        parameters.put("newClientOrderId", clientOrderId); // Unique client order ID
        parameters.put("timestamp", Instant.now().toEpochMilli());

        if ("LIMIT".equalsIgnoreCase(type)) {
            parameters.put("price", price);
            parameters.put("timeInForce", "GTC"); // Good Till Cancelled is the standard for limit orders
        }

        try {
            // Execute the API call to place the order
            String response = userContext.getSpotClient().createTrade().newOrder(parameters);

            // Extract Binance order ID from the response
            JSONObject jsonObject = new JSONObject(response);
            Long binanceOrderId = jsonObject.getLong("orderId");

            // Save the order in the database
            OrderEntity orderEntity = OrderEntity.builder()
                .clientOrderId(clientOrderId)
                .orderId(binanceOrderId)
                .source(source)
                .discordUserId(userId)
                .symbol(symbol)
                .build();

            orderRepository.save(orderEntity);
            log.info("{} order placed successfully for user {}: {}", type, userId, response);

            return binanceOrderId; // Return the Binance order ID
        } catch (Exception exception) {
            throw new RuntimeException("Failed to place " + type + " order for user " + userId + ". Details: " + exception.getMessage());
        }
    }

    public void cancelOrder(long userId, Long orderId) {
        BinanceContext userContext = binanceContextProviderService.getUserContext(userId);
        if (userContext == null) {
            throw new RuntimeException("Cannot cancel order for user " + userId + " as no Binance credentials are present.");
        }

        // Load the order entity from the database
        OrderEntity orderEntity = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Order with ID " + orderId + " not found in the database. The bot can only cancel what it created."));

        String symbol = orderEntity.getSymbol();

        // Prepare parameters for canceling the order
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("symbol", symbol);
        parameters.put("orderId", orderId);
        parameters.put("timestamp", Instant.now().toEpochMilli());

        try {
            // Execute the API call to cancel the order
            String response = userContext.getSpotClient().createTrade().cancelOrder(parameters);

            // Log and handle successful cancellation
            log.info("Order {} for user {} canceled successfully. Response: {}", orderId, userId, response);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to cancel order " + orderId + " for user " + userId + ". Details: " + exception.getMessage());
        }
    }

    private String generateClientOrderId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
