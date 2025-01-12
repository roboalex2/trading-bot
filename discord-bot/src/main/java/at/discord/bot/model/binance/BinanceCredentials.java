package at.discord.bot.model.binance;

import lombok.*;

@Data
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BinanceCredentials {
    private long discordUserId;
    private byte[] apiKey;
    private byte[] secretApiKey;
}
