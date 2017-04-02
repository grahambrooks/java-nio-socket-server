package grahambrooks.nio.socket.server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class InputStreamHandler {
    private final StringBuffer received;

    InputStreamHandler() {
        received = new StringBuffer();
    }

    void read(SocketChannel channel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead;
            do {
                bytesRead = channel.read(buffer);

                if (bytesRead == -1) {
                    Socket socket = channel.socket();
                    SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                    System.out.println("Connection closed by client: " + remoteAddr);
                    channel.close();
                    processedReceived();
                    System.out.println("PENDING RECEIVED: " + received.toString());
                    return;
                }

                received.append(new String(buffer.array(), 0, bytesRead));
                buffer.clear();
            } while (bytesRead > 0);

            processedReceived();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processedReceived() {
        int eol = received.indexOf("\n");
        while (eol >= 0) {
            String line = received.substring(0, eol);
            System.out.println("LINE " + line);
            received.delete(0, eol + 1);
            eol = received.indexOf("\n");
        }
    }
}
