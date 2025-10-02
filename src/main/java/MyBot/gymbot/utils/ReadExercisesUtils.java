package MyBot.gymbot.utils;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReadExercisesUtils {
    public List<String> readExercisesFromFile(String filePath) throws IOException {
        List<String> exercises = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Добавляем только непустые строки
                if (!line.trim().isEmpty()) {
                    exercises.add(line);
                }
            }
        }
        return exercises;
    }
}
