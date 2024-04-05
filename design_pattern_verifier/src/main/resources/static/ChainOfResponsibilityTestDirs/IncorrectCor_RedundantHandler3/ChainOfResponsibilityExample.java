package ChainOfResponsibilityTestDirs.IncorrectCor_UnhandledRequest;

import java.util.logging.Level;

abstract class Logger {
    private Logger nextLogger;

    public void setNextLogger(Logger nextLogger) {
        this.nextLogger = nextLogger;
    }

    public void logMessage(Level level, String message) {
        if (this.getLevel().intValue() <= level.intValue()) {
            write(message);
        }
        if (nextLogger != null) {
            nextLogger.logMessage(level, message);
        }
    }

    protected abstract Level getLevel();

    protected abstract void write(String message);
}

class ConsoleLogger extends Logger {
    @Override
    protected Level getLevel() {
        return Level.INFO;
    }

    @Override
    protected void write(String message) {
        System.out.println("Console Logger: " + message);
    }
}

class FileLogger extends Logger {
    @Override
    protected Level getLevel() {
        return Level.ALL;
    }

    @Override
    protected void write(String message) {
        System.out.println("File Logger: " + message);
    }
}

class ErrorLogger extends Logger {
    @Override
    protected Level getLevel() {
        return Level.SEVERE;
    }

    @Override
    protected void write(String message) {
        System.out.println("Error Logger: " + message);
    }
}

class WhateverLogger extends Logger {
    @Override
    protected Level getLevel() {
        return Level.FINE;
    }

    @Override
    protected void write(String message) {
        System.out.println("Whatever Logger: " + message);
    }
}

public class ChainOfResponsibilityExample {
    private static Logger getLoggerChain() {
        Logger errorLogger = new ErrorLogger();
        Logger fileLogger = new FileLogger();
        Logger consoleLogger = new ConsoleLogger();

        errorLogger.setNextLogger(fileLogger);
        fileLogger.setNextLogger(consoleLogger);

        return errorLogger;
    }

    public static void main(String[] args) {
        Logger loggerChain = getLoggerChain();

        loggerChain.logMessage(Level.INFO, "This is an INFO message.");
        loggerChain.logMessage(Level.ALL, "This is a DEBUG message.");
        loggerChain.logMessage(Level.SEVERE, "This is an ERROR message.");
    }
}