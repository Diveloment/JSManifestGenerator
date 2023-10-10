import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import com.google.gson.Gson;

public class AutoGroupManifestGenerator {
    static List<String> groups = new ArrayList<>();
    static List<String> keys = List.of("ske", "img", "tex");

    public static void main(String[] args) throws IOException {
        // Папка, в которой находятся медиа-ресурсы
        String mediaDirectory = "/home/Изображения/media";

        Converter.convert(mediaDirectory, mediaDirectory);

        // Получаем список файлов в папке
        File[] mediaFiles = getMediaFiles(mediaDirectory);
        List<FileMeta> filesMeta = new ArrayList<>();

        // Путь к JavaScript-файлу для записи манифестов
        String jsFilePath = "/home/Изображения/mediaManifest.js";

        for (File file : mediaFiles) {
            String fileName = file.getName();
            String varName = fileName.split("\\.")[0]; // Имя переменной импорта без расширения
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (!fileExtension.equals("json"))
                continue;

            Path sourcePath = Paths.get(mediaDirectory + "/" + fileName);
            Path targetDirectory = Paths.get(mediaDirectory);

            if (!varName.contains("png")) {
                String newFileName = varName + "_png." + fileExtension;
                String newFileNameWebp = varName + "_webp." + fileExtension;

                Path copiedFilePath = Files.copy(sourcePath, targetDirectory.resolve(newFileName), StandardCopyOption.REPLACE_EXISTING);
                Path renamedFilePath = copiedFilePath.resolveSibling(newFileNameWebp);
                Files.move(copiedFilePath, renamedFilePath, StandardCopyOption.REPLACE_EXISTING);
                FileMeta.jsonFilePathConvert(renamedFilePath.toString());

                File newFile = new File(file.getParent(), newFileName);
                if (file.renameTo(newFile)) {
                    System.out.println("Файл успешно переименован в " + newFileName);
                } else {
                    System.err.println("Не удалось переименовать файл " + fileName);
                }
            }
        }

        mediaFiles = getMediaFiles(mediaDirectory);
        for (File file : mediaFiles) {
            String fileName = file.getName();
            String varName = fileName.split("\\.")[0]; // Имя переменной импорта без расширения
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
            String varNameCamelCase = toCamelCase(varName, fileExtension);
            String nameFirstPart = getFirstNamePart(varName);

            FileMeta fileMeta = new FileMeta();
            fileMeta.setFileName(fileName);
            fileMeta.setFileExtension(fileExtension);
            fileMeta.setFilePath(fileName);
            fileMeta.setFileNameWithoutExtension(varName);
            fileMeta.setCamelCaseName(varNameCamelCase);
            fileMeta.setGroup(nameFirstPart);
            if (varNameCamelCase.contains("Webp"))
                fileMeta.setType(FileMeta.Type.WEBP);
            else if (varNameCamelCase.contains("Png"))
                fileMeta.setType(FileMeta.Type.PNG);

            groups.remove(nameFirstPart);
            groups.add(nameFirstPart);
            filesMeta.add(fileMeta);
        }

        // Записываем манифесты в JavaScript-файл
        writeManifestsToJS(jsFilePath, filesMeta);

        System.out.println("Манифесты успешно созданы и записаны в " + jsFilePath);
    }

    private static String getFirstNamePart(String varName) {
        String[] parts = varName.split("_");
        return parts[0];
    }

    private static File[] getMediaFiles(String directoryPath) {
        File directory = new File(directoryPath);
        return directory.listFiles();
    }

    private static void writeManifestsToJS(String filePath, List<FileMeta> filesMeta) {
        try (FileWriter writer = new FileWriter(filePath)) {
            filesMeta.sort(Comparator.comparing(FileMeta::getCamelCaseName));
            // Записываем импорты
            for (FileMeta meta : filesMeta) {
                writer.write("import " + meta.getCamelCaseName() + " from '" + meta.getFilePath() + "';\n");
            }
            writer.write("\n");
            for (String group : groups) {
                writer.write("export const " + group + " = '" + group + "'\n");
            }
            writer.write("\n");

            for (FileMeta.Type type : FileMeta.Type.values()) {
                // Записываем манифест
                writer.write("export const manifest" + type.getExtension() + " = {\n");
                for (String group : groups) {
                    writer.write("    " + "[" + group + "]: {\n");
                    int i = 0;
                    for (FileMeta meta : filesMeta) {
                        if (Objects.nonNull(meta.getType()) && meta.getType().equals(type) && meta.getGroup().equals(group)) {
                            writer.write("    " + "    " + nextKey(i) + ": " + meta.getCamelCaseName() + ",\n");
                            i++;
                        }
                    }
                    writer.write("    " + "},\n");
                }
                writer.write("};\n\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String nextKey(int index) {
        return keys.get(index % keys.size());
    }

    private static String toCamelCase(String fileNameWithoutExtension, String extension) {
        String[] parts = fileNameWithoutExtension.split("_");

        StringBuilder camelCaseName = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            camelCaseName.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }

        boolean hasExtension = false;
        for (FileMeta.Type type : FileMeta.Type.values()) {
            if (camelCaseName.toString().toLowerCase().contains(type.getExtension()))
                hasExtension = true;
        }
        if (!hasExtension)
            camelCaseName.append(Character.toUpperCase(extension.charAt(0))).append(extension.substring(1));
        if (extension.equals("json"))
            camelCaseName.append(Character.toUpperCase(extension.charAt(0))).append(extension.substring(1));

        String camelCaseFileName = camelCaseName.toString();
        return camelCaseFileName;
    }
}

