package nwen303;

import nwen303.shared.Commands;
import nwen303.util.Blowfish;
import nwen303.util.Log;
import nwen303.util.network.Numbers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;

public class Client {
    private final Socket connection;
    private final Log log = new Log("Client");

    private Thread processThread;

    private byte[] cipher;
    private int keySize;
    private long chunkSize;
    private boolean shutdown = false;
    private String extectedPlaintext;


    public static void main(String argv[]) throws IOException {
        String host = argv[0];
        int portNumber = Integer.valueOf(argv[1]);
        long chunkSize = Long.valueOf(argv[2]);

        portNumber = portNumber == 0 ? Integer.valueOf(System.getenv("PORT")) : portNumber;

        Client client = new Client(new Socket(host, portNumber), chunkSize);
        client.begin();

        Runtime.getRuntime().addShutdownHook(new Thread(client::gracefulShutdown));
    }

    private Client(Socket connection, long chunkSize) {
        this.connection = connection;
        this.chunkSize = chunkSize;

    }

    private void begin() {
        log.logMessage("Starting waiting for instructions");
        processThread = Thread.currentThread();
        try {
            InputStream inputStream = connection.getInputStream();
            OutputStream outputStream = connection.getOutputStream();
            while (connection.isConnected() && !shutdown) {
                switch (Commands.fromId(inputStream.read())) {
                    default:
                        throw new RuntimeException("Unknown command");
                    case WELCOME:
                        processWelcome(inputStream);
                        requestRange(outputStream);
                        break;
                    case PROCESS_RANGE:
                        processRange(inputStream, outputStream);
                        break;
                    case SHUTDOWN:
                        log.logMessage("Got message to shutdown");
                        log.logMessage("Shutting down");
                        shutdown = true;
                        break;
                }
            }

            log.logMessage("Closing connection...");
            connection.close();

        } catch(IOException e) {
            log.logMessage("Connection died");
        }
    }

    private void processRange(InputStream inputStream, OutputStream outputStream) throws IOException {
        log.logMessage("Got command to process range");
        BigInteger start = Numbers.decodeBigInteger(inputStream);
        BigInteger end = Numbers.decodeBigInteger(inputStream);
        log.logf("Got range to process: %s, %s", start.toString(), end.toString());


        long range = end.subtract(start).longValue();
        for(int a = 0; a < range;a++) {
            byte[] key = Blowfish.asByteArray(start.add(BigInteger.valueOf(a)), keySize);
            Blowfish.setKey(key);
            String result = Blowfish.decryptToString(cipher);
            if(result.equals(extectedPlaintext)) {
                log.logMessage("Found key!");
                outputStream.write(Commands.KEY_FOUND.getCommand());
                outputStream.write(Numbers.encodeBigInteger(start.add(BigInteger.valueOf(a))));
                shutdown = true;
                break;
            }
        }

        if (!shutdown) {
            requestRange(outputStream);
        }
    }

    private void requestRange(OutputStream outputStream) throws IOException {
        log.logMessage("Requesting range to process");
        outputStream.write(Commands.REQUEST_RANGE.getCommand());
        outputStream.write(Numbers.encodeLong(chunkSize));
    }

    private void processWelcome(InputStream inputStream) throws IOException {
        log.logMessage("Processing welcome");
        int cipherLength = (int)Numbers.decodeLong(inputStream);
        byte buffer[] = new byte[cipherLength];
        if(inputStream.read(buffer) != cipherLength) {
            throw new IOException("Length miss match");
        }
        String plaintext;
        this.cipher = Blowfish.fromBase64(plaintext = new String(buffer, "UTF-8"));
        this.keySize = (int)Numbers.decodeLong(inputStream);

        int plaintextLength = (int) Numbers.decodeLong(inputStream);
        buffer = new byte[plaintextLength];
        if(inputStream.read(buffer) != plaintextLength) {
            throw new IOException("length miss match");
        }
        this.extectedPlaintext = new String(buffer, "UTF-8");

        log.logf("Got cipher, keysize and plaintext: %s %d %s", plaintext, keySize, extectedPlaintext);
    }

    /**
     * Shutdown client by sending details of the progress to the server and closing the connection
     */
    private void gracefulShutdown() {
        log.logMessage("Graceful shutdown");
        shutdown = true;
        try {
            processThread.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
