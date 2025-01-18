package at.discord.bot.service.channel;

import at.discord.bot.config.discord.DiscordConfigProperties;
import at.discord.bot.persistent.GlobalSettingsRepository;
import at.discord.bot.persistent.model.GlobalSettingsEntity;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageChannelService {

    private final DiscordConfigProperties discordConfigProperties;
    private final JDA jdaInstance;
    private final GlobalSettingsRepository globalSettingsRepository;

    private Map<String, Long> channelIdPerSetting = new HashMap<>();

    public long getChannel(String settingId) {
        if (channelIdPerSetting.get(settingId) == null) {
            Optional<Long> bySetting = globalSettingsRepository.findBySetting(settingId)
                .map(GlobalSettingsEntity::getValue)
                .map(el -> {
                    try {
                        return Long.parseLong(el);
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                });

            if (bySetting.isEmpty()) {
                channelIdPerSetting.put(settingId, discordConfigProperties.getFallbackChannel());
            } else {
                channelIdPerSetting.put(settingId, bySetting.get());
            }
        }

        return channelIdPerSetting.get(settingId);
    }

    public void setChannel(String settingId, long channelId) {
        if (jdaInstance.getTextChannelById(channelId) == null) {
            throw new RuntimeException("The provided channel is not a Text-Channel.");
        }

        GlobalSettingsEntity globalSettingsEntity = new GlobalSettingsEntity();
        globalSettingsEntity.setSetting(settingId);
        globalSettingsEntity.setValue(Long.toString(channelId));

        globalSettingsRepository.save(globalSettingsEntity);
        channelIdPerSetting.put(settingId, channelId);
    }
}
