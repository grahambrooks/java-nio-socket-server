package grahambrooks.nio.socket.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SocketServer {

  private final int tcpPort;
  private Selector selector;
  private Semaphore started;
  private Thread thread;
  private Map<SocketChannel, List> dataMapper = new HashMap<>();

  public SocketServer(int tcpPort) {
    this.tcpPort = tcpPort;
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
      ServerSocketChannel serverChannel = ServerSocketChannel.open();
      serverChannel.configureBlocking(false);

      serverChannel.socket().bind(new InetSocketAddress(tcpPort));
      serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);

      this.started.release();


      while (true) {
        int select = this.selector.select(100);

        if (Thread.currentThread().isInterrupted()) {
          serverChannel.close();
        }

        final Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();

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
    } catch (IOException e) {
      this.started.release();
      throw new RuntimeException(e);
    }
  }

  private void read(SelectionKey key) {
    SocketChannel channel = (SocketChannel) key.channel();
    InputStreamHandler handler = (InputStreamHandler) key.attachment();

    handler.read(channel);
  }

  private void accept(SelectionKey key) {
    System.out.println("Accepting connection");

    try {
      ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();

      SocketChannel channel = serverChannel.accept();


      channel.configureBlocking(false);
      Socket socket = channel.socket();
      SocketAddress remoteAddr = socket.getRemoteSocketAddress();
      System.out.println("Connected to: " + remoteAddr);
      // register channel with selector for further IO
      dataMapper.put(channel, new ArrayList());
      SelectionKey register = channel.register(this.selector, SelectionKey.OP_READ);
      if (register.attachment() != null) {
        System.out.println("KEY ALREADY HAS ATTACHMENT");
      }
      register.attach(new InputStreamHandler());

    } catch (ClosedChannelException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public void stop() {
    try {
      if (selectorNeedsToClose()) {
        this.selector.close();
        if (this.selector.isOpen()) {

        }

      }
      if (isRunning()) {
        thread.interrupt();
      }
    } catch (IOException e) {
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
