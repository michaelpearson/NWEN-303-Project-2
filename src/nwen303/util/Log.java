package nwen303.util;

import java.io.PrintStream;
import java.util.Date;

public class Log {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static final PrintStream terminal = System.out;
    private final String owner;

    public Log(String owner) {
        this.owner = owner;
    }

    public void logMessage(String message) {
        System.out.printf("%s[%s] [%s]%s %s\n", ANSI_CYAN, new Date(), owner, ANSI_RESET, message);
    }

    public void logf(String format, Object...args) {
        logMessage(String.format(format, args));
    }
}
