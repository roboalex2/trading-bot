// AssetHistoryDTO.java
package at.discord.bot.model.asset;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class AssetHistoryDTO {
    private OffsetDateTime timestamp; // Ensure it's named correctly
    private String assetName;
    private BigDecimal assetBalance;

    // Constructor, getters, setters
    public AssetHistoryDTO(OffsetDateTime timestamp, String assetName, BigDecimal assetBalance) {
        this.timestamp = timestamp;
        this.assetName = assetName;
        this.assetBalance = assetBalance;
    }

    // Getters and Setters
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public BigDecimal getAssetBalance() {
        return assetBalance;
    }

    public void setAssetBalance(BigDecimal assetBalance) {
        this.assetBalance = assetBalance;
    }
}
