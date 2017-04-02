package grahambrooks.nio.socket.server;

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.range;

public class FunctionalTests {

  private static final int TEST_PORT = 9000;
  private static final String LOREM_IPSEM = "Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium" +
    " doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae " +
    "vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia " +
    "consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia " +
    "dolor sit amet consectetur adipisci[ng] velit, sed quia non numquam [do] eius modi tempora inci[di]dunt, ut labore et " +
    "dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit " +
    "laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate " +
    "velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur?\n";

  @Test
  public void acceptsConnection() throws IOException {
    Accepter Accepter = new Accepter(TEST_PORT);
    Accepter.start();

    Socket socket = testSocket();
    socket.close();

    Accepter.stop();
  }

  @Test
  public void acceptsMultipleConnections() throws IOException {
    Accepter Accepter = new Accepter(TEST_PORT);
    Accepter.start();

    List<Socket> sockets = (range(0, 10)).mapToObj(i -> testSocket()).collect(Collectors.toList());

    sockets.forEach(this::close);

    Accepter.stop();
  }

  @Test
  public void acceptsDataWithMultipleConnections() throws IOException, InterruptedException {
    Accepter Accepter = new Accepter(TEST_PORT);
    Accepter.start();

    List<Socket> sockets = (range(0, 10)).mapToObj(i -> testSocket()).collect(Collectors.toList());

    sockets.forEach(this::sendTestData);
    Thread.sleep(500);
    sockets.forEach(this::close);
    Thread.sleep(200);

    Accepter.stop();
  }

  private void sendTestData(Socket socket) {
    try {
      OutputStream outputStream = socket.getOutputStream();
      String[] loremIpsem = LOREM_IPSEM.split(" ");
      for (String s : loremIpsem) {
        String text = s + " ";
        outputStream.write(text.getBytes());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Socket testSocket() {
    try {
      return new Socket(InetAddress.getLocalHost(), TEST_PORT);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void close(Socket socket) {
    try {
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
