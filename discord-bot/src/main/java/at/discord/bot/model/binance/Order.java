package at.discord.bot.model.binance;

import lombok.*;

import java.math.BigDecimal;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private BigDecimal avgPrice;           // Average price
    private String clientOrderId;          // Client order ID
    private BigDecimal cumBase;            // Cumulative base
    private BigDecimal executedQty;        // Executed quantity
    private Long orderId;                  // Order ID
    private BigDecimal origQty;            // Original quantity
    private String origType;               // Original type
    private BigDecimal price;              // Price
    private Boolean reduceOnly;            // Reduce only flag
    private String side;                   // Order side (BUY/SELL)
    private String positionSide;           // Position side (LONG/SHORT)
    private String status;                 // Order status
    private BigDecimal stopPrice;          // Stop price (ignored for TRAILING_STOP_MARKET)
    private Boolean closePosition;         // Close-all flag
    private String symbol;                 // Trading pair symbol
    private Long time;                     // Order creation time
    private String timeInForce;            // Time in force
    private String type;                   // Order type
    private BigDecimal activatePrice;      // Activation price (only for TRAILING_STOP_MARKET)
    private BigDecimal priceRate;          // Callback rate (only for TRAILING_STOP_MARKET)
    private Long updateTime;               // Last update time
    private String workingType;            // Working type (e.g., CONTRACT_PRICE)
    private Boolean priceProtect;          // Price protection flag
    private String priceMatch;             // Price match mode
    private String selfTradePreventionMode; // Self-trading prevention mode
    private BigDecimal commissionAmount;
    private String commissionAsset;

    private String source;                 // Source of the order
    private Long discordUserId;            // The owner of the order.
}