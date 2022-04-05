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

  private void loop() throws Exception {
    // A session object to store the incoming session information
    Helpers.Session session = null;

    boolean connectionEstablished = MOTDProtocol.establishHandshake(socket, session);
    
    if (!connectionEstablished) {
      // Temporary error handling
      throw new Exception("failed to establish a connection");
    }

    sendMOTD("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", session);
  }

  private void sendMOTD(String message, Helpers.Session session) throws IOException {

    byte sequence = 0;
    for (int offset = 0; offset < message.length(); offset += 16) {
      String data = message.substring(offset);
      
      MOTDPacket motdPacket = new MOTDPacket(data.getBytes());
      motdPacket.setSequence(sequence);

      // Send data
      socket.send(MOTDProtocol.createMOTDDatagram(motdPacket, session.dAddress, session.dPort));

      // Wait for ACK. This could result in a timeout

      // Make sure the ACK has a valid sequence number

      // Clear the buffer after every message.
      sequence = MOTDProtocol.nextSequenceValue(sequence);
    }
  }

  public static void main(String[] args) throws IOException, SocketException {
    System.out.println("READY TO CONNECT");
    MOTDServer server = new MOTDServer();
    try {
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
