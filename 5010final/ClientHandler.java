import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

public class ClientHandler implements Runnable {
  private Socket clientSocket;
  private Sever server;
  private DataOutputStream out;
  private DataInputStream in;
  private String userName;
  private static final String DISCONNECT = "logoff";

  public ClientHandler(Socket socket, Sever server) {
    this.clientSocket = socket;
    this.server = server;
  }

  @Override
  public void run() {
    try {
      setupStreams();
      processClientRequests();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      cleanup();
    }
  }

  public void setupStreams() throws IOException {
    out = new DataOutputStream(clientSocket.getOutputStream());
    in = new DataInputStream(clientSocket.getInputStream());
  }
//初始化io流
  private void processClientRequests() throws IOException {
    while (true) {
      Message chatMessage = Protocol.parseMessage(in);
      handleCommand(chatMessage);
    }
  }

  public void handleCommand(Message chatMessage) throws IOException {
    switch (chatMessage.messageId) {
      case Protocol.CONNECT_MESSAGE:
        handleConnectMessage(chatMessage);
        break;
      case Protocol.DISCONNECT_MESSAGE:
        handleDisconnectMessage(chatMessage);
        break;
      case Protocol.QUERY_CONNECTED_USERS:
        handleQueryConnectedUsers(chatMessage);
        break;
      case Protocol.DIRECT_MESSAGE:
        handleDirectMessage(chatMessage);
        break;
      case Protocol.BROADCAST_MESSAGE:
        handleBroadcastMessage(chatMessage);
        break;
      case Protocol.SEND_INSULT:
        handleSendInsultMessage(chatMessage);
        break;
    }
  }
//
  private void handleConnectMessage(Message chatMessage) throws IOException {
    this.userName = chatMessage.content;
    server.addClient(userName, this);

    String responseMessage = "Connected Successfully. There are " + (server.getNumberOfConnectedClients() - 1) + " other connected clients.";

    byte[] response = Protocol.createConnectResponseMessage(true, responseMessage);
    sendMessage(response);
  }

  private void handleDisconnectMessage(Message chatMessage) throws IOException {
    String disconnectingUsername = chatMessage.content;

    boolean isSuccess = server.removeClient(disconnectingUsername);

    String responseMessage;
    if (isSuccess) {
      responseMessage = disconnectingUsername + ", You are no longer connected.";
    } else {
      responseMessage = "Disconnect failed: User '" + disconnectingUsername + " not found.";
    }

    byte[] response = Protocol.createDisconnectResponseMessage(isSuccess, responseMessage);
    sendMessage(response);

    if (isSuccess && in.readUTF().equals(DISCONNECT)) {
      closeConnection();
    }
  }

  private void handleQueryConnectedUsers(Message chatMessage) throws IOException {
    String requestingUsername = chatMessage.content;

    if (server.isConnected(requestingUsername)) {
      List<String> otherConnectedUsernames = server.getOthernames(requestingUsername);

      byte[] response = Protocol.createQueryResponseMessage(otherConnectedUsernames);
      sendMessage(response);
    } else {
      byte[] response = Protocol.createQueryResponseMessage(Collections.emptyList());
      sendMessage(response);
    }
  }

  private void handleDirectMessage(Message chatMessage) throws IOException {
    String content = chatMessage.content;
    String[] parts = content.split("\\|");

    String senderUsername = parts[0].substring("sender:".length()).trim();
    String recipientUsername = parts[1].substring("receiver:".length()).trim();
    String messageContent = parts[2].substring("message:".length()).trim();

    ClientHandler recipientHandler = server.getClient(recipientUsername);
    ClientHandler senderHandler = server.getClient(senderUsername);

    if (!server.isConnected(senderUsername) || !server.isConnected(recipientUsername)) {
      String failureMessage = "Failed to send message: Recipient or sender not found or connected.";
      sendFailureMessage(failureMessage);
      return;
    }

    if (recipientHandler != null && senderHandler!= null) {
      byte[] messageBytes = Protocol.createDirectMessage(senderUsername, recipientUsername, messageContent);
      recipientHandler.sendMessage(messageBytes);
    } else {
      String failureMessage = "Failed to send message: Recipient or sender not found or connected.";
      senderHandler.sendFailureMessage(failureMessage);
    }
  }

  private void handleBroadcastMessage(Message chatMessage) throws IOException {
    String[] parts = chatMessage.content.split(":", 3);

    String senderUsername = parts[1].trim();
    String messageContent = parts[2].trim();

    if (!server.isConnected(senderUsername)) {
      System.err.println("Error: Sender '" + senderUsername + "' is not connected. Cannot broadcast message.");
      return;
    }

    for (ClientHandler client : server.getAllClients()) {
        byte[] messageBytes = Protocol.createBroadcastMessage(senderUsername, messageContent);
        client.sendMessage(messageBytes);
    }
  }

  private void handleSendInsultMessage(Message chatMessage) throws IOException {
    String senderUsername = chatMessage.content.split(" -> ")[0];
    String recipientUsername = chatMessage.content.split(" -> ")[1];

    if (!server.isConnected(recipientUsername)) {
      System.err.println("Error: Recipient '" + recipientUsername + "' not found or not connected.");
      return;
    }

    String insult = RandomInsult.generateRandom();
    ClientHandler recipientHandler = server.getClient(recipientUsername);
    ClientHandler senderHandler = server.getClient(senderUsername);

    if (recipientHandler != null && senderHandler != null) {
      byte[] messageBytes = Protocol.createDirectMessage(senderUsername, recipientUsername, insult);
      recipientHandler.sendMessage(messageBytes);
    } else {
      senderHandler.sendFailureMessage("Send message failed. Recipient not found or not connected. ");
    }
  }

  public void sendFailureMessage(String failureMessage) throws IOException {
    byte[] messageBytes = Protocol.createFailedMessage(failureMessage);
    sendMessage(messageBytes);
  }

  public void sendMessage(byte[] messageBytes) throws IOException {
    out.write(messageBytes);
    out.flush();
  }

  public void cleanup() {
    closeConnection();
    server.removeClient(userName);
  }

  public void setUserName(String userName) {
    this.userName = userName;
    server.addClient(userName, this);
  }

  public String getUserName() {
    return userName;
  }

  public Sever getServer() {
    return server;
  }

  public void closeConnection() {
    try {
      if (out != null) out.close();
      if (in != null) in.close();
      if (clientSocket != null) clientSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}


