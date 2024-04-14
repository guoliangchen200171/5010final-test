import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
  private Socket socket;
  private DataInputStream in;
  private DataOutputStream out;

  public Client(Socket socket) throws IOException {
    this.socket = socket;
    this.out = new DataOutputStream(socket.getOutputStream());
    this.in = new DataInputStream(socket.getInputStream());
  }
  private static final String TARGET_ADDRESS = "127.0.0.1";
  private static final String DISCONNECT_WORD = "Exit";
  public static void main(String[] args) throws IOException {
    int port = Integer.parseInt(args[0]);
    BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("Please enter your username to join the chat: ");
    String username = stdIn.readLine().trim();

    try {
      Socket socket = new Socket(TARGET_ADDRESS, port);
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      DataInputStream in = new DataInputStream(socket.getInputStream());
      // Send connect message to the server
      byte[] connectMessage = Protocol.createConnectMessage(username);
      out.write(connectMessage);
      //handleInput(socket,in,out,username);
      // Start a thread to handle user input
      Thread userInputThread = new Thread(() -> {
        try (BufferedReader stdInTemp = new BufferedReader(new InputStreamReader(System.in))) {
          CommandHandler commandHandler = new CommandHandler(socket, username);
          String userInput;
          // Continuously read user input until there's none left (null)
          while ((userInput = stdIn.readLine()) != null) {
            commandHandler.DealCommand(userInput); // Process each command entered by the user
          }
        } catch (IOException e) {
          e.printStackTrace(); // Print stack trace for any IOExceptions
        }
      });

      // Start a thread to handle server input
      Thread serverInputThread = new Thread(() -> {
        try {
          while (true) {
            Message chatMessage = Protocol.parseMessage(in); // Parse incoming messages from server
            System.out.println("[Server]: " + chatMessage.content); // Print messages from the server

            // Check if the server sent a disconnect command
            if (chatMessage.content.equals(DISCONNECT_WORD)) {
              out.writeUTF(DISCONNECT_WORD); // Send disconnect acknowledgment
              socket.close(); // Close the socket
              break; // Exit the loop to end the thread
            }
          }
        } catch (IOException e) {
          e.printStackTrace(); // Print stack trace for any IOExceptions
        }
      });

      // Start both threads
      userInputThread.start();
      serverInputThread.start();

      // Optionally wait for both threads to finish
      try {
        userInputThread.join();
        serverInputThread.join();
      } catch (InterruptedException e) {
        e.printStackTrace(); // Print stack trace for any InterruptedExceptions
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw e; // Rethrow to indicate failure to start
    }
  }

}
