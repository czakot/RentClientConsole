package rentclientconsole;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RentClientConsole {
  private final static String HOST = "localhost";
  private final static int PORT = 40000;
  private final static String RENT_CLIENT_UI_TYPE = "console";
  private final static String RESOURCE_BUNDLE_MESSAGES_DIRECTORY = "resources.MessageBundle";
  public static final String ACKNOWLEDGE_TOKEN = "copy";
  public static final String ACKNOWLEDGE_TOKEN_SP = "copy ";
  private static final char SEPARATOR = '|';
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
        receiveCommandsFromServer();
      } while (!s.isClosed());
    }
  }
  
  private static void setInternationalization(String[] args) {
    Locale locale = (args.length == 2) ? new Locale(args[0], args[1]) : Locale.getDefault();
    messages = ResourceBundle.getBundle(RESOURCE_BUNDLE_MESSAGES_DIRECTORY, locale);
  }

  //TODO timeout: extended Scanner class narrowed down for reading a line with timeout
  
  private static boolean successfulConnection() {
    return initialDataForServer(RENT_CLIENT_UI_TYPE, "UI type") &&
           initialDataForServer(messages.getLocale().toString(), "Locals");
  }

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

  private static boolean initialDataForServer(String paramToSend, String failedReplyMsgInsert) {
    boolean successful = true;
    String reply;
    pw.println(paramToSend);
    pw.flush();
    reply = sc.nextLine();
    if (!reply.equals(ACKNOWLEDGE_TOKEN_SP + paramToSend)) {
      successful = false;
      String msg = "Server has not acknoledged " + failedReplyMsgInsert + " (" + RENT_CLIENT_UI_TYPE +
                   "). Server reply: " + reply;
      logger.info(msg);
    }
    return successful;
  }

  private static void receiveCommandsFromServer() {
    String command;
    String commandExtension = null;
    do {
      String receivedLine = sc.nextLine();
      int firstPipeCharAt = receivedLine.indexOf(SEPARATOR);
      if (firstPipeCharAt < 0) {
        command = receivedLine;
      } else {
        command = receivedLine.substring(0, firstPipeCharAt - 1).trim();
        commandExtension = receivedLine.substring(firstPipeCharAt + 1).trim();
      }
      processReceivedCommand(command, commandExtension);
    } while (!command.equals(ACKNOWLEDGE_TOKEN));
  }

  private static void processReceivedCommand(String command, String commandExtension) {
    String[] params = {};
    if (commandExtension != null) {
      params = commandExtension.split("" + SEPARATOR);
    }
    switch (command) {
      case "set available commands":
        availableCommands.clear();
        for (String param : params) {
          availableCommands.add(param);
        }
        break;
      case "display available commands":
        System.out.print(messages.getString("available_commands"));
        availableCommands.forEach((availableCommand) -> { System.out.print(availableCommand + " ");});
        System.out.println();
        break;
      case "message":
        System.out.println(messages.getString("message") + commandExtension);
        break;
      case "copy": // params[0] = disconnect/terminate
        if (commandExtension != null) {
          try {
            s.close();
          } catch (IOException ex) {
            Logger.getLogger(RentClientConsole.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
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
