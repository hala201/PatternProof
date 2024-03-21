import java.util.logging.Level;

abstract class Logger {
   private Logger nextLogger;

   public void setNextLogger(Logger nextLogger) {
       this.nextLogger = nextLogger;
   }

   public void logMessage(Level level, String message) {
       if (this.getLevel().intValue() <= level.intValue()) {
           this.write(message);
       }
       if (this.nextLogger != null) {
           this.nextLogger.logMessage(level, message);
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
       return Level.WARNING;
   }

   @Override
   protected void write(String message) {
       System.out.println("Error Logger: " + message);
   }
}

public class ChainGood {
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
       loggerChain.logMessage(Level.WARNING, "This is an ERROR message.");
   }
}
