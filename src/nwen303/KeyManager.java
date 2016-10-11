package nwen303;

import nwen303.server.Master;
import nwen303.server.RangeRequest;
import nwen303.shared.Range;
import nwen303.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.Queue;

public class KeyManager implements Master {

    private final ServerSocket serverSocket;
    private final String cipherText;
    private final int keySize;
    private final BigInteger initialKey;
    private final Queue<RangeRequest> requestQueue = new LinkedList<>();
    private final Object lock = new Object();
    private final Range searchSpace;
    private final String expectedPlaintext;

    private final static Log log = new Log("Key Manager");

    private boolean shutdown = false;

    public static void main(String argv[]) throws IOException {
        BigInteger initialKey = new BigInteger(argv[0]);
        int keySize = Integer.valueOf(argv[1]);
        String cipherText = argv[2];
        int portNumber = System.getenv("PORT") != null ? Integer.valueOf(System.getenv("PORT")) : (int)(Math.random() * 0xFFFF);
        log.logf("Listening on port: %d", portNumber);
        String expectedPlaintext = "May good flourish; Kia hua ko te pai";
        KeyManager keyManager = new KeyManager(new ServerSocket(portNumber), cipherText, keySize, initialKey, expectedPlaintext);
        keyManager.begin();
    }

    private KeyManager(ServerSocket serverSocket, String cipherText, int keysize, BigInteger initialKey, String expectedPlaintext) {
        this.serverSocket = serverSocket;
        this.cipherText = cipherText;
        this.keySize = keysize;
        this.initialKey = initialKey;
        this.expectedPlaintext = expectedPlaintext;

        byte[] bytes = new byte[keysize + 1];
        for(int a = 0;a < bytes.length;a++) {
            bytes[a] = (byte) 0xFF;
            if(a == 0) {
                bytes[a] = 0;
            }
        }

        this.searchSpace = new Range(initialKey, new BigInteger(bytes));
        log.logMessage(searchSpace.toString());

        new Thread(this::processQueue).start();
        log.logf("Init KeyManager cipher: %s", cipherText);
    }

    private void begin() throws IOException {
        serverSocket.setSoTimeout(1000);
        Socket connection;

        while(!shutdown) {
            try {
                connection = serverSocket.accept();
            } catch (SocketTimeoutException ignore) { continue; }
            new ClientManager(connection, this);
        }
    }

    private void processQueue() {
        while(!shutdown && !Thread.currentThread().isInterrupted()) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException ignore) {
                    shutdown = true;
                }
                if(!shutdown) {
                    RangeRequest request;
                    while ((request = requestQueue.poll()) != null) {
                        request.getClient().processRange(generateRange(request.getChunkSize()));
                    }
                }
            }
        }
        log.logMessage("Shutdown...");
    }

    private Range generateRange(long chunkSize) {
        log.logf("generating new range of size: %d", chunkSize);
        return searchSpace.split(chunkSize);
    }

    @Override
    public void requestRange(ClientManager manager, long chunkSize) {
        log.logMessage("Range requested");
        synchronized (lock) {
            requestQueue.add(new RangeRequest(manager, chunkSize));
            lock.notifyAll();
        }
    }

    @Override
    public String getCipher() {
        return cipherText;
    }

    @Override
    public int getKeyLength() {
        return keySize;
    }

    @Override
    public BigInteger getInitialKey() {
        return initialKey;
    }

    @Override
    public boolean isShuttingDown() {
        return shutdown;
    }

    @Override
    public void keyFound() {
        shutdown = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public String getExpectedPlaintext() {
        return expectedPlaintext;
    }
}
