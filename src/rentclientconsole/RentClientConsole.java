package rentclientconsole;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RentClientConsole {

  static final String MACHINE = "localhost";
  static final int PORT = 40000;

  public static void main(String[] args) {
    try (
      Socket s = new Socket(MACHINE, PORT);
      Scanner sc = new Scanner(s.getInputStream());
      PrintWriter pw = new PrintWriter(s.getOutputStream());
      Scanner console = new Scanner(System.in);
    ) {
      String command = "";
      pw.println("rent_client_console_connection");
      pw.flush();
      do {
        System.out.println("Message: " + sc.nextLine());
        System.out.println("Available commands: " + sc.nextLine());
        System.out.print("Rent> ");
        command = console.nextLine();
        pw.println(command);
        pw.flush();
      } while (!command.trim().equalsIgnoreCase("logout"));
      System.out.println("Message: " + sc.nextLine());
    } catch (IOException ex) {
      String msg = "Server not running or mismatching host/port.";
      Logger.getLogger(RentClientConsole.class.getName()).log(Level.SEVERE, msg, ex);
    }
  }
}
