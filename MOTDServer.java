import java.net.*;
import java.util.Timer;
import java.util.*;
import java.io.*;

public class MOTDServer {
  public String message = "MOTD Not Ready.";

  private final static int SERVER_PORT = 5000;
  DatagramSocket socket;
  private int updateCount = 0;

  private Timer messageChangeTimer = new Timer();
  private long messageChangeDelay = 0L; // Wait 0 seconds before starting the timer
  private long messageChangePeriod = 5000L; // Update every 5 seconds

  private TimerTask messageChangeTask = new TimerTask() {
    public void run() {
      updateCount++;
      message = "Good day. It has been " + messageChangePeriod * updateCount + "s since the server started.";
    }
  };

  public MOTDServer() throws SocketException {
    this.socket = new DatagramSocket(SERVER_PORT);

    // Start the timer
    messageChangeTimer.scheduleAtFixedRate(messageChangeTask, messageChangeDelay, messageChangePeriod);
  }

  public void start() throws IOException, Exception {
    loop();
  }

  private void loop() {
    while (true) {
      // A session object to store the incoming session information
      Helpers.Session session = new Helpers.Session(null, 0);

      try {
        System.out.println("MOTDServer: Ready to connect.");
        MOTDProtocol.establishHandshake(socket, session);

        sendMOTD(message, session);
      } catch (IOException | RuntimeException e) {
        e.printStackTrace();

        System.out.println("MOTDServer: Recovering from error.");
      }
    }
  }

  private void sendMOTD(String message, Helpers.Session session) throws IOException {
    System.out.println("MOTDServer: Starting to send MOTD.");
    byte sequence = 0;

    for (int offset = 0; offset < message.length(); offset += 16) {
      // If offset + 16 is larger than the message length, then there are less than 16 characters in the next substring
      // This clause is used to fetch the remainder of the string without raising an IndexOutOfBounds error
      String data = (offset + 16) > message.length() ? message.substring(offset) : message.substring(offset, 16);

      MOTDPacket outPacket = new MOTDPacket(data.getBytes());
      outPacket.setSequence(sequence);

      // Send data
      DatagramPacket sndPacket = MOTDProtocol.createMOTDDatagram(outPacket, session.dAddress, session.dPort);
      MOTDPacket inPacket = MOTDProtocol.connectionSend(socket, sndPacket);

      // Make sure the ACK has a valid sequence number

      // Generate the next sequence number
      sequence = MOTDProtocol.nextSequenceValue(sequence);
    }

    MOTDPacket packet = new MOTDPacket(MOTDProtocolFlags.FLAG_TYPE_FIN);
    DatagramPacket sndPacket = MOTDProtocol.createMOTDDatagram(packet, session.dAddress, session.dPort);
    
    // Send a FIN and wait for an ACK
    MOTDProtocol.connectionSend(socket, sndPacket);
  }

  public static void main(String[] args) throws IOException, SocketException {
    MOTDServer server = new MOTDServer();
    try {
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
