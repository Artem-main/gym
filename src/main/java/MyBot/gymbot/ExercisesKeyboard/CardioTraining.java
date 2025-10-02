package MyBot.gymbot.ExercisesKeyboard;

import MyBot.gymbot.utils.KeyboardUtils;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static MyBot.gymbot.service.BotCommand.*;

public class CardioTraining {

    @SneakyThrows
    public ReplyKeyboard cardioKeyboard (Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Кардио тренировка");

        ReplyKeyboardMarkup replyKeyboard = new KeyboardUtils()
                .createBasicKeyboard();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(CARDIO_TRAINING.getCommand());

        KeyboardRow row1 = new KeyboardRow();
        row1.add(CARDIO_RECOMMENDATIONS.getCommand());

//        // Добавляем кнопку завершения
//        KeyboardRow finishRow = new KeyboardRow();
//        finishRow.add(FINISH_EXERCISE.getCommand());

        keyboardRows.add(row);
        keyboardRows.add(row1);
//        keyboardRows.add(finishRow);

        // Кнопка назад
        KeyboardRow backButton = new KeyboardRow();
        backButton.add(BACK_ON_MENU.getCommand());
        keyboardRows.add(backButton);

        replyKeyboard.setKeyboard(keyboardRows);

        return replyKeyboard;
    }

    public String getPathCardioCategory (String category) {
        return switch (category) {
            case "Кардио тренировка" -> CARDIO_TRAINING.getFilePath();
            case "Общие рекомендации" -> CARDIO_RECOMMENDATIONS.getFilePath();
            default -> null;
        };
    }

}
