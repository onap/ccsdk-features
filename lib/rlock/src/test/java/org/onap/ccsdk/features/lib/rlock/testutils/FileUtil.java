package org.onap.ccsdk.features.lib.rlock.testutils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

    public static String read(String fileName) throws Exception {
        String ss = "";
        try (InputStream is = FileUtil.class.getResourceAsStream(fileName)) {
            try (InputStreamReader isr = new InputStreamReader(is)) {
                try (BufferedReader in = new BufferedReader(isr)) {
                    String s = in.readLine();
                    while (s != null) {
                        ss += s + '\n';
                        s = in.readLine();
                    }
                }
            }
        }
        return ss;
    }
}
