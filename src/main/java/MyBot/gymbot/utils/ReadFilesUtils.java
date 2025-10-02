package MyBot.gymbot.utils;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.FileReader;

public class ReadFilesUtils {
    @SneakyThrows
    public String readTextFromFile(String filePath) {
        StringBuilder messageText = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean inBlock = false; // Флаг для отслеживания блока текста
            StringBuilder currentBlock = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();

                if (trimmedLine.isEmpty()) {
                    // Если строка пустая и мы находимся в блоке, добавляем текущий блок
                    if (inBlock) {
                        // Добавляем блок в общий текст с переносом строки
                        messageText.append(currentBlock.toString().trim()).append("\n\n");
                        currentBlock = new StringBuilder();
                        inBlock = false;
                    }
                    continue;
                }

                // Добавляем строку к текущему блоку
                currentBlock.append(trimmedLine).append("\n");
                inBlock = true;
            }

            // Добавляем последний блок, если он есть
            if (inBlock) {
                messageText.append(currentBlock.toString().trim());
            }
        }

        return messageText.toString().trim();
    }

}
