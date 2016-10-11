package nwen303.util.network;

import nwen303.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;

public class Numbers {

    private static Log log = new Log("Numbers Util");

    public static long decodeLong(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[8];
        if(inputStream.read(buffer) != 8) {
            log.logMessage("Could not read all numbers");
            throw new IOException("Could not read all numbers");
        }
        return decodeLong(buffer);
    }

    public static long decodeLong(byte[] buffer) {
        long build = 0;
        for (byte aBuffer : buffer) {
            build <<= 8;
            build |= (aBuffer & 0xFF);
        }
        return build;
    }

    public static byte[] encodeLong(long number) {
        byte[] buffer = new byte[8];
        for (int a = buffer.length - 1; a >= 0; a--) {
            buffer[a] = (byte)(number & 0xFF);
            number >>= 8;
        }
        return buffer;
    }

    public static byte[] encodeBigInteger(BigInteger number) {
        byte[] bytes = number.toByteArray();
        byte[] buffer = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, buffer, 1, bytes.length);
        buffer[0] = (byte)bytes.length;
        return buffer;
    }

    public static BigInteger decodeBigInteger(InputStream inputStream) throws IOException {
        int length = inputStream.read();
        if(length <= 0) {
            log.logMessage("Could not read the header");
            throw new IOException("Could not read the header");
        }
        byte[] buff = new byte[length];
        if(inputStream.read(buff) != length) {
            log.logMessage("Could not read the correct length of bytes");
            throw new IOException("Could not read the correct length of bytes");
        }
        return new BigInteger(buff);
    }

}
