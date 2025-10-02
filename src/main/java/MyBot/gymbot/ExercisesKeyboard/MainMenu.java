package MyBot.gymbot.ExercisesKeyboard;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static MyBot.gymbot.service.BotCommand.*;

public class MainMenu {
    public ReplyKeyboard createReplyKeyboard (Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId());

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true); // Автоматическое изменение размера
        keyboard.setOneTimeKeyboard(false); // Скрывать клавиатуру после нажатия
        keyboard.setSelective(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Первая строка
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(WEIGHT_GAIN.getCommand()));
        row1.add(new KeyboardButton(STRENGHT.getCommand()));

        // Вторая строка
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(CARDIO.getCommand()));
        row2.add(new KeyboardButton(WEIGHT_LOSS.getCommand()));
        // Третья строка
        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(NUTRITION.getCommand()));
        row3.add(new KeyboardButton(FUNCTIONAL.getCommand()));

        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton(FAT_BURNING.getCommand()));
        row4.add(new KeyboardButton(WARM_UP.getCommand()));

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardRows.add(row4);

        keyboard.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboard);

        // Создаем саму клавиатуру
        return keyboard;
    }
}
