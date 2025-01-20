package at.discord.bot.persistent.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "strategy_deployments")
public class StrategyDeploymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deploymentId", updatable = false, nullable = false)
    private Long deploymentId;

    private Long discordUserId;
    private String strategyName;
    private String deploymentSettings;
    private Boolean active;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
