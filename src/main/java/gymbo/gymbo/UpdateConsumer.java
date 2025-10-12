package gymbo.gymbo;

import gymbo.gymbo.config.BotConfig;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    private final Logger logger = LoggerFactory.getLogger(UpdateConsumer.class);
    private final TelegramClient telegramClient;

    public UpdateConsumer(BotConfig botConfig) {
        logger.info("Initializing UpdateConsumer with token: {}", botConfig.getToken());

        if (botConfig.getToken() == null || botConfig.getToken().isEmpty()) {
            throw new IllegalArgumentException("Bot token cannot be null or empty");
        }
        this.telegramClient = new OkHttpTelegramClient(botConfig.getToken());
    }

    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (text.equals("/start")) {
                sendMainMenu(chatId);
            } else {
                sendMessage (chatId, "Unknown command");
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
//        var user = callbackQuery.getFrom();
        switch (data) {
            case "button1" -> sendMenuButton1(chatId);
            case "button2" -> sendMenuButton2(chatId);
        }
    }

    @SneakyThrows
    private void sendMenuButton2(Long chatId) {
        sendMessage(chatId, "You click 2 button");
    }

    @SneakyThrows
    private void sendMenuButton1(Long chatId) {
        sendMessage(chatId, "You click 1 button");
    }

    private void sendMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build();
        telegramClient.execute(sendMessage);
    }

    private void sendMainMenu(Long chatId) throws TelegramApiException {

        SendMessage sendMessage = SendMessage.builder()
                .text("This is first menu")
                .chatId(chatId)
                .build();

        var mainMenuButton = InlineKeyboardButton.builder()
                .text("First button")
                .callbackData("button1")
                .build();

        var mainMenuButton2 = InlineKeyboardButton.builder()
                .text("Second button")
                .callbackData("button2")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(mainMenuButton),
                new InlineKeyboardRow(mainMenuButton2)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        sendMessage.setReplyMarkup(markup);
        telegramClient.execute(sendMessage);
    }
}
