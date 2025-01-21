package at.discord.bot.service.binance.credential;

import at.discord.bot.model.binance.BinanceCredentials;
import at.discord.bot.persistent.BinanceCredentialsRepository;
import at.discord.bot.persistent.model.BinanceCredentialsEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialsDataAccessService {

    private final BinanceCredentialsRepository binanceCredentialsRepository;
    private final AesEncryptionService aesEncryptionService;

    @Transactional
    public BinanceCredentials getCredentials(long userId) {
        Optional<BinanceCredentialsEntity> byUserId = binanceCredentialsRepository.findByUserId(userId);
        if (byUserId.isEmpty()) {
            return null;
        }

        BinanceCredentialsEntity binanceCredentialsEntity = byUserId.get();

        return BinanceCredentials.builder()
            .discordUserId(userId)
            .apiKey(aesEncryptionService.decrypt(binanceCredentialsEntity.getApiKey(), userId))
            .secretApiKey(aesEncryptionService.decrypt(binanceCredentialsEntity.getApiSecret(), userId))
            .build();
    }
    public List<Long> getAllUserIds() {
        // Alle Benutzer-IDs aus der Tabelle holen
        List<BinanceCredentialsEntity> credentialsEntities = binanceCredentialsRepository.findAll();
        // Extrahiere die User-IDs aus den Entitäten
        return credentialsEntities.stream()
                .map(BinanceCredentialsEntity::getUserId)  // getUserId ist der getter für die ID
                .collect(Collectors.toList());
    }
    @Transactional
    public synchronized void setCredentials(BinanceCredentials binanceCredentials) {
        BinanceCredentialsEntity byUserId = binanceCredentialsRepository.findByUserId(binanceCredentials.getDiscordUserId())
            .orElse(new BinanceCredentialsEntity());
        validateKeyFile(binanceCredentials.getSecretApiKey());

        byUserId.setUserId(binanceCredentials.getDiscordUserId());
        byUserId.setApiKey(aesEncryptionService.encrypt(binanceCredentials.getApiKey(), binanceCredentials.getDiscordUserId()));
        byUserId.setApiSecret(aesEncryptionService.encrypt(binanceCredentials.getSecretApiKey(), binanceCredentials.getDiscordUserId()));
        binanceCredentialsRepository.save(byUserId);
    }

    @Transactional
    public synchronized boolean clearCredentials(long userId) {
        Optional<BinanceCredentialsEntity> byUserId = binanceCredentialsRepository.findByUserId(userId);
        if (byUserId.isEmpty()) {
            return false;
        }
        binanceCredentialsRepository.delete(byUserId.get());
        return true;
    }

    private void validateKeyFile(byte[] secretApiKey) {
        Ed25519PrivateKeyParameters key = null;

        try (PemReader pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(secretApiKey), StandardCharsets.UTF_8))) {
            PemObject pemObject = pemReader.readPemObject();
            byte[] privateKeyBytes = pemObject.getContent();
            key = (Ed25519PrivateKeyParameters) PrivateKeyFactory.createKey(privateKeyBytes);
        } catch (Exception e){
            // Nothing needs be done
        }

        if (key == null || !key.isPrivate()) {
            throw new RuntimeException("The provided keyfile does not contain a valid key.");
        }
    }
}
