package MyBot.gymbot.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public class KeyboardUtils {
    private ReplyKeyboardMarkup keyboard;
    public ReplyKeyboardMarkup createBasicKeyboard() {
        keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true); // Автоматическое изменение размера
        keyboard.setOneTimeKeyboard(false); // Скрывать клавиатуру после нажатия
        keyboard.setSelective(false);
        return keyboard;
    }

    public KeyboardUtils setOneTimeKeyboard (boolean active) {
        keyboard.setOneTimeKeyboard(active);
        return this;
    }


}
