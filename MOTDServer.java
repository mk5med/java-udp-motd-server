import java.net.*;
import java.util.Timer;
import java.util.*;
import java.io.*;

public class MOTDServer {
  private final boolean DEBUG = false;
  public String message = "MOTD Not Ready.";

  private final static int SERVER_PORT = 5000;
  DatagramSocket socket;
  private int updateCount = 0;

  private Timer messageChangeTimer = new Timer();
  private long messageChangeDelay = 0L; // Wait 0 seconds before starting the timer
  private long messageChangePeriod = DEBUG ? 5000L : 1000 * 60 * 60 * 24; // Update every 5 seconds when in debug mode

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
    byte currentSequence = 0;

    for (int offset = 0; offset < message.length(); offset += 16) {
      // If offset + 16 is larger than the message length, then there are less than 16
      // characters in the next substring
      // This clause is used to fetch the remainder of the string without raising an
      // IndexOutOfBounds error
      String data = (offset + 16) > message.length() ? message.substring(offset)
          : message.substring(offset, offset + 16);

      MOTDPacket outPacket = new MOTDPacket(data.getBytes());
      outPacket.setSequence(currentSequence);

      // Send data
      DatagramPacket sndPacket = MOTDProtocol.createMOTDDatagram(outPacket, session.dAddress, session.dPort);
      MOTDPacket inPacket = null;
      try {
        inPacket = MOTDProtocol.connectionSend(socket, sndPacket);
      } catch (RuntimeException exception) {
        int _time = socket.getSoTimeout();
        socket.setSoTimeout(5000);
        socket.send(sndPacket);
        inPacket = MOTDProtocol.createMOTDFromSocket(socket);
        socket.setSoTimeout(_time);

        if (inPacket.getType() == MOTDProtocolFlags.FLAG_TYPE_REQUEST) {
          // Restart from the beginning
          offset = 0;
          continue;
        } else if (inPacket.getType() == MOTDProtocolFlags.FLAG_TYPE_ACK) {
          if (inPacket.getSequence() != currentSequence) {
            // This is safe because at this point the offset is a multiple of 16
            offset -= 16;
            continue;
          }

          exception.printStackTrace();
        }

        // Otherwise stop the loop
        break;
      }

      // Generate the next sequence number
      currentSequence = MOTDProtocol.nextSequenceValue(currentSequence);
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
