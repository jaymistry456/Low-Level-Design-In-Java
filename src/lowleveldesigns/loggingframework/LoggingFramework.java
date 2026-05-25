package lowleveldesigns.loggingframework;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;

/*
enums
* */
enum SeverityLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    CRITICAL;
}

/*
LogMessage
LogFormatter (interface)
SimpleFormatter (implements LogFormatter)
JSONFormatter (implements LogFormatter)
Logger (interface)
LoggerDecorator (implements Logger)
ConsoleLogger (extends LoggerDecorator)
FileLogger (extends LoggerDecorator)
DatabaseLogger (extends LoggerDecorator)
LoggingFramework
* */

/*
LogMessage
    knows:
        SeverityLevel
        message
        timestamp
        source
    does:
        nothing (data carrier)
* */
class LogMessage {
    private SeverityLevel level;
    private String message;
    private LocalDateTime timestamp;
    private String source;

    public LogMessage(SeverityLevel level, String message, LocalDateTime timestamp, String source) {
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
        this.source = source;
    }

    public SeverityLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSource() {
        return source;
    }
}

/*
LogFormatter (interface)
    knows:
    does:
        format(LogMessage) -> String
* */
interface LogFormatter {
    String format(LogMessage logMessage);
}

/*
SimpleFormatter (implements LogFormatter)
    knows:
    does:
        format(LogMessage) -> String
* */
class SimpleFormatter implements LogFormatter {
    @Override
    public String format(LogMessage logMessage) {
        return String.format("[%s] %s %s - %s",
                logMessage.getLevel(),
                logMessage.getTimestamp(),
                logMessage.getSource(),
                logMessage.getMessage());
    }
}

/*
JSONFormatter (implements LogFormatter)
    knows:
    does:
        format(LogMessage) -> String
* */
class JSONFormatter implements LogFormatter {
    @Override
    public String format(LogMessage logMessage) {
        String message = logMessage.toString();
        return "JSON message: " + message;
    }
}

/*
Logger (interface)
    knows:
    does:
        log(LogMessage logMessage)
* */
interface Logger {
    void log(LogMessage logMessage);
}

/*
LoggerDecorator (implements Logger)
    knows:
        next (Logger)
        LogFormatter
        minimumLevel (SeverityLevel)
    does:
        shouldLog(LogMessage) -> boolean
        passToNext(LogMessage)
* */
abstract  class LoggerDecorator implements Logger {
    protected Logger next;
    protected LogFormatter logFormatter;
    protected SeverityLevel minimumLevel;

    public LoggerDecorator(Logger next, LogFormatter logFormatter, SeverityLevel minimumLevel) {
        this.next = next;
        this.logFormatter = logFormatter;
        this.minimumLevel = minimumLevel;
    }

    protected boolean shouldLog(LogMessage logMessage) {
        return logMessage.getLevel().ordinal() >= minimumLevel.ordinal();
    }

    protected void passToNext(LogMessage logMessage) {
        if(next == null) return;
        next.log(logMessage);
    }
}

/*
ConsoleLogger (extends LoggerDecorator)
    knows:
        next (Logger)
        LogFormatter
        minimumLevel (SeverityLevel)
    does:
        log(LogMessage message)
* */
class ConsoleLogger extends LoggerDecorator {
    public ConsoleLogger(Logger next, LogFormatter logFormatter, SeverityLevel minimumLevel) {
        super(next, logFormatter, minimumLevel);
    }

    @Override
    public void log(LogMessage logMessage) {
        if(shouldLog(logMessage)) {
            String formattedMessage = logFormatter.format(logMessage);
            System.out.println(formattedMessage);
        }
        passToNext(logMessage);
    }
}

/*
FileLogger (extends LoggerDecorator)
    knows:
        next (Logger)
        LogFormatter
        minimumLevel (SeverityLevel)
    does:
        log(LogMessage message)
* */
class FileLogger extends LoggerDecorator {
    public FileLogger(Logger next, LogFormatter logFormatter, SeverityLevel minimumLevel) {
        super(next, logFormatter, minimumLevel);
    }

    @Override
    public void log(LogMessage logMessage) {
        if(shouldLog(logMessage)) {
            String formatterMessage = logFormatter.format(logMessage);
            System.out.println("Log Message written to File");
        }
        passToNext(logMessage);
    }
}

/*
DatabaseLogger (extends LoggerDecorator)
    knows:
        next (Logger)
        LogFormatter
        minimumLevel (SeverityLevel)
    does:
        log(LogMessage message)
* */
class DatabaseLogger extends LoggerDecorator {
    public DatabaseLogger(Logger next, LogFormatter logFormatter, SeverityLevel minimumLevel) {
        super(next, logFormatter, minimumLevel);
    }

    @Override
    public void log(LogMessage logMessage) {
        if(shouldLog(logMessage)) {
            String formatterMessage = logFormatter.format(logMessage);
            System.out.println("Log Message written to Database");
        }
        passToNext(logMessage);
    }
}

/*
LoggingFramework
    knows:
        Logger
    does:
        log(LogMessage message)
* */

public class LoggingFramework {
    private Logger logger;

    public LoggingFramework(Logger logger) {
        this.logger = logger;
    }

    public void log(LogMessage logMessage) {
        logger.log(logMessage);
    }
}

/*

LogMessage has-a SeverityLevel

SimpleFormatter is-a LogFormatter

JSONFormatter is-a LogFormatter

LoggerDecorator is-a Logger

ConsoleLogger is-a Logger

FileLogger is-a Logger

DatabaseLogger is-a Logger
DatabaseLogger has-a Logger (next)
DatabaseLogger has-a LogFormatter
DatabaseLogger has-a SeverityLevel

LoggingFramework has-a Logger

* */