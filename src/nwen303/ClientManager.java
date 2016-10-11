package nwen303;

import nwen303.server.Master;
import nwen303.shared.Commands;
import nwen303.shared.Range;
import nwen303.util.Log;
import nwen303.util.network.Numbers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class ClientManager {

    private final Socket connection;
    private final Log log;
    private final Master master;
    private BufferedInputStream inputStream;
    private OutputStream outputStream;

    ClientManager(Socket connection, Master master) {
        log = new Log(connection.getRemoteSocketAddress().toString());

        this.connection = connection;
        this.master = master;
        new Thread(this::serviceClient).start();
    }

    private void serviceClient() {
        log.logMessage("Accepted connection, sending welcome");
        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
            outputStream = connection.getOutputStream();
            sendWelcome(outputStream);

            while(connection.isConnected() && !master.isShuttingDown()) {
                switch (Commands.fromId(inputStream.read())) {
                    case SHUTDOWN:
                        connection.close();
                        return;
                    default:
                        throw new RuntimeException("Unknown command");
                    case REQUEST_RANGE:
                        log.logMessage("Range requested");
                        master.requestRange(this, Numbers.decodeLong(inputStream));
                        break;
                    case KEY_FOUND:
                        log.logMessage("Key found!");
                        log.logf("Key: %s", Numbers.decodeBigInteger(inputStream).toString());
                        master.keyFound();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.logMessage("Connection died");
    }

    private void sendWelcome(OutputStream outputStream) throws IOException {
        log.logMessage("Sending welcome");
        outputStream.write(Commands.WELCOME.getCommand());

        byte cipher[] = master.getCipher().getBytes("UTF-8");
        outputStream.write(Numbers.encodeLong(cipher.length));
        outputStream.write(cipher);
        outputStream.write(Numbers.encodeLong(master.getKeyLength()));
        outputStream.write(Numbers.encodeLong(master.getExpectedPlaintext().length()));
        outputStream.write(master.getExpectedPlaintext().getBytes("UTF-8"));
    }

    void processRange(Range range) {
        try {
            if (range == null) {
                log.logMessage("Range is null, sending shutdown command");
                outputStream.write(Commands.SHUTDOWN.getCommand());
                return;
            }
            log.logf("Sending client range: %s", range);
            outputStream.write(Commands.PROCESS_RANGE.getCommand());
            outputStream.write(Numbers.encodeBigInteger(range.getStart()));
            outputStream.write(Numbers.encodeBigInteger(range.getEnd()));
        } catch(IOException ignore) {
            log.logMessage("Error sending process range command");
        }
    }

}
