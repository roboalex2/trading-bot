package at.discord.bot.mapper;

import at.discord.bot.model.binance.Order;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
public class OrderMapper {

    public Order mapFromStream(JSONObject jsonObject) {
        try {
            return Order.builder()
                .orderId(jsonObject.optLong("i")) // Maps "i" to orderId
                .clientOrderId(jsonObject.optString("c")) // Maps "c" to clientOrderId
                .side(jsonObject.optString("S")) // Maps "S" to side
                .symbol(jsonObject.optString("s")) // Maps "s" to symbol
                .status(jsonObject.optString("X")) // Maps "X" to status
                .type(jsonObject.optString("o")) // Maps "o" to type
                .price(getExecutionPrice(jsonObject)) // Maps "p" to price
                .origQty(jsonObject.has("q") ? jsonObject.getBigDecimal("q") : null) // Maps "q" to origQty
                .executedQty(jsonObject.has("z") ? jsonObject.getBigDecimal("z") : null) // Maps "z" to executedQty
                .stopPrice(jsonObject.has("P") ? jsonObject.getBigDecimal("P") : null) // Maps "P" to stopPrice
                .time(jsonObject.has("O") ? jsonObject.getLong("O") : null) // Maps "O" to time (order creation time)
                .updateTime(jsonObject.has("E") ? jsonObject.getLong("E") : null) // Maps "E" to updateTime (event time)
                .timeInForce(jsonObject.optString("f")) // Maps "f" to timeInForce
                .commissionAmount(jsonObject.has("n") ? jsonObject.getBigDecimal("n") : null)
                .commissionAsset(extractCommissionAsset(jsonObject))
                .build();
        } catch (Exception exception) {
            log.error("OrderMapping error", exception);
            log.error(jsonObject.toString());
            throw exception;
        }
    }

    private String extractCommissionAsset(JSONObject jsonObject) {
        if (jsonObject.has("N") && jsonObject.get("N") != null && !jsonObject.isNull("N")) {
            return jsonObject.getString("N");
        }
        return null;
    }

    private BigDecimal getExecutionPrice(JSONObject jsonObject) {
        if (!jsonObject.has("p") || jsonObject.getBigDecimal("p").doubleValue() == 0d) {
            if (jsonObject.has("Z") && jsonObject.has("z")) {
                return jsonObject.getBigDecimal("Z").divide(jsonObject.getBigDecimal("z"), RoundingMode.HALF_UP);
            }
            return jsonObject.has("P") && jsonObject.getBigDecimal("P").doubleValue() != 0d ? jsonObject.getBigDecimal("P") : new BigDecimal(-1);
        }
        return jsonObject.getBigDecimal("p");
    }
}
