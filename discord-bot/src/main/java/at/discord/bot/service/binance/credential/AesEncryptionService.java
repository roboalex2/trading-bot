package at.discord.bot.service.binance.credential;

import at.discord.bot.config.binance.BinanceConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class AesEncryptionService {
    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";
    private static final int AES_KEY_SIZE = 32; // 256 bits

    private final BinanceConfigProperties binanceConfigProperties;
    private SecretKeySpec secretKey;

    public SecretKeySpec getKey() {
        if (secretKey == null) {
            try {
                // Hash the key to ensure proper length
                MessageDigest sha = MessageDigest.getInstance("SHA-256");
                byte[] hashedKey = sha.digest(binanceConfigProperties.getCredentialsEncryptionKey().getBytes(StandardCharsets.UTF_8));
                this.secretKey = new SecretKeySpec(Arrays.copyOf(hashedKey, AES_KEY_SIZE), "AES");
            } catch (Exception e) {
                throw new RuntimeException("Error setting key", e);
            }
        }
        return this.secretKey;
    }

    public byte[] encrypt(byte[] plaintext, long iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(longToBytes(iv));
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), ivSpec);
            return cipher.doFinal(plaintext);
        } catch (Exception e) {
            throw new RuntimeException("Error during encryption", e);
        }
    }

    public byte[] decrypt(byte[] ciphertext, long iv) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(longToBytes(iv));
            cipher.init(Cipher.DECRYPT_MODE, getKey(), ivSpec);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Error during decryption", e);
        }
    }

    private byte[] longToBytes(long value) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(value);
        buffer.putLong(value);
        return buffer.array();
    }

}
