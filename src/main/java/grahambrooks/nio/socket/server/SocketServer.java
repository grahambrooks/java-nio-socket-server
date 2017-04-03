package grahambrooks.nio.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

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
      this.started = new Semaphore(1);
      this.started.acquire();

      thread = new Thread(this::run);
      thread.start();

      this.started.acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  private void run() {
    Thread.currentThread().setName("Selector thread");
    try {
      this.selector = Selector.open();
      ServerSocketChannel serverChannel = openServerSocketChannel();

      this.started.release();


      while (true) {
        int select = this.selector.select(100);

        if (Thread.currentThread().isInterrupted()) {
          serverChannel.close();
        }

        if (select > 0) {
          processKeysWithWork(this.selector.selectedKeys().iterator());
        }
      }
    } catch (IOException e) {
      this.started.release();
      log.log(WARNING, "Selector thread failed", e);
    }
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

      SocketChannel channel = serverChannel.accept();
      channel.configureBlocking(false);
      Socket socket = channel.socket();

      log.log(INFO, "Connected to: " + socket.getRemoteSocketAddress());

      SelectionKey register = channel.register(this.selector, SelectionKey.OP_READ);
      if (register.attachment() != null) {
        log.log(WARNING, "KEY ALREADY HAS ATTACHMENT");
      }
      register.attach(readHandlerFactory.readHandler());
//      register.attach(new InputStreamHandler());

    } catch (IOException e) {
      e.printStackTrace();
    }
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
