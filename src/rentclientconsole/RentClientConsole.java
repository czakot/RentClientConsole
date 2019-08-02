package rentclientconsole;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RentClientConsole {

  public static void main(String[] args) {
    final String MACHINE = "localhost";
    final int PORT = 40000;
    final String rentClientPrgId = "console";

    Locale locale = (args.length == 2) ? new Locale(args[0], args[1]) : Locale.getDefault();
    ResourceBundle messages = ResourceBundle.getBundle("rentclientconsole.MessageBundle", locale);
    // TODO how to load properties files from different directory
    
    try (
      Socket s = new Socket(MACHINE, PORT);
      Scanner sc = new Scanner(s.getInputStream());
      PrintWriter pw = new PrintWriter(s.getOutputStream());
      Scanner console = new Scanner(System.in);
    ) {
      String command;
      pw.println(rentClientPrgId);
      pw.println(locale.toString());
      pw.flush();
      do {
        System.out.println(messages.getString("message") + sc.nextLine());
        System.out.println(messages.getString("available_commands") + sc.nextLine());
        System.out.print("Rent> ");
        command = console.nextLine();
        pw.println(command);
        pw.flush();
      } while (!command.trim().equalsIgnoreCase("logout"));
      System.out.println(messages.getString("message") + sc.nextLine());
    } catch (IOException ex) {
      String msg = "Server not running or mismatching host/port.";
      Logger.getLogger(RentClientConsole.class.getName()).log(Level.SEVERE, msg, ex);
    }
  }
}
