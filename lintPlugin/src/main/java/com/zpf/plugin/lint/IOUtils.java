package com.zpf.plugin.lint;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
}