package grahambrooks.nio.socket;

import grahambrooks.nio.socket.server.InputStreamHandler;
import grahambrooks.nio.socket.server.LineBufferingStream;
import grahambrooks.nio.socket.server.SocketReadHandlerFactory;
import grahambrooks.nio.socket.server.SocketServer;

public class SampleApplication {
  public static void main(String[] args) {
    SocketReadHandlerFactory readHandlerFactory = () -> new InputStreamHandler(new LineBufferingStream(System.out::println));
    SocketServer socketServer = new SocketServer(9000, readHandlerFactory);

    socketServer.start();
  }
}
