package at.discord.bot.service.user;

import at.discord.bot.persistent.BinanceCredentialsRepository;
import at.discord.bot.persistent.model.BinanceCredentialsEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JDA jdaInstance;
    private final BinanceCredentialsRepository binanceCredentialsRepository;

    public List<Long> getActiveUsers() {
        return binanceCredentialsRepository.findAll().stream()
            .map(BinanceCredentialsEntity::getUserId)
            .filter(Objects::nonNull)
            .toList();
    }

    public User getUser(long userId) {
        User user = jdaInstance.getUserById(userId);
        if (user == null) {
            user = jdaInstance.retrieveUserById(userId).complete();
            if (user == null) {
                return null;
            }
        }
        return user;
    }
}
