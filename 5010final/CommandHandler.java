import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class CommandHandler {
  private Socket socket;
  private DataOutputStream out;
  private String username;
  private static final String BROADCAST_COMMAND = "@all";
  private static final int BROADCAST_LENGTH = 5;
  private static final String DIRECT_MESSAGE = "@";
  private static final String LIST_COMMAND = "who";
  private static final String EXIT_COMMAND = "logoff";
  private static final String GET_RANDOM_COMMAND = "!";
  private static final String GUIDE_COMMAND = "?";

  public CommandHandler(Socket socket, String username) throws IOException {
    this.socket = socket;
    this.out = new DataOutputStream(socket.getOutputStream());
    this.username = username;
  }

  public void DealCommand(String command) throws IOException {
    if (command.startsWith(BROADCAST_COMMAND)) {
      broadcastMessage(command.substring(BROADCAST_LENGTH));
    } else if (command.startsWith(DIRECT_MESSAGE)) {
      personMessage(command);
    } else if (command.equals(LIST_COMMAND)) {
      getUsers();
    } else if (command.equals(EXIT_COMMAND)) {
      disconnect();
    } else if (command.startsWith(GET_RANDOM_COMMAND)) {
      getRandom(command);
    } else if (command.equals(GUIDE_COMMAND)) {
      Help();
    } else {
      System.out.println("Please use the right command,if you need help you can type ‘?’" + command);
    }
  }

  private void personMessage(String command) throws IOException {
    String[] information = command.split(" ", 2);
    if (information.length == 2) {
      String targetUser = information[0].substring(1);
      String message = information[1];
      byte[] messageBytes = Protocol.createDirectMessage(this.username, targetUser, message);
      out.write(messageBytes);
    }
  }

  private void broadcastMessage(String message) throws IOException {
    byte[] messageBytes = Protocol.createBroadcastMessage(this.username, message);
    out.write(messageBytes);
  }

  private void getUsers() throws IOException {
    byte[] messageBytes = Protocol.createQueryUsersMessage(this.username);
    out.write(messageBytes);
  }

  private void disconnect() throws IOException {
    byte[] messageBytes = Protocol.createDisconnectMessage(this.username);
    out.write(messageBytes);
  }

  private void getRandom(String command) throws IOException {
    String[] informations = command.split(" ", 2);
    if (informations.length >= 1) {
      String targetUser = informations[0].substring(1);
      byte[] messageBytes = Protocol.createSendInsultMessage(this.username, targetUser);
      out.write(messageBytes);
    }
  }

  private void Help() throws IOException {
    String help = "Available commands:\n"
            + "@[username] [message] - Send a direct message to [username]\n"
            + "@all [message] - Send a broadcast message to all users\n"
            + "who - Query connected users\n"
            + "logoff - Disconnect from the server\n"
            + "![username] - Send a random insult to [username]\n"
            + "? - Display this help message";
    System.out.println(help);
  }

}
