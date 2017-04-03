package grahambrooks.nio.socket.server;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LineBufferingStreamTests {
  @Test
  public void emitsLineOnReceipt() {
    StringBuffer output = new StringBuffer();
    LineBufferingStream stream = new LineBufferingStream(output::append);

    stream.write("This is a line\n");
    assertThat(output.toString()).isEqualTo("This is a line");
    stream.write("This is another line\n");
    assertThat(output.toString()).isEqualTo("This is a lineThis is another line");
    stream.close();
    assertThat(output.toString()).isEqualTo("This is a lineThis is another line");
  }

  @Test
  public void emitsUnprocessedTextWhenClosed() {
    StringBuffer output = new StringBuffer();
    LineBufferingStream stream = new LineBufferingStream(output::append);

    stream.write("partial");
    stream.close();

    assertThat(output.toString()).isEqualTo("partial");
  }

  @Test
  public void emitsLineTextOnNewline() {
    StringBuffer output = new StringBuffer();
    LineBufferingStream stream = new LineBufferingStream(output::append);

    stream.write("This is a line\n");
    stream.close();

    assertThat(output.toString()).isEqualTo("This is a line");
  }
}
