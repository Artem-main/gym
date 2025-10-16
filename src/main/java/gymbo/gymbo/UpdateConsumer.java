package gymbo.gymbo;

import gymbo.gymbo.config.BotConfig;
import gymbo.gymbo.db.TrainingFileStorage;
import gymbo.gymbo.db.TrainingList;
import gymbo.gymbo.db.TrainingRecord;
import lombok.SneakyThrows;
import model.ButtonName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static model.ButtonName.*;

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

    @Autowired
    private TrainingFileStorage storage;
    @Autowired
    private TrainingList trainingList;
    // Хранилище контекста для всех пользователей
    private final Map<Long, String> exerciseContext = new ConcurrentHashMap<>();


    @SneakyThrows
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String user = update.getMessage().getFrom().getFirstName();

            // Проверяем, ожидает ли пользователь ввода веса
            if (exerciseContext.containsKey(chatId)) {
                try {
                    Double weight = Double.parseDouble(text);
                    String exercise = exerciseContext.remove(chatId);
                    saveTrainingResult(chatId, user, exercise, weight);
                    sendMessage(chatId, "Результат сохранен!");

                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Пожалуйста, введите числовое значение веса");
                }
                return;
            }

            if (text.equals("/start")) {
                sendMainMenu(chatId);
            } else {
                sendMessage(chatId, "Неизвестная команда");
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    @SneakyThrows
    private void saveTrainingResult(Long chatId, String user, String exercise, Double weight) {
        String muscleGroup = getMuscleGroupByExercise(exercise);

        try {
            TrainingRecord record = new TrainingRecord();
            record.setChatId(chatId);
            record.setFirstName(user);
            record.setMuscleGroup(muscleGroup);
            record.setExerciseName(exercise);
            record.setWeight(weight);
            record.setTimestamp(LocalDateTime.now());

            storage.saveRecord(record);

            sendMessage(chatId, "Результат сохранен!");
        } catch (Exception e) {
            logger.error("Ошибка при сохранении результата", e);
            sendMessage(chatId, "Произошла ошибка при сохранении результата");
        }
    }

    private String getMuscleGroupByExercise(String exercise) {
        if (exercise.contains("Жим") || exercise.contains("Разводка")) {
            return "Грудь";
        } else if (exercise.contains("Тяга") || exercise.contains("Подтягивание")) {
            return "Спина";
        } else if (exercise.contains("Приседания") || exercise.contains("Выпады")) {
            return "Ноги";
        }
        return "Неизвестно";
    }

    @SneakyThrows
    private void listAllExercises(Long chatId) {
        List<String> allExercises = new ArrayList<>();

        try {
            allExercises.addAll(readExercisesFromFile("Грудь"));
            allExercises.addAll(readExercisesFromFile("Спина"));
            allExercises.addAll(readExercisesFromFile("Ноги"));

            StringBuilder message = new StringBuilder("Список всех упражнений:\n\n");

            for (String exercise : allExercises) {
                message.append("- ").append(exercise).append("\n");
            }

            sendMessage(chatId, message.toString());
        } catch (IOException e) {
            logger.error("Ошибка при чтении упражнений", e);
            sendMessage(chatId, "Произошла ошибка при получении списка упражнений");
        }
    }

    @SneakyThrows
    private void handleCallbackQuery(CallbackQuery callbackQuery) throws TelegramApiException {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();

        // Добавляем логирование полной структуры callback
        logger.info("Получен callback: {}", data);

        if (data.startsWith("exercise_")) {
            String[] parts = data.split("_", 3); // Разделяем на 3 части

            if (parts.length != 3) {
                sendMessage(chatId, "Некорректные данные callback");
                return;
            }

            String muscleGroup = parts[1];
            String safeExercise = parts[2];
            String fileName = muscleGroup.toLowerCase().replace(" ", "") + "_weightgain.csv";

            try (BufferedReader reader = new BufferedReader(
                    new FileReader(fileName))) {
                String line;
                boolean found = false;

                while ((line = reader.readLine()) != null) {
                    String[] fileParts = line.split(",");
                    if (fileParts.length == 3) {
                        String originalExercise = fileParts[1].trim() + " (" + fileParts[2].trim() + ")";
                        String currentSafeExercise = originalExercise
                                .replaceAll("[() ]", "_")
                                .replaceAll("[^a-zA-Z0-9_]", "");

                        if (currentSafeExercise.equals(safeExercise)) {
                            exerciseContext.put(chatId, originalExercise);
                            sendMessage(chatId, "Введите вес, который вы подняли (в кг):");
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    sendMessage(chatId, "Упражнение не найдено в базе данных");
                }
            } catch (IOException e) {
                logger.error("Ошибка чтения файла: {}", fileName, e);
                sendMessage(chatId, "Ошибка при обработке упражнения");
            }
        } else {
            // Обработка других callback
            switch (data) {
                case "Набор массы":
                    groupMuscule(chatId);
                    break;
                case "Назад":
                    sendMenuButton2(chatId);
                    break;
                case "Спина":
                    backWeightGainExercises(chatId, callbackQuery.getFrom().getFirstName(), data);
                    break;
                case "Ноги":
                    legsWeightGainExercises(chatId, callbackQuery.getFrom().getFirstName());
                    break;
                case "Грудь":
                    chestWeightGainExercises(chatId);
                    break;
                case "Список упражнений":
                    listAllExercises(chatId);
                    break;
                default:
                    sendMessage(chatId, "Неизвестная команда");
                    break;
            }
        }
    }

    @SneakyThrows
    private void showExercises(Long chatId, String muscleGroup) throws IOException {
        List<String> exercises = readExercisesFromFile(muscleGroup);
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();

        for (String exercise : exercises) {
            String safeExercise = exercise
                    .replaceAll("[() ]", "_")
                    .replaceAll("[^a-zA-Z0-9_]", "");

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(exercise)
                    .callbackData("exercise_" + muscleGroup + "_" + safeExercise)
                    .build();

            keyboardRows.add(new InlineKeyboardRow(button));
        }

        String message = "Упражнения на " + muscleGroup.toLowerCase() + " для набора массы:";
        sendMessageWithKeyboard(chatId, message, keyboardRows);
    }

    private List<String> readExercisesFromFile(String muscleGroup) throws IOException {
        String fileName = muscleGroup.toLowerCase().replace(" ", "") + "_weightgain.csv";
        List<String> exercises = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1); // Сохраняем пустые поля
                if (parts.length >= 3) {
                    String exercise = parts[1].trim() + " (" + parts[2].trim() + ")";
                    exercises.add(exercise);
                }
            }
        } catch (NoSuchFileException e) {
            logger.error("Файл не найден: {}", fileName);
            throw new FileNotFoundException("Файл с упражнениями не найден");
        }

        return exercises;
    }


    @SneakyThrows
    private void chestWeightGainExercises(Long chatId) throws IOException {
        showExercises(chatId, "chest");
    }

    @SneakyThrows
    private void backWeightGainExercises(Long chatId, String user, String data) throws IOException {
        showExercises(chatId, "back");
    }

    @SneakyThrows
    private void legsWeightGainExercises(Long chatId, String user) throws IOException {
        showExercises(chatId, "legs");
    }

    private List<String> readChestExercisesFromFile() throws IOException {
        List<String> exercises = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new FileReader("chest_weightgain.csv"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String exercise = parts[1].trim() + " (" + parts[2].trim() + ")";
                    exercises.add(exercise);
                }
            }
        }
        return exercises;
    }

    private void sendMessageWithKeyboard(Long chatId, String messageText, List<InlineKeyboardRow> keyboardRows) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        sendMessage.setReplyMarkup(markup);

        telegramClient.execute(sendMessage);
    }

    @SneakyThrows
    private void sendMenuButton2(Long chatId) {
        sendMessage(chatId, "You click 2 button");
    }

    @SneakyThrows
    private void groupMuscule(Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .text("Выберите группу мышц")
                .chatId(chatId)
                .build();
        var buttonBack = InlineKeyboardButton.builder()
                .text(BACK.getName())
                .callbackData(ButtonName.BACK.getName())
                .build();
        var buttonLegs = InlineKeyboardButton.builder()
                .text(LEGS.getName())
                .callbackData(ButtonName.LEGS.getName())
                .build();
        var buttonChest = InlineKeyboardButton.builder()
                .text(CHEST.getName())
                .callbackData(ButtonName.CHEST.getName())
                .build();
        var buttonBackInMenu = InlineKeyboardButton.builder()
                .text(BACKBUTTON.getName())
                .callbackData(ButtonName.BACKBUTTON.getName())
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(buttonBack),
                new InlineKeyboardRow(buttonLegs),
                new InlineKeyboardRow(buttonChest),
                new InlineKeyboardRow(buttonBackInMenu)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        sendMessage.setReplyMarkup(markup);
        telegramClient.execute(sendMessage);
    }

    private void sendMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .parseMode("Markdown")
                .build();
        telegramClient.execute(sendMessage);
    }

    private void sendMainMenu(Long chatId) throws TelegramApiException {

        SendMessage sendMessage = SendMessage.builder()
                .text("Выберите направление")
                .chatId(chatId)
                .build();

        var mainMenuButton = InlineKeyboardButton.builder()
                .text(WEIGHTGAIN.getName())
                .callbackData(WEIGHTGAIN.getName())
                .build();

        var mainMenuButton2 = InlineKeyboardButton.builder()
                .text("Список упражнений")
                .callbackData("Список упражнений")
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
