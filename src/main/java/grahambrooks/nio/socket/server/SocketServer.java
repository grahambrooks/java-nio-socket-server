package grahambrooks.nio.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.util.logging.Level.*;

public class SocketServer {
  private static final Logger log = Logger.getLogger(SocketServer.class.getName());

  private final SocketReadHandlerFactory readHandlerFactory;
  private final int tcpPort;
  private Selector selector;
  private Semaphore started;
  private Thread thread;

  public SocketServer(int tcpPort, SocketReadHandlerFactory readHandlerFactory) {
    this.tcpPort = tcpPort;
    this.readHandlerFactory = readHandlerFactory;
  }

  public void start() {
    stop();
    try {
      initializeStartedSignal();

      thread = new Thread(this::run);
      thread.start();

      waitUntilStarted();
    } catch (InterruptedException e) {
      log.log(SEVERE, "Failed to start server thread", e);
    }
  }

  private void initializeStartedSignal() throws InterruptedException {
    this.started = new Semaphore(1);
    this.started.acquire();
  }

  private void waitUntilStarted() throws InterruptedException {
    this.started.acquire();
  }


  private void run() {
    setThreadName("Selector thread");
    try {
      this.selector = Selector.open();
      ServerSocketChannel serverChannel = openServerSocketChannel();
      signalStarted();

      while (true) {
        work(serverChannel);
      }
    } catch (IOException e) {
      this.started.release();
      log.log(WARNING, "Selector thread failed", e);
    }
  }

  private void work(ServerSocketChannel serverChannel) throws IOException {
    int workItemCount = this.selector.select(100);

    if (isInterrupted()) {
      serverChannel.close();
    }

    if (workItemCount > 0) {
      processKeysWithWork(this.selector.selectedKeys().iterator());
    }
  }

  private void setThreadName(String threadName) {
    Thread.currentThread().setName(threadName);
  }

  private void signalStarted() {
    this.started.release();
  }

  private boolean isInterrupted() {
    return Thread.currentThread().isInterrupted();
  }

  private void processKeysWithWork(Iterator<SelectionKey> keys) {
    keys.forEachRemaining((key) -> {
      if (key.isValid()) {
        if (key.isAcceptable()) {
          this.accept(key);
        } else if (key.isReadable()) {
          this.read(key);
        }
      }
      keys.remove();
    });
  }

  private ServerSocketChannel openServerSocketChannel() throws IOException {
    ServerSocketChannel serverChannel = ServerSocketChannel.open();
    serverChannel.configureBlocking(false);

    serverChannel.socket().bind(new InetSocketAddress(tcpPort));
    serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    return serverChannel;
  }

  private void read(SelectionKey key) {
    SocketChannel channel = (SocketChannel) key.channel();
    ReadHandler handler = (ReadHandler) key.attachment();

    handler.read(channel);
  }

  private void accept(SelectionKey key) {
    log.log(INFO, "Accepting connection");

    try {
      ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

      SocketChannel channel = acceptNonBlocking(serverChannel);

      log.log(INFO, "Connected to: " + channel.socket().getRemoteSocketAddress());

      configureSelector(channel, this.selector, readHandlerFactory);

    } catch (IOException e) {
      log.log(WARNING, "Error accepting connection ", e);
    }
  }

  private void configureSelector(SocketChannel channel, Selector selector, SocketReadHandlerFactory readHandlerFactory) throws ClosedChannelException {
    SelectionKey register = channel.register(selector, OP_READ);

    if (register.attachment() != null) {
      log.log(WARNING, "KEY ALREADY HAS ATTACHMENT");
    }

    register.attach(readHandlerFactory.readHandler());
  }

  private SocketChannel acceptNonBlocking(ServerSocketChannel serverChannel) throws IOException {
    SocketChannel channel = serverChannel.accept();
    channel.configureBlocking(false);
    return channel;
  }

  public void stop() {
    try {
      if (selectorNeedsToClose()) {
        this.selector.close();
      }
      if (isRunning()) {
        thread.interrupt();
      }
    } catch (IOException e) {
      log.log(WARNING, "Failed closing selector channel", e);
      e.printStackTrace();
    }
  }

  private boolean isRunning() {
    return this.thread != null;
  }

  private boolean selectorNeedsToClose() {
    return this.selector != null && this.selector.isOpen();
  }
}
