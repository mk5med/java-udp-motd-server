import java.net.*;

import java.io.*;

public class MOTDServer {
  private final static int SERVER_PORT = 5000;
  public String message;
  DatagramSocket socket;

  public MOTDServer() throws SocketException {
    this.socket = new DatagramSocket(SERVER_PORT);
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

        sendMOTD("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", session);
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
      String data = message.substring(offset);

      MOTDPacket outPacket = new MOTDPacket(data.getBytes());
      outPacket.setSequence(sequence);
      
      // Send data
      DatagramPacket sndPacket = MOTDProtocol.createMOTDDatagram(outPacket, session.dAddress, session.dPort);
      MOTDPacket inPacket = MOTDProtocol.connectionSend(socket, sndPacket);

      // Make sure the ACK has a valid sequence number

      // Generate the next sequence number
      sequence = MOTDProtocol.nextSequenceValue(sequence);
    }
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
