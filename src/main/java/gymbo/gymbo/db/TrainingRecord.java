package gymbo.gymbo.db;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TrainingRecord {
    private Long chatId;
    private String firstName;
    private String muscleGroup;
    private String exerciseName;
    private double repetitionsWeightGain;
    private double weight;

    public TrainingRecord() {
    }

    private LocalDateTime timestamp = LocalDateTime.now();

    public TrainingRecord(Long chatId, String firstName,
                          String muscleGroup, String exerciseName,
                          int repetitionsWeightGain, double weight,
                          LocalDateTime timestamp) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.muscleGroup = muscleGroup;
        this.exerciseName = exerciseName;
        this.repetitionsWeightGain = repetitionsWeightGain;
        this.weight = weight;
        this.timestamp = timestamp;
    }
}
