package grahambrooks.nio.socket.server;

import java.nio.channels.SocketChannel;

public interface ReadHandler {
  void read(SocketChannel channel);
}
