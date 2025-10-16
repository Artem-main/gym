package gymbo.gymbo.db;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainingList {
    private static final String FILENAME = "training_list.csv";
    private List<TrainingRecord> recordArrayList = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(TrainingList.class);

    // Метод загрузки данных из файла
    @PostConstruct
    public void loadData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    TrainingRecord record = new TrainingRecord();
                    record.setMuscleGroup(parts[0].trim());
                    record.setExerciseName(parts[1].trim());
                    // Остальные поля можно оставить пустыми или задать значения по умолчанию
                    recordArrayList.add(record);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при чтении файла", e);
        }
    }

    // Получение списка упражнений по группе мышц
    public List<TrainingRecord> getTrainingListByMuscleGroup(String muscleGroup) {
        return recordArrayList.stream()
                .filter(r -> r.getMuscleGroup().equalsIgnoreCase(muscleGroup))
                .collect(Collectors.toList());
    }

    // Получение полного списка всех упражнений
    public List<TrainingRecord> getAllTrainingList() {
        return new ArrayList<>(recordArrayList);
    }

    // Получение списка всех уникальных групп мышц
    public List<String> getAllMuscleGroups() {
        return recordArrayList.stream()
                .map(TrainingRecord::getMuscleGroup)
                .distinct()
                .collect(Collectors.toList());
    }
}
