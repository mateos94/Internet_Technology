package client;

import java.io.*;
import java.util.Base64;
import java.nio.file.Files;

public class Base64EncoderAndDecoder {
    static String encodeFileToBase64(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("could not read file " + file, e);
        }
    }

    static void decodeBase64ToFile(String base64String, String fileName) {
        byte[] data = Base64.getDecoder().decode(base64String);
        try (OutputStream stream = new FileOutputStream("InternetTechnologyAssignment/relatedTextFiles/" + fileName)) {
            stream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
