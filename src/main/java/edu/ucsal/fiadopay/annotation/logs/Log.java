package edu.ucsal.fiadopay.annotation.logs;

import java.util.logging.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.io.IOException;

public class Log {
    private static Log instance;
    private Logger logger;
    private FileHandler fileHandler;

    private Log() {
        this.logger = Logger.getLogger(Log.class.getName());
        this.fileHandler = null;
        this.setup();
    }

    public static Log getInstance() {
        if(instance == null) instance = new Log();

        return instance;
    }

    private void setup() {
        try {
            this.fileHandler = new FileHandler("logs/app.log", true);
            this.logger.addHandler(this.fileHandler);
            
            Formatter formatter = new LogFormatter();
            this.fileHandler.setFormatter(formatter);

            this.logger.setUseParentHandlers(false); 

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

