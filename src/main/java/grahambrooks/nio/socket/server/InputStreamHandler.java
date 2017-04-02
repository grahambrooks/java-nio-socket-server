package grahambrooks.nio.socket.server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class InputStreamHandler {
  private final LineBufferingStream stream;

  InputStreamHandler() {
    stream = new LineBufferingStream(line -> System.out.println(line));
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
          stream.close();
          return;
        }

        stream.write(new String(buffer.array(), 0, bytesRead));
        buffer.clear();
      } while (bytesRead > 0);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
