package MyBot.gymbot.service;

import java.util.Arrays;

public enum BotCommand {
    START("/start", "src/main/resources/trainingFiles/MainMenu.txt"),
    MAIN_MENU("/main", "filePath"),
    WEIGHT_GAIN("Набор массы", "src/main/resources/trainingFiles/weightGainRecommendation.txt"),
    STRENGHT("Cиловые тренировки",""),
    NUTRITION("Питание", ""),
    WEIGHT_LOSS("Похудение",""),
    FAT_BURNING("Жиросжигание",""),
    FUNCTIONAL("Фукциональные",""),
    BACK_ON_MENU("Назад", ""),
    WARM_UP("Разминка",""),
    CLEAR("/clear", "filePath"),
    FINISH_EXERCISE("Закончить упражнение", "filePath"),
    BACK_MUSCLES("Спина", "src/main/resources/trainingFiles/back.txt"),
    CHEST("Грудь", "src/main/resources/trainingFiles/chest.txt"),
    LEGS("Ноги", "src/main/resources/trainingFiles/legs.txt"),
    CARDIO("Кардио",""),
    CARDIO_TRAINING("Кардио тренировка", "src/main/resources/trainingFiles/cardioTraining.txt"),
    CARDIO_RECOMMENDATIONS("Общие рекомендации", "src/main/resources/trainingFiles/recommendationsCardioTraining.txt"),
    RESULTS("Результат", "filePath");

    private final String command;
    private final String filePath;

    BotCommand(String command, String filePath) {
        this.command = command;
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCommand() {
        return command;
    }

    // Метод для поиска команды по тексту
    public static BotCommand fromCommand(String command) {
        return Arrays.stream(values())
                .filter(c -> c.getCommand().equals(command))
                .findFirst()
                .orElse(null);
    }
}
