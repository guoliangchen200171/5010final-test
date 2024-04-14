import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Protocol {
  public static final int CONNECT_MESSAGE = 19;
  public static final int CONNECT_RESPONSE = 20;
  public static final int DISCONNECT_MESSAGE = 21;
  public static final int QUERY_CONNECTED_USERS = 22;
  public static final int QUERY_USER_RESPONSE = 23;
  public static final int BROADCAST_MESSAGE = 24;
  public static final int DIRECT_MESSAGE = 25;
  public static final int FAILED_MESSAGE = 26;
  public static final int SEND_INSULT = 27;


  public static byte[] createConnectMessage(String username) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);
    out.writeInt(CONNECT_MESSAGE);
    byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
    out.writeInt(usernameBytes.length);
    out.write(usernameBytes);
    return bytestream.toByteArray();
  }

  private static Message parseConnectMessage(DataInputStream in) throws IOException {
    int usernameLength = in.readInt();
    byte[] usernameBytes = new byte[usernameLength];
    in.readFully(usernameBytes);
    String username = new String(usernameBytes, StandardCharsets.UTF_8);
    return new Message(CONNECT_MESSAGE, username);
  }

  public static byte[] createConnectResponseMessage(boolean success, String responseMessage) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);
    out.writeInt(CONNECT_RESPONSE);
    out.writeBoolean(success);
    byte[] messageBytes = responseMessage.getBytes(StandardCharsets.UTF_8);
    out.writeInt(messageBytes.length);
    out.write(messageBytes);
    return bytestream.toByteArray();
  }

  private static Message parseConnectResponseMessage(DataInputStream in) throws IOException {
    boolean success = in.readBoolean();
    int messageLength = in.readInt();
    byte[] messageBytes = new byte[messageLength];
    in.readFully(messageBytes);
    String responseMessage = new String(messageBytes, StandardCharsets.UTF_8);

    String content = "Success: " + success + ", Message: " + responseMessage;
    return new Message(CONNECT_RESPONSE, content);
  }

  public static byte[] createDisconnectMessage(String username) throws IOException {
    ByteArrayOutputStream byteoutstream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(byteoutstream);

    out.writeInt(DISCONNECT_MESSAGE);

    byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
    out.writeInt(usernameBytes.length);
    out.write(usernameBytes);

    return byteoutstream.toByteArray();
  }

  private static Message parseDisconnectMessage(DataInputStream in) throws IOException {
    int usernameLength = in.readInt();
    byte[] usernameBytes = new byte[usernameLength];
    in.readFully(usernameBytes);

    String username = new String(usernameBytes, StandardCharsets.UTF_8);

    return new Message(DISCONNECT_MESSAGE, username);
  }

  public static byte[] createDisconnectResponseMessage(boolean success, String responseMessage) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);

    out.writeInt(CONNECT_RESPONSE);

    out.writeBoolean(success);

    byte[] messageBytes = responseMessage.getBytes(StandardCharsets.UTF_8);
    out.writeInt(messageBytes.length);
    out.write(messageBytes);

    return bytestream.toByteArray();
  }

  private static Message parseDisconnectResponseMessage(DataInputStream in) throws IOException {
    boolean success = in.readBoolean();
    int messageLength = in.readInt();
    byte[] messageBytes = new byte[messageLength];
    in.readFully(messageBytes);

    String responseMessage = new String(messageBytes, StandardCharsets.UTF_8);

    String content = "Success: " + success + ", Message: " + responseMessage;
    return new Message(CONNECT_RESPONSE, content);
  }

  public static byte[] createQueryUsersMessage(String username) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);

    out.writeInt(QUERY_CONNECTED_USERS);

    byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
    out.writeInt(usernameBytes.length);
    out.write(usernameBytes);

    return bytestream.toByteArray();
  }

  private static Message parseQueryUsersMessage(DataInputStream in) throws IOException {
    int usernameLength = in.readInt();
    byte[] usernameBytes = new byte[usernameLength];
    in.readFully(usernameBytes);

    String username = new String(usernameBytes, StandardCharsets.UTF_8);

    return new Message(QUERY_CONNECTED_USERS, username);
  }

  public static byte[] createQueryResponseMessage(List<String> connectedUsernames) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);

    out.writeInt(QUERY_USER_RESPONSE);

    out.writeInt(connectedUsernames.size());

    for (String username : connectedUsernames) {
      byte[] usernameBytes = username.getBytes(StandardCharsets.UTF_8);
      out.writeInt(usernameBytes.length);
      out.write(usernameBytes);
    }

    return bytestream.toByteArray();
  }

  private static Message parseQueryResponseMessage(DataInputStream in) throws IOException {
    int Users = in.readInt();

    StringBuilder usersList = new StringBuilder();
    for (int i = 0; i < Users; i++) {
      int usernameLength = in.readInt();

      byte[] usernameBytes = new byte[usernameLength];
      in.readFully(usernameBytes);

      String username = new String(usernameBytes, StandardCharsets.UTF_8);
      usersList.append(username).append(i < Users - 1 ? ", " : "");
    }

    return new Message(QUERY_USER_RESPONSE, "Connected Users: " + usersList.toString());
  }

  public static byte[] createBroadcastMessage(String senderUsername, String message) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);

    out.writeInt(BROADCAST_MESSAGE);

    byte[] senderUsernameBytes = senderUsername.getBytes(StandardCharsets.UTF_8);
    out.writeInt(senderUsernameBytes.length);
    out.write(senderUsernameBytes);

    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
    out.writeInt(messageBytes.length);
    out.write(messageBytes);

    return bytestream.toByteArray();
  }

  private static Message parseBroadcastMessage(DataInputStream in) throws IOException {
    int senderUsernameLength = in.readInt();
    byte[] senderUsernameBytes = new byte[senderUsernameLength];
    in.readFully(senderUsernameBytes);
    String senderUsername = new String(senderUsernameBytes, StandardCharsets.UTF_8);
    int messageLength = in.readInt();
    byte[] messageBytes = new byte[messageLength];
    in.readFully(messageBytes);
    String message = new String(messageBytes, StandardCharsets.UTF_8);

    return new Message(BROADCAST_MESSAGE, "Information from: " + senderUsername + ": " + message);
  }


  public static byte[] createDirectMessage(String senderUsername, String recipientUsername, String message) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);
    out.writeInt(DIRECT_MESSAGE);
    byte[] senderUsernameBytes = senderUsername.getBytes(StandardCharsets.UTF_8);
    out.writeInt(senderUsernameBytes.length);
    out.write(senderUsernameBytes);

    byte[] recipientUsernameBytes = recipientUsername.getBytes(StandardCharsets.UTF_8);
    out.writeInt(recipientUsernameBytes.length);
    out.write(recipientUsernameBytes);

    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
    out.writeInt(messageBytes.length);
    out.write(messageBytes);

    return bytestream.toByteArray();
  }

  private static Message parseDirectMessage(DataInputStream in) throws IOException {
    int senderUsernameLength = in.readInt();
    byte[] senderUsernameBytes = new byte[senderUsernameLength];
    in.readFully(senderUsernameBytes);
    String senderUsername = new String(senderUsernameBytes, StandardCharsets.UTF_8);

    int recipientUsernameLength = in.readInt();
    byte[] recipientUsernameBytes = new byte[recipientUsernameLength];
    in.readFully(recipientUsernameBytes);
    String recipientUsername = new String(recipientUsernameBytes, StandardCharsets.UTF_8);

    int messageLength = in.readInt();
    byte[] messageBytes = new byte[messageLength];
    in.readFully(messageBytes);
    String message = new String(messageBytes, StandardCharsets.UTF_8);

    return new Message(DIRECT_MESSAGE, "sender:" + senderUsername + "|receiver:" + recipientUsername + "|message:" + message);
  }


  public static byte[] createFailedMessage(String failureMessage) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);

    out.writeInt(FAILED_MESSAGE);

    byte[] failureMessageBytes = failureMessage.getBytes(StandardCharsets.UTF_8);
    out.writeInt(failureMessageBytes.length);
    out.write(failureMessageBytes);

    return bytestream.toByteArray();
  }

  private static Message parseFailedMessage(DataInputStream in) throws IOException {
    int failureMessageLength = in.readInt();
    byte[] failureMessageBytes = new byte[failureMessageLength];
    in.readFully(failureMessageBytes);

    String failureMessage = new String(failureMessageBytes, StandardCharsets.UTF_8);

    return new Message(FAILED_MESSAGE, failureMessage);
  }


  public static byte[] createSendInsultMessage(String senderUsername, String recipientUsername) throws IOException {
    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bytestream);
    out.writeInt(SEND_INSULT);
    byte[] senderUsernameBytes = senderUsername.getBytes(StandardCharsets.UTF_8);
    out.writeInt(senderUsernameBytes.length);
    out.write(senderUsernameBytes);
    byte[] recipientUsernameBytes = recipientUsername.getBytes(StandardCharsets.UTF_8);
    out.writeInt(recipientUsernameBytes.length);
    out.write(recipientUsernameBytes);
    return bytestream.toByteArray();
  }

  private static Message parseSendInsultMessage(DataInputStream in) throws IOException {
    int senderUsernameLength = in.readInt();
    byte[] senderUsernameBytes = new byte[senderUsernameLength];
    in.readFully(senderUsernameBytes);
    String senderUsername = new String(senderUsernameBytes, StandardCharsets.UTF_8);

    int recipientUsernameLength = in.readInt();
    byte[] recipientUsernameBytes = new byte[recipientUsernameLength];
    in.readFully(recipientUsernameBytes);
    String recipientUsername = new String(recipientUsernameBytes, StandardCharsets.UTF_8);

    return new Message(SEND_INSULT, senderUsername + " -> " + recipientUsername);
  }

  public static Message parseMessage(DataInputStream in) throws IOException {
    int messageId = in.readInt();
    switch (messageId) {
      case CONNECT_MESSAGE:
        return parseConnectMessage(in);
      case CONNECT_RESPONSE:
        return parseConnectResponseMessage(in);
      case DISCONNECT_MESSAGE:
        return parseDisconnectMessage(in);
      case QUERY_CONNECTED_USERS:
        return parseQueryUsersMessage(in);
      case QUERY_USER_RESPONSE:
        return parseQueryResponseMessage(in);
      case BROADCAST_MESSAGE:
        return parseBroadcastMessage(in);
      case DIRECT_MESSAGE:
        return parseDirectMessage(in);
      case FAILED_MESSAGE:
        return parseFailedMessage(in);
      case SEND_INSULT:
        return parseSendInsultMessage(in);
      default:
        return new Message(messageId, "Unknown message type");
    }
  }

  public static byte[] stringToBytes(String str) {
    return str.getBytes(StandardCharsets.UTF_8);
  }

  public static String bytesToString(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

}
