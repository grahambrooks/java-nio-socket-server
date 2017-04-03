package grahambrooks.nio.socket.server;

public class Application {
  public static void main(String[] args) {
    SocketReadHandlerFactory readHandlerFactory = () -> new InputStreamHandler(new LineBufferingStream(line -> System.out.println(line)));
    SocketServer socketServer = new SocketServer(9000, readHandlerFactory);

    socketServer.start();
  }
}
