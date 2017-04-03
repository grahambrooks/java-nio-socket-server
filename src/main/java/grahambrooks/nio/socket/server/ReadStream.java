package grahambrooks.nio.socket.server;

public interface ReadStream {
  void write(String text);

  void close();
}
