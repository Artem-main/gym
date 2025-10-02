package MyBot.gymbot.ExercisesKeyboard;

import MyBot.gymbot.utils.KeyboardUtils;
import MyBot.gymbot.utils.ReadExercisesUtils;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static MyBot.gymbot.service.BotCommand.*;

public class MassExercisesMenuKeyboard {
    private final ReadExercisesUtils readExercisesUtils = new ReadExercisesUtils();
    private final SendMessage sendMessageBack = new SendMessage();

    @SneakyThrows
    public ReplyKeyboardMarkup menuExercisesKeyboard(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        String category = getCategoryFromUpdate(update);

        sendMessageBack.setChatId(chatId);
        sendMessageBack.setText("Выберите упражнение для " + category);

        // Читаем список упражнений из файла
        List<String> exercises = readExercisesUtils.readExercisesFromFile(getPathExercises(category));

        // Создаем объект клавиатуры
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        // Создаем список строк для клавиатуры
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // Для каждой кнопки создаем отдельную строку
        for (String exercise : exercises) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(exercise));
            keyboardRows.add(row);
        }

        // Добавляем кнопку завершения
        KeyboardRow finishRow = new KeyboardRow();
        finishRow.add(FINISH_EXERCISE.getCommand());
        finishRow.add(RESULTS.getCommand());
        keyboardRows.add(finishRow);

        // Кнопка назад
        KeyboardRow backButton = new KeyboardRow();
        backButton.add(BACK_ON_MENU.getCommand());
        keyboardRows.add(backButton);

        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }

    private Map<String, String> chatCategories = new HashMap<>();

    public void setCategory(String chatId, String category) {
        chatCategories.put(chatId, category);
    }

    public String getCategoryFromUpdate(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        return chatCategories.getOrDefault(chatId, null);
    }

    public String getPathExercises(String category) {
        return switch (category) {
            case "Спина"-> BACK_MUSCLES.getFilePath();
            case "Ноги" -> LEGS.getFilePath();
            case "Грудь" -> CHEST.getFilePath();
            default -> null;
        };
    }

    public ReplyKeyboardMarkup showCategoriesKeyboard(Update update) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());

        ReplyKeyboardMarkup keyboard = new KeyboardUtils()
                .createBasicKeyboard();


        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(CHEST.getCommand()));
        row.add(new KeyboardButton(BACK_MUSCLES.getCommand()));
        row.add(new KeyboardButton(LEGS.getCommand()));
        keyboardRows.add(row);

        // Кнопка назад
        KeyboardRow backButton = new KeyboardRow();
        backButton.add(BACK_ON_MENU.getCommand());
        keyboardRows.add(backButton);

        keyboard.setKeyboard(keyboardRows);
        return keyboard;
    }
}

