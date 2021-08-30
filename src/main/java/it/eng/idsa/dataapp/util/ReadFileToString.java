package it.eng.idsa.dataapp.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReadFileToString
{
    public static String read(Path dataLakeDirectory, String fileName) throws IOException {

        String message = null;
        byte[] bytes;

        bytes = Files.readAllBytes(dataLakeDirectory.resolve(fileName));
        message = IOUtils.toString(bytes, "UTF8");

        return message;
    }
}
