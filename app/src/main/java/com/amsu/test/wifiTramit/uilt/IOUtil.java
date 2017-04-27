package com.amsu.test.wifiTramit.uilt;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by HP on 2017/4/27.
 */

public class IOUtil {

    //关闭流
    public static void closeIOStream(Closeable... closeableList) {
        try {
            for (Closeable closeable : closeableList) {
                if (closeable != null){
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
