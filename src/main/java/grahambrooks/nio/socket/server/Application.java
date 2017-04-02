package grahambrooks.nio.socket.server;

public class Application {
  public static void main(String[] args) {
    SocketServer socketServer = new SocketServer(9000);

    socketServer.start();
  }
}
