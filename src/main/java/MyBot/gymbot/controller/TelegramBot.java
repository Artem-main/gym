package MyBot.gymbot.controller;

import MyBot.gymbot.Data.DatabaseHelper;
import MyBot.gymbot.ExercisesKeyboard.CardioTraining;
import MyBot.gymbot.config.properties.BotProperties;
import MyBot.gymbot.ExercisesKeyboard.MassExercisesMenuKeyboard;
import MyBot.gymbot.ExercisesKeyboard.MainMenu;
import MyBot.gymbot.service.*;
import MyBot.gymbot.utils.ReadExercisesUtils;
import MyBot.gymbot.utils.ReadFilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static MyBot.gymbot.service.BotCommand.*;


@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private final String botToken;
    private final String botUsername;
    private final MainMenu mainMenu = new MainMenu();
    private final MassExercisesMenuKeyboard massExercisesMenuKeyboard = new MassExercisesMenuKeyboard();
    private final WeightInputService weightInputService = new WeightInputService();

    // Хранилище для сохранения текущей категории для каждого чата
    private final Map<String, String> chatCategories = new HashMap<>();
    private final ReadExercisesUtils readExercisesUtils = new ReadExercisesUtils();
    private final ReplyKeyboardRemove removeKeyboard = new ReplyKeyboardRemove();
    private final CardioTraining cardioTraining = new CardioTraining();
    private final ReadFilesUtils readFilesUtils = new ReadFilesUtils();
    private final DatabaseHelper dbHelper = new DatabaseHelper();
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);


    @Autowired
    public TelegramBot(BotProperties botProperties) {
        this.botToken = botProperties.token();
        this.botUsername = botProperties.name();

        // Проверка на пустые значения
        if (botToken.isBlank() || botUsername.isBlank()) {
            throw new IllegalArgumentException("Bot token and username must be specified");
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();

            SendMessage sendMessage = new SendMessage();
            BotCommand command = fromCommand(text);

            // Сначала проверяем есть ID в базе
            if (dbHelper.getUser(chatId) == null) {
                // Сохраняем или обновляем информацию о пользователе
                dbHelper.addUser(chatId, username);
                sendMessage(chatId, "Привет, " + (username != null ? "@" + username : "пользователь") + "!\n" +
                        "Ваш ID сохранен в базе данных.");
            }

            if (command == null) {
                // Если команда не распознана, обрабатываем как выбор упражнения
                handleExerciseSelection(update, sendMessage, text, chatId);
                return;
            }
//        STRENGHT
//        WEIGHT_LOSS
//        NUTRITION
//        FUNCTIONAL
//        FAT_BURNING
//        WARM_UP

            switch (command) {
                case START, BACK_ON_MENU -> {
                    getStartedMenu(update,sendMessage,chatId);
                }
                case WEIGHT_GAIN -> {
                    sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText(readFilesUtils.readTextFromFile(
                            WEIGHT_GAIN.getFilePath()
                    ));

                    sendMessage.setReplyMarkup(massExercisesMenuKeyboard
                            .showCategoriesKeyboard(update));
                    execute(sendMessage);
                }
                case FINISH_EXERCISE -> {
                    getFinishExercises(update,sendMessage,chatId);
                }
                case LEGS, CHEST, BACK_MUSCLES -> {
                    handleExerciseCategory(update, sendMessage, text);
                }
                case RESULTS -> {
                    String category = massExercisesMenuKeyboard.getCategoryFromUpdate(update);
                    getResultsExercises(sendMessage, chatId, category);
                }
                case CARDIO -> {
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Выберите меню");
                    sendMessage.setReplyMarkup(cardioTraining.cardioKeyboard(update));
                    execute(sendMessage);
                }
                case CARDIO_TRAINING -> {
                    sendMessage.setChatId(chatId);
                    String exe = readFilesUtils.readTextFromFile(
                            cardioTraining.getPathCardioCategory(CARDIO_TRAINING.getCommand()));
                    sendMessage.setText(exe);
                    execute(sendMessage);

                }
                case CARDIO_RECOMMENDATIONS -> {
                    sendMessage.setChatId(chatId);
                    String exercises = readFilesUtils.readTextFromFile(
                            cardioTraining.getPathCardioCategory(CARDIO_RECOMMENDATIONS.getCommand()));
                    sendMessage.setText(exercises);
                    execute(sendMessage);
                }
                default -> {
                    handleExerciseSelection(update, sendMessage, text, chatId);
                }
            }
        }
    }

    @SneakyThrows
    private void getStartedMenu (Update update, SendMessage sendMessage, Long chatId) {
//        final String readMainMenuText = readFilesUtils.readTextFromFile("Выберите направление тренировок");
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберите направление тренировок");
        sendMessage.setReplyMarkup(mainMenu.createReplyKeyboard(update));
        execute(sendMessage);
    }

    @SneakyThrows
    private void getFinishExercises (Update update, SendMessage sendMessage, Long chatId) {
        sendMessage.setChatId(chatId);
        sendMessage.setText("Вы закончили тренировку");
        // создаем объект удаления клавиатуры
        removeKeyboard.setRemoveKeyboard(true);
        sendMessage.setReplyMarkup(removeKeyboard);
        sendMessage.setReplyMarkup(mainMenu.createReplyKeyboard(update));
        // Показываем главное меню после завершения упражнения
        execute(sendMessage);
        chatCategories.remove(chatId); // Очищаем категорию
    }

    // Получаем информацию о тренировках, но не сохраняем ее

//    @SneakyThrows
//    private void getResultsExercises(Update update, SendMessage sendMessage, Long chatId) {
//        sendMessage.setChatId(chatId);
//
//        // Получаем результаты из базы данных
//        Map<String, Integer> results = dbHelper.getUserResults(chatId);
//
//        if (results.isEmpty()) {
//            sendMessage.setText("Результаты пока отсутствуют");
//        } else {
//            StringBuilder resultMessage = new StringBuilder("Ваши результаты:\n");
//
//            for (Map.Entry<String, Integer> entry : results.entrySet()) {
//                resultMessage.append(entry.getKey())
//                        .append(" - ")
//                        .append(entry.getValue())
//                        .append(" кг\n");
//            }
//
//            sendMessage.setText(resultMessage.toString());
//        }
//
//        execute(sendMessage);
//    }

    @SneakyThrows
    private void getResultsExercises(SendMessage sendMessage, Long chatId, String category) {
        sendMessage.setChatId(chatId);
        String muscleGroup = "Группа мышц: ";
        // Получаем результаты по категории
        Map<String, Integer> results = dbHelper.getUserResultsByCategory(chatId, category);

        if (results.isEmpty()) {
            sendMessage.setText(muscleGroup + category + " пока отсутствуют");
        } else {
            StringBuilder resultMessage = new StringBuilder(muscleGroup + category + ":\n");

            for (Map.Entry<String, Integer> entry : results.entrySet()) {
                resultMessage.append(entry.getKey())
                        .append(" - ")
                        .append(entry.getValue())
                        .append(" кг\n");
            }

            sendMessage.setText(resultMessage.toString());
        }

        execute(sendMessage);
    }


    @SneakyThrows
    private void handleExerciseCategory(Update update, SendMessage sendMessage, String text) {
        String chatId = update.getMessage().getChatId().toString();

        // Сохраняем выбранную категорию
        massExercisesMenuKeyboard.setCategory(chatId, text);

        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберите упражнение");
        sendMessage.setReplyMarkup(massExercisesMenuKeyboard.menuExercisesKeyboard(update));
        execute(sendMessage);
    }

    @SneakyThrows
    private void handleExerciseSelection(Update update, SendMessage sendMessage, String text, Long chatId) {
        if (chatId == null) {
            log.error("chatId is null in handleExerciseSelection");
            return;
        }
        String category = massExercisesMenuKeyboard.getCategoryFromUpdate(update);

        if (category == null) {
            sendMessage.setText("Неверная категория упражнений");
            execute(sendMessage);
            return;
        }

        List<String> exercises = readExercisesUtils.readExercisesFromFile(
                massExercisesMenuKeyboard.getPathExercises(category)
        );

        if (exercises.contains(text)) {
            weightInputService.setCurrentExercise(chatId, text);
            weightInputService.setCurrentCategory(chatId, category);

            weightInputService.startWaitingForNumber(chatId, text);
            sendMessage.setChatId(chatId);
            sendMessage.setText("Введите вес для упражнения: \n" + text);
            execute(sendMessage);
            return;
        }

        if (weightInputService.isWaitingForNumber(chatId) && !update.getMessage().getText().equals(FINISH_EXERCISE.getCommand())) {
            try {
                int weight = Integer.parseInt(text);
                String exerciseName = weightInputService.getCurrentExercise(chatId);
                String currentCategory = weightInputService.getCurrentCategory(chatId);

                dbHelper.saveExerciseResult(chatId, currentCategory, exerciseName, weight);

                sendMessage.setChatId(chatId);
                sendMessage.setText("Результат сохранен: \n" + exerciseName + " - " + weight + " кг");
                execute(sendMessage);

                weightInputService.resetState(chatId);
            } catch (NumberFormatException e) {
                sendMessage.setChatId(chatId);
                sendMessage.setText("Пожалуйста, введите числовое значение веса");
                execute(sendMessage);
            }
        }
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        execute(sendMessage);
    }

}
