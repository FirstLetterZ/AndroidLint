package com.zpf.plugin.lint;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

public class IOUtils {

    public static boolean writeToFile(InputStream inputStream, OutputStream outputStream) {
        byte[] buf = new byte[4096];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
                outputStream.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                //
            }
            try {
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                //
            }
        }
        return false;
    }

    public static boolean writeStringToFile(File file, String content) {
        try {
            byte[] data = content.getBytes(Charset.defaultCharset());
            return writeByteToFile(file, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeByteToFile(File file, byte[] data) {
        BufferedOutputStream bufferedOut = null;
        try {
            bufferedOut = new BufferedOutputStream(new FileOutputStream(file));
            bufferedOut.write(data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedOut != null) {
                    bufferedOut.flush();
                    bufferedOut.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void copyFile(String head, InputStream inputStream, File targetFile) {
        byte[] buffer = new byte[1024];
        try (RandomAccessFile target = new RandomAccessFile(targetFile, "rw")) {
            target.write(head.getBytes());
            while (inputStream.read(buffer) != -1) {
                target.write(buffer);
            }
            target.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                //
            }
        }
    }
}