package at.discord.bot.config.binance;

import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BinanceConfig {

    private final BinanceConfigProperties binanceConfigProperties;

    @Bean
    public WebSocketStreamClient getBinanceWebSocketStreamClient() {
        return new WebSocketStreamClientImpl(binanceConfigProperties.getBaseWebsocket());
    }
}
