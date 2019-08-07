package rentclientconsole;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RentClientConsole {
  private final static String HOST = "localhost";
  private final static int PORT = 40000;
  private final static String UI_TYPE = "console";
  private final static String RESOURCE_BUNDLE_MESSAGES_DIRECTORY = "resources.MessageBundle";
  public static final String ACKNOWLEDGE_TOKEN = "copy";
  public static final String ACKNOWLEDGE_TOKEN_SP = "copy ";
  private static final String WHITESPACES = "\\s+";
  private static final String SPACE = " ";
  private static final String PROMPT = "Rent> ";

  private static ResourceBundle messages;
  private static Socket s;
  private static Scanner sc;
  private static PrintWriter pw;
  private static Scanner console;
  private static final Logger logger = Logger.getLogger(RentClientConsole.class.getName());
  private static final LinkedList<String> availableCommands = new LinkedList();

  public static void main(String[] args) {
    setInternationalization(args);
    if (successfulInputOutputSettings()) {
      do {
        commandsFromServer();
      } while (!s.isClosed());
    }
  }
  
  private static void setInternationalization(String[] args) {
    Locale locale = (args.length == 2) ? new Locale(args[0], args[1]) : Locale.getDefault();
    messages = ResourceBundle.getBundle(RESOURCE_BUNDLE_MESSAGES_DIRECTORY, locale);
  }

  //TODO timeout: extended Scanner class narrowed down for reading a line with timeout
  
  private static boolean successfulInputOutputSettings() {
    boolean successful = true;
    try {
      s = new Socket(HOST, PORT);
      sc = new Scanner(s.getInputStream());
      pw = new PrintWriter(s.getOutputStream());
      console = new Scanner(System.in);
    } catch (IOException ex) {
      successful = false;
      String msg = "Server not running or port (" + PORT +") or host (" + HOST + ") mismatching.";
      logger.info(msg);
    }
    return successful;
  }

  private static void commandsFromServer() {
    String command;
    String extension = null;
    String line = sc.nextLine().trim().replaceAll(WHITESPACES, SPACE);
    int firstSpaceAt = line.indexOf(SPACE);
    if (firstSpaceAt < 0) {
      command = line;
    } else {
      command = line.substring(0, firstSpaceAt - 1);
      extension = line.substring(firstSpaceAt + 1);
    }
    processReceivedCommand(command, extension);
  }

  private static void processReceivedCommand(String command, String commandExtension) {
    String[] params = {};
    if (commandExtension != null) {
      params = commandExtension.split(WHITESPACES);
    }
    switch (command) {
      case "set_available_commands":
        availableCommands.clear();
        availableCommands.addAll(Arrays.asList(params));
        break;
      case "display_available_commands":
        System.out.print(messages.getString("available_commands"));
        availableCommands.forEach((availableCommand) -> { System.out.print(availableCommand + " ");});
        System.out.println();
        break;
      case "message":
        System.out.println(messages.getString("message") + commandExtension);
        break;
      case "question_user_interface_type":
        sendCommandToServer(UI_TYPE);
        break;
      case "question_locals":
        sendCommandToServer(messages.getLocale().toString());
        break;
      case "terminate":
      case "disconnect":
        String origin = (command.equals("terminate")) ? "solicited" : "forced";
        try {
          s.close();
          logger.log(Level.INFO, "Closing connection ({0}) and exiting ...", origin);
        } catch (IOException ex) {
          Logger.getLogger(RentClientConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
        break;
      case "copy":
        sendCommandToServer();
        break;
      default:
    }
  }

  private static void sendCommandToServer() {
    String command;
    System.out.print(PROMPT);
    command = console.nextLine();
    pw.println(command);
    pw.flush();
  }
  private static void sendCommandToServer(String command) {
    System.out.print(PROMPT + command);
    pw.println(command);
    pw.flush();
  }
}
