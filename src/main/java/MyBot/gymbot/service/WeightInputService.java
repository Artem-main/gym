package MyBot.gymbot.service;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class WeightInputService {
    private final Map<Long, String> waitingForNumbers = new HashMap<>();
    private final String resultsFilePath = "trainingFiles/results.txt";
    // Map для хранения результатов
    private final Map<Long, Map<String, Integer>> exerciseResults = new HashMap<>();

    // Активация режима ожидания числа
    public void startWaitingForNumber(Long chatId, String exerciseName) {
        waitingForNumbers.put(chatId, exerciseName);
    }

    // Проверка на ожидание числа
    public boolean isWaitingForNumber(Long chatId) {
        return waitingForNumbers.containsKey(chatId);
    }

    // Получение названия упражнения
    public String getCurrentExercise(Long chatId) {
        return waitingForNumbers.getOrDefault(chatId, "");
    }

    // Обработка введенного числа
    @SneakyThrows
    public void processNumberInput(Long chatId, String text, Executor executor) {
        try {
            int number = Integer.parseInt(text);
            String exerciseName = getCurrentExercise(chatId);
            waitingForNumbers.remove(chatId);

            // Сохраняем результат в Map
            saveResult(chatId, exerciseName, number);

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText(exerciseName + ": " + number + " кг");


            try {
                executor.execute(sendMessage);
            } catch (TelegramApiException e) {
                handleSendError(chatId, executor);
            }
        } catch (NumberFormatException e) {
            handleInvalidInput(chatId, executor);
        }
    }
    private  Map<Long, String> currentExerciseMap = new HashMap<>();
    private  Map<Long, String> currentCategoryMap = new HashMap<>();


    // Метод сохранения результата в Map
    private void saveResult(Long chatId, String exerciseName, int weight) {
        // Получаем или создаем Map для конкретного чата
        Map<String, Integer> chatResults = exerciseResults.computeIfAbsent(chatId, k -> new HashMap<>());

        // Сохраняем результат
        chatResults.put(exerciseName, weight);
    }

    // Получение всех результатов для чата
    public Map<String, Integer> getResultsForChat(Long chatId) {
        return exerciseResults.getOrDefault(chatId, new HashMap<>());
    }

    // Метод для записи результата в файл
    private boolean saveResultToFile(String exercise, int weight) {
        File file = new File(resultsFilePath);
        File dir = file.getParentFile();

        // Проверяем и создаем директорию, если она null или не существует
        if (dir == null || !dir.exists()) {
            try {
                Files.createDirectories(dir.toPath());
                System.out.println("Создана директория: " + dir.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Ошибка создания директории: " + e.getMessage());
                return false;
            }
        }

        try {
            // Проверяем права доступа
            if (!file.canWrite()) {
                System.err.println("Нет прав на запись в файл: " + file.getAbsolutePath());
                return false;
            }

            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(file, true))) {
                String content = exercise + " - " + weight + " кг\n";
                System.out.println("Пытаемся записать: " + content); // Логируем записываемое содержимое
                writer.append(content);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи в файл: " + e.getMessage());
            return false;
        }
    }

    // Обработка ошибки записи в файл
    private void handleFileWriteError(String chatId, Executor executor) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Ошибка при сохранении результата в файл");

        try {
            executor.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Критическая ошибка при отправке сообщения: " + e.getMessage());
        }
    }

    // Обработка ошибки отправки
    private void handleSendError(Long chatId, Executor executor) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Ошибка при отправке результата");

        try {
            executor.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Критическая ошибка при отправке сообщения: " + e.getMessage());
        }
    }

    // Обработка неверного ввода
    private void handleInvalidInput(Long chatId, Executor executor) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Ошибка! Введите числовое значение веса");

        try {
            executor.execute(sendMessage);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка при отправке сообщения об ошибке: " + e.getMessage());
        }
    }

    // Интерфейс для выполнения отправки сообщений
    public interface Executor {
        void execute(SendMessage message) throws TelegramApiException;
    }

    public void setCurrentExercise(Long chatId, String exercise) {
        currentExerciseMap.put(chatId, exercise);
    }

    public void setCurrentCategory(Long chatId, String category) {
        currentCategoryMap.put(chatId, category);
    }

    public String getCurrentCategory(Long chatId) {
        return currentCategoryMap.getOrDefault(chatId, "");
    }

    public void resetState(Long chatId) {
        currentExerciseMap.remove(chatId);
        currentCategoryMap.remove(chatId);
    }
}
