package gymbo.gymbo;

import gymbo.gymbo.config.BotConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class MyTelegramBot implements SpringLongPollingBot {
    @Value("${telegram.bot.token}")
    private final String botToken;

    private final UpdateConsumer updateConsumer;

    public MyTelegramBot(BotConfig botConfig, UpdateConsumer updateConsumer) {
        this.botToken = botConfig.getToken();
        this.updateConsumer = updateConsumer;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
