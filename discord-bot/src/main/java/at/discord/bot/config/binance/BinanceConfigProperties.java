package at.discord.bot.config.binance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "binance")
@Data
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class BinanceConfigProperties {
    @NotNull
    private String credentialsEncryptionKey;
    @NotNull
    private String baseUrl;
    @NotNull
    private String baseWebsocket;
    @NotNull
    private String baseWebsocketApi;
}
