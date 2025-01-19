package at.discord.bot.service.binance.credential;

import at.discord.bot.config.binance.BinanceConfigProperties;
import at.discord.bot.model.binance.BinanceContext;
import at.discord.bot.model.binance.BinanceCredentials;
import com.binance.connector.client.impl.SpotClientImpl;
import com.binance.connector.client.impl.WebSocketApiClientImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceContextProviderService {

    private final CredentialsDataAccessService credentialsDataAccessService;
    private final BinanceConfigProperties binanceConfigProperties;

    // TODO do not allways access database
    public BinanceContext getUserContext(long userId) {
        BinanceCredentials credentials = credentialsDataAccessService.getCredentials(userId);
        if (credentials == null) {
            return null;
        }
        try {
            BinanceSignatureGenerator binanceSignatureGenerator = new BinanceSignatureGenerator(credentials.getSecretApiKey());

            return BinanceContext.builder()
                .discordUserId(userId)
                .spotClient(new SpotClientImpl(
                    new String(credentials.getApiKey(), StandardCharsets.UTF_8),
                    binanceSignatureGenerator,
                    binanceConfigProperties.getBaseUrl())
                )
                .webSocketApiClient(new WebSocketApiClientImpl(
                    new String(credentials.getApiKey(), StandardCharsets.UTF_8),
                    binanceSignatureGenerator,
                    binanceConfigProperties.getBaseWebsocketApi())
                )
                .build();
        } catch (IOException exception) {
            log.warn("Could not create binance context based on provided credentials.", exception);
        }
        return null;
    }
}
