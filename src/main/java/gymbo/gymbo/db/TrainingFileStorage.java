package gymbo.gymbo.db;

import gymbo.gymbo.UpdateConsumer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TrainingFileStorage {
    private static final String FILENAME = "training_records.csv";
    private List<TrainingRecord> records = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(UpdateConsumer.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    private void loadRecords() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 7) { // Проверяем корректность строки
                    continue;
                }
                TrainingRecord record = new TrainingRecord();
                record.setChatId(Long.parseLong(parts[0]));
                record.setFirstName(parts[1]);
                record.setMuscleGroup(parts[2]);
                record.setExerciseName(parts[3]);
                record.setRepetitionsWeightGain(Double.parseDouble(parts[4]));
                record.setWeight(Double.parseDouble(parts[5]));
                record.setTimestamp(LocalDateTime.parse(parts[6], formatter));
                records.add(record);
            }
        } catch (IOException e) {
            logger.error("Ошибка при загрузке данных", e);
        }
    }

    public void saveRecord(TrainingRecord record) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILENAME, true))) {
            writer.newLine();
            writer.append(quoteIfNeeded(record.getChatId().toString()));
            writer.append(",");
            writer.append(quoteIfNeeded(record.getFirstName()));
            writer.append(",");
            writer.append(quoteIfNeeded(record.getMuscleGroup()));
            writer.append(",");
            writer.append(quoteIfNeeded(record.getExerciseName()));
            writer.append(",");
            writer.append(String.valueOf(record.getRepetitionsWeightGain()));
            writer.append(",");
            writer.append(String.valueOf(record.getWeight()));
            writer.append(",");
            writer.append(record.getTimestamp().format(formatter));
        } catch (IOException e) {
            logger.error("Ошибка при сохранении данных", e);
        }
    }

    private String quoteIfNeeded(String value) {
        return value.contains(",") ? "\"" + value + "\"" : value;
    }
}
