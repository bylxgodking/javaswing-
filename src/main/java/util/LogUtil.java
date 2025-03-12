package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "system.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static {
        // 确保日志目录存在
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    public static void info(String message) {
        log("INFO", message, null);
    }
    
    public static void error(String message, Throwable e) {
        log("ERROR", message, e);
    }
    
    private static synchronized void log(String level, String message, Throwable e) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_DIR + File.separator + LOG_FILE, true))) {
            String timestamp = DATE_FORMAT.format(new Date());
            writer.println(String.format("[%s] [%s] %s", timestamp, level, message));
            if (e != null) {
                writer.println("Exception details:");
                e.printStackTrace(writer);
                writer.println("--------------------");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
} 