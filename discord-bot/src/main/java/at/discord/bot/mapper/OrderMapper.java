package at.discord.bot.mapper;

import at.discord.bot.model.binance.Order;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderMapper {

    public Order mapFromStream(JSONObject jsonObject) {
        return Order.builder()
            .orderId(jsonObject.optLong("i")) // Maps "i" to orderId
            .clientOrderId(jsonObject.optString("c")) // Maps "c" to clientOrderId
            .side(jsonObject.optString("S")) // Maps "S" to side
            .symbol(jsonObject.optString("s")) // Maps "s" to symbol
            .status(jsonObject.optString("X")) // Maps "X" to status
            .type(jsonObject.optString("o")) // Maps "o" to type
            .price(jsonObject.has("p") ? jsonObject.getBigDecimal("p") : null) // Maps "p" to price
            .origQty(jsonObject.has("q") ? jsonObject.getBigDecimal("q") : null) // Maps "q" to origQty
            .executedQty(jsonObject.has("z") ? jsonObject.getBigDecimal("z") : null) // Maps "z" to executedQty
            .stopPrice(jsonObject.has("P") ? jsonObject.getBigDecimal("P") : null) // Maps "P" to stopPrice
            .time(jsonObject.has("O") ? jsonObject.getLong("O") : null) // Maps "O" to time (order creation time)
            .updateTime(jsonObject.has("E") ? jsonObject.getLong("E") : null) // Maps "E" to updateTime (event time)
            .timeInForce(jsonObject.optString("f")) // Maps "f" to timeInForce
            .commissionAmount(jsonObject.has("n") ? jsonObject.getBigDecimal("n") : null)
            .commissionAsset(jsonObject.has("N") ? jsonObject.getString("N") : null)
            .build();
    }
}
