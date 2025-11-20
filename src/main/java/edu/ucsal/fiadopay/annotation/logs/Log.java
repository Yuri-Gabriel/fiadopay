package edu.ucsal.fiadopay.annotation.logs;

import java.util.logging.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;

public class Log {
    private static List<Log> instances = new LinkedList<>();
    private String log_file;
    private Logger logger;
    private FileHandler fileHandler;

    private Log(String log_file) throws Throwable {

        if(log_file == null || log_file.trim().equals("")) throw new Exception(
            "File name to log_file is invalid"
        );

        this.log_file = log_file;
        this.logger = Logger.getLogger("LOG_" + this.log_file);
        this.fileHandler = null;
        this.setup();
    }

    public static Log getInstance(String log_file) throws Throwable {
        if(!hasInstance(log_file)) {
            instances.add(new Log(log_file));
        }
        Log instance = null;
        for(Log e : instances) {
            if(e.log_file.toLowerCase().equals(log_file.toLowerCase())) {
                instance = e;
            }
        }
        return instance;
    }

    private static boolean hasInstance(String log_file) {
        for(Log e : instances) {
            if(e.log_file.toLowerCase().equals(log_file.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private void setup() {
        try {
            File dir = new File("logs");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String file = this.log_file == null ? "app.log" : this.log_file;

            this.fileHandler = new FileHandler(
                System.getProperty("user.dir") + "/logs/" + file,
                true
            );
            this.logger.addHandler(this.fileHandler);
            
            Formatter formatter = new LogFormatter();
            this.fileHandler.setFormatter(formatter);

            this.logger.setUseParentHandlers(false); 

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (this.fileHandler != null) {
                    this.fileHandler.close();
                }
            }));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void info(String message) {
        this.logger.info(message);
    }

    public void warning(String message) {
        this.logger.warning(message);
    }

    public void severe(String message, Exception e) {
        this.logger.severe(message + " - Exception: " + e.getMessage());
    }
}

