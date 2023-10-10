import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;

public class FileMeta {
    enum Type {
        WEBP("webp"),
        PNG("png");

        Type(String extension) {
            this.extension = extension;
        }

        private String extension;

        public String getExtension() {
            return extension;
        }
    }
    private String camelCaseName;
    private String fileName;
    private String fileNameWithoutExtension;
    private String filePath;
    private String fileExtension;
    private Type type;
    private String group;

    public FileMeta() {
    }

    public static void jsonFilePathConvert(String filePath) {
        // Создайте объект Gson
        Gson gson = new Gson();

// Прочитайте JSON-файл в объект JsonObject
        try (Reader reader = new FileReader(filePath)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            // Проверьте наличие поля в JsonObject
            if (jsonObject.has("imagePath")) {
                // Получите текущее значение поля
                String currentValue = jsonObject.get("imagePath").getAsString();

                currentValue = currentValue.substring(0, currentValue.lastIndexOf('.'));
                // Измените значение поля
                jsonObject.addProperty("imagePath", currentValue + ".webp");

                // Сохраните изменения обратно в JSON-файл
                try (Writer writer = new FileWriter(filePath)) {
                    gson.toJson(jsonObject, writer);
                }
            } else {
                System.out.println("Поле не найдено в JSON.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCamelCaseName() {
        return camelCaseName;
    }

    public void setCamelCaseName(String camelCaseName) {
        this.camelCaseName = camelCaseName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameWithoutExtension() {
        return fileNameWithoutExtension;
    }

    public void setFileNameWithoutExtension(String fileNameWithoutExtension) {
        this.fileNameWithoutExtension = fileNameWithoutExtension;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        filePath = "./" + fileName;
        this.filePath = filePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
