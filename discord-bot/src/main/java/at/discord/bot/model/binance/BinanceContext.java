package at.discord.bot.model.binance;

import com.binance.connector.client.SpotClient;
import com.binance.connector.client.WebSocketApiClient;
import lombok.*;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BinanceContext {
    private long discordUserId;
    private SpotClient spotClient;
    private WebSocketApiClient webSocketApiClient;
}
