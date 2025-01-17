package at.discord.bot.model.asset;

import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserAsset {
    private long discordUserId;
    private String asset;
    private BigDecimal free;
    private BigDecimal locked;
    private BigDecimal frozen;
    private BigDecimal withdrawing;
    private BigDecimal ipoable;
    private BigDecimal btcValuation;
    private BigDecimal usdcValuation;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(asset);

        BigDecimal total = new BigDecimal(0);
        if (free != null) {
            total = total.add(free);
        }
        if (locked != null) {
            total = total.add(locked);
        }
        if (frozen != null) {
            total = total.add(frozen);
        }
        if (withdrawing != null) {
            total = total.add(withdrawing);
        }
        sb.append(" total=").append(total);

        if (free != null && free.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(" free=").append(free);
        }
        if (locked != null && locked.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(" locked=").append(locked);
        }
        if (frozen != null && frozen.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(" frozen=").append(frozen);
        }
        if (withdrawing != null && withdrawing.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(" withdrawing=").append(withdrawing);
        }

        if (usdcValuation != null && usdcValuation.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(" usdcValuation=").append(usdcValuation.setScale(2, RoundingMode.HALF_UP));
        }

        return sb.toString();
    }
}
