package at.discord.bot.persistent.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "binance_credentials")
public class BinanceCredentialsEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private Long userId;
    @Lob
    private byte[] apiKey;
    @Lob
    private byte[] apiSecret;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public byte[] getApiKey() {
        return apiKey;
    }

    public void setApiKey(byte[] apiKey) {
        this.apiKey = apiKey;
    }

    public byte[] getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(byte[] apiSecret) {
        this.apiSecret = apiSecret;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "BinanceCredentialsEntity{" +
            "userId=" + userId +
            ", apiKey='" + Arrays.toString(apiKey) + '\'' +
            ", apiSecret='" + Arrays.toString(apiSecret) + '\'' +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
