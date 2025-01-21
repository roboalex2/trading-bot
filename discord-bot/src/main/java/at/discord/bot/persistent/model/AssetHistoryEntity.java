package at.discord.bot.persistent.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;


import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "asset_history")
@Data // Lombok annotation to generate getters and setters
public class AssetHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "discord_user_id", nullable = false)
    private Long UserId;

    @Column(name = "asset_name", nullable = false)
    private String assetName;

    @Column(name = "asset_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal assetBalance;

    @CreationTimestamp // Automatically set the timestamp when the record is created
    @Column(name = "capture_timestamp", nullable = false, updatable = false)
    private OffsetDateTime captureTimestamp;

    // Lombok @Data generates the setter for captureTimestamp automatically,
    // but you can add it explicitly if needed:
    public void setCaptureTimestamp(OffsetDateTime captureTimestamp) {
        this.captureTimestamp = captureTimestamp;
    }

    // Other necessary methods if any (like constructor, toString, etc.)
}
