package at.discord.bot.service.order;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
public class OrderLimitService {
    public void createLimitOrder(String buyOrSell, String symbol, BigDecimal quantity, BigDecimal price) {
    }
}
