package nwen303.server;

import nwen303.ClientManager;

import java.math.BigInteger;

public interface Master {
    void requestRange(ClientManager manager, long l);
    String getCipher();
    int getKeyLength();
    BigInteger getInitialKey();
    boolean isShuttingDown();
    void keyFound();
    String getExpectedPlaintext();
}
