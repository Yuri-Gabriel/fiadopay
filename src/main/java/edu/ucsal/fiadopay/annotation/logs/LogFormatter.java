package edu.ucsal.fiadopay.annotation.logs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        String date = sdf.format(new Date(record.getMillis()));
        return date + " - " + record.getLevel() + " - " + record.getMessage() + "\n";
    }
}

