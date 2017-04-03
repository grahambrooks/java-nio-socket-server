package grahambrooks.nio.socket.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputStreamHandler implements ReadHandler {
  private final static Logger log = Logger.getLogger(SocketServer.class.getName());
  private final LineBufferingStream stream;

  public InputStreamHandler(LineBufferingStream stream) {
    this.stream = stream;
  }

  InputStreamHandler() {
    this(new LineBufferingStream(System.out::println));
  }

  @Override
  public void read(SocketChannel channel) {
    try {
      ByteBuffer buffer = ByteBuffer.allocate(1024);
      int bytesRead;
      do {
        bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
          handleConnectionClosed(channel);
          return;
        }

        stream.write(new String(buffer.array(), 0, bytesRead));
        buffer.clear();
      } while (bytesRead > 0);
    } catch (IOException e) {
      log.log(Level.WARNING, "Error reading from socket channel ", e);
    }
  }

  private void handleConnectionClosed(SocketChannel channel) throws IOException {
    Socket socket = channel.socket();
    log.log(Level.INFO, "Connection closed by client: " + socket.getRemoteSocketAddress());
    channel.close();
    stream.close();
  }
}
