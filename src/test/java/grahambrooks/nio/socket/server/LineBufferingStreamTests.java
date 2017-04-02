package grahambrooks.nio.socket.server;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LineBufferingStreamTests {
  @Test
  public void emitsLineTextOnNewline() {
    StringBuffer output = new StringBuffer();
    LineBufferingStream stream = new LineBufferingStream(line -> output.append(line));

    stream.write("This is a line\n");
    stream.close();

    assertThat(output.toString()).isEqualTo("This is a line");
  }
}
