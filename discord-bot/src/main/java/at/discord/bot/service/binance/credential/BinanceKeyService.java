package at.discord.bot.service.binance.credential;

import org.springframework.stereotype.Service;

@Service
public class BinanceKeyService {
    public boolean validateApiKey(String apiKey) {
        return false;
    }

    public boolean setApiKey(String id, String apiKey) {
        return false;
    }

    public boolean clearApiKey(String id) {
        return false;
    }
}
