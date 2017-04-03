package grahambrooks.nio.socket.server;

public class LineBufferingStream implements ReadStream{
  private final Delegate delegate;
  private final StringBuffer received;

  public LineBufferingStream(Delegate delegate) {
    this.delegate = delegate;
    this.received = new StringBuffer();
  }

  @Override
  public void write(String text) {
    received.append(text);
    emitLines();
  }

  @Override
  public void close() {
    emitLines();
    this.delegate.accept(received.toString());
  }

  private void emitLines() {
    int eol = received.indexOf("\n");
    while (eol >= 0) {
      String line = received.substring(0, eol);

      this.delegate.accept(line);

      received.delete(0, eol + 1);
      eol = received.indexOf("\n");
    }
  }

  public interface Delegate {
    void accept(String line);
  }
}
