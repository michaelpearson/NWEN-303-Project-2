package nwen303.shared;

import java.util.Arrays;

public enum Commands {
    UNKNOWN(-1),
    WELCOME(0),
    PROCESS_RANGE(1),
    REQUEST_RANGE(2),
    MARK_RANGE_COMPLETE(3),
    SHUTDOWN(4),
    KEY_FOUND(5);

    private final byte commandId;

    Commands(int commandId) {
        this.commandId = (byte) commandId;
    }

    public byte getCommand() {
        return commandId;
    }

    public static Commands fromId(int id) {
        if(id == -1) {
            return SHUTDOWN;
        }
        return Arrays.stream(values()).filter((a) -> a.getCommand() == id).findFirst().orElse(UNKNOWN);
    }
}
