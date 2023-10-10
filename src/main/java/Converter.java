import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Converter {
    public static void convert(String pathDir, String pathOut) throws IOException {
        String convTo = "webp";

        if (convTo == null || convTo == "")
            convTo = "webp";

        List<Path> files = Files.walk(Paths.get(pathDir))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        int count = 0;
        for (Path path : files) {
            System.out.println(path.toAbsolutePath().toString());
            String name = pathOut + "/" + path.getFileName().toString();
            String ext = path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf("."));
            int lastIndex = name.lastIndexOf(".");
            if (lastIndex > -1 && Arrays
                    .stream(ImageMIMETypes.values())
                    .anyMatch(types -> Objects.equals("." + types.getExtension(), ext))) {
                name = name.substring(0, lastIndex);
                name += ("." + convTo);

                boolean isWrite = encodeWebpFromArr(path.toAbsolutePath().toString(), name, convTo);
                if (isWrite) {
                    count += 1;

                    //path.toFile().delete();
                }
            }
        }

        System.out.println(count);
    }

    public static boolean encodeWebpFromArr(String inputImagePath, String outputImagePath, String conv) throws IOException {
        FileInputStream inputStream = new FileInputStream(inputImagePath);


        BufferedImage image = ImageIO.read(inputStream);

        if (image == null) {
            System.out.println("ERROR: " + inputImagePath);
            return false;
        }
        FileOutputStream outputStream = new FileOutputStream(outputImagePath);

        boolean write = ImageIO.write(image, conv, outputStream);

        outputStream.close();
        inputStream.close();
        return write;
    }

    public static void generateImageAtlas(List<String> imagePaths, int atlasWidth, int atlasHeight, String outputPath, int cropSize) {
        BufferedImage atlas = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = atlas.createGraphics();

        int currentX = 0;
        int currentY = 0;

        for (String imagePath : imagePaths) {
            try {
                // Загрузка изображения
                BufferedImage image = ImageIO.read(new File(imagePath));

                // Масштабирование изображения для помещения в атлас
                Image scaledImage = image.getScaledInstance(cropSize, cropSize, Image.SCALE_FAST);
                BufferedImage resizedImage = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_INT_ARGB);
                resizedImage.getGraphics().drawImage(scaledImage, 0, 0, null);

                // Проверка, помещается ли изображение в текущую позицию атласа
                if (currentX + cropSize <= atlas.getWidth()) {
                    if (currentY + cropSize <= atlas.getHeight()) {
                        // Вставка изображения в атлас
                        graphics.drawImage(resizedImage, currentX, currentY, null);
                    }

                    // Обновление текущей позиции по горизонтали
                    currentX += cropSize;
                } else {
                    // Переход на новую строку, если достигнут конец строки
                    currentX = 0;
                    currentY += cropSize;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Сохранение атласа в файл
        try {
            ImageIO.write(atlas, "webp", new File(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Освобождение ресурсов
        graphics.dispose();
    }

    public static int nextPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
}
