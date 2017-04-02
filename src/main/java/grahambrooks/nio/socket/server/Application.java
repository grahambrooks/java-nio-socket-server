package grahambrooks.nio.socket.server;

public class Application {
  public static void main(String[] args) {
    Accepter accepter = new Accepter(9000);

    accepter.start();
  }
}
