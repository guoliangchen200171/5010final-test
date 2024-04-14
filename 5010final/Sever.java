import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Sever {
  private static final int MAX= 10;
  private final int port;
  private final ExecutorService clientPool = Executors.newFixedThreadPool(MAX);
  //规定最大的线程数量
  private final ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();

  public Sever(int port) {
    this.port = port;
  }

  public void start() throws IOException {
    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("The port for the sever is: " + port);

    while (true) {
      Socket clientSocket = serverSocket.accept();
      ClientHandler clienthandler = new ClientHandler(clientSocket, this);
      clientPool.execute(clienthandler);
    }
  }

  public void addClient(String userName, ClientHandler clienthandler) {
    clients.put(userName, clienthandler);
  }

  public boolean removeClient(String userName) {
    if (clients.containsKey(userName)) {
      clients.remove(userName);
      return true;
    } else {
      return false;
    }
  }

  public ClientHandler getClient(String username) {
    return clients.get(username);
  }

  public boolean isConnected(String username) {
    return clients.containsKey(username);
  }

  public List<String> getOthernames(String excludeUsername) {
    List<String> othernames = new ArrayList<>();
    for (String username : clients.keySet()) {
      if (!username.equals(excludeUsername)) {
        othernames.add(username);
      }
    }
    return othernames;
  }

  public List<ClientHandler> getAllClients() {
    return new ArrayList<>(clients.values());
  }

  public int getNumberOfConnectedClients() {
    return clients.size();
  }


  public static void main(String[] args) throws IOException {
    int port = Integer.parseInt(args[0]);
    Sever server = new Sever(port);
    server.start();
  }
}
