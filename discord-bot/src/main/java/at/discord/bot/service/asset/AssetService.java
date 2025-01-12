package at.discord.bot.service.asset;

import org.springframework.stereotype.Service;

@Service
public class AssetService {
    public String getUserAssets(String userId) {
        return userId;
    }
}
