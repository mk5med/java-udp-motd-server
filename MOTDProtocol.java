import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class MOTDProtocol {
  public static void establishHandshake(DatagramSocket socket, Helpers.Session session) throws IOException, RuntimeException {
    System.out.println("Handshake: Waiting");
    MOTDPacket packet;

    socket.setSoTimeout(0); // Wait indefinitely for a connection

    // READ a SYN MOTDPacket packet
    // This will block until data is read
    packet = createMOTDFromSocket(socket);

    // Save the inbound connection information
    session.dAddress = packet.address;
    session.dPort = packet.port;

    if (packet.getType() != MOTDProtocolFlags.FLAG_TYPE_SYN) {
      System.out.println("Handshake: Failed");
      throw new RuntimeException("Handshake: Failed. Expected SYN type");
    }

    // SEND a SYNACK MOTDPacket packet. This is grouped with READ
    DatagramPacket datagramPacket = MOTDProtocol.createMOTDDatagram(new MOTDPacket(MOTDProtocolFlags.FLAG_TYPE_SYNACK), session.dAddress, session.dPort);

    // READ a REQUEST MOTDPacket packet
    // This will throw an error if it fails to send the SYNACK or detect a REQUEST packet
    MOTDProtocol.connectionSend(socket, datagramPacket, MOTDProtocolFlags.FLAG_TYPE_REQUEST);

    // A connection is established and the data is ready to send
    System.out.println("Handshake: Complete");
  }

  /**
   * Send a datagram over a socket and wait for an ACK response
   * @param socket
   * @param sndPacket
   * @return
   * @throws IOException
   */
  public static MOTDPacket connectionSend(DatagramSocket socket, DatagramPacket sndPacket)
  throws IOException {
    return connectionSend(socket, sndPacket, MOTDProtocolFlags.FLAG_TYPE_ACK, 5000, 3);
  }

  public static MOTDPacket connectionSend(DatagramSocket socket, DatagramPacket sndPacket, byte expectType)
  throws IOException {
    return connectionSend(socket, sndPacket, expectType, 5000, 3);
  }

  public static MOTDPacket connectionSend(DatagramSocket socket, DatagramPacket sndPacket, byte expectType, int timeout, int tries)
      throws IOException {

    int initialTimeout = socket.getSoTimeout();
    socket.setSoTimeout(timeout);

    // Send the packet
    socket.send(sndPacket);

    int try_count = 0;
    MOTDPacket rcvPacket = null;

    while (try_count < tries) {
      try {
        rcvPacket = MOTDProtocol.createMOTDFromSocket(socket);
        if ((rcvPacket.getType() & expectType) != expectType) {
          throw new RuntimeException("NOT EXPECTED TYPE");
        }

        break;
      } catch (SocketTimeoutException | RuntimeException exception) {
        // This triggers if the connection timed out or if the packet type is not expected
        try_count++;
        rcvPacket = null;

        // If the try_count reaches a threshold, stop retransmitting and report an error
        if (try_count == tries)
          throw exception;

        System.out.println("Packet failed. retransmitting.");
        socket.send(sndPacket);
      }
    }

    // Reset the socket timeout
    socket.setSoTimeout(initialTimeout);
    return rcvPacket;
  }

  /**
   * Return the next sequence value. This simulates the alternating bit protocol
   * 
   * @param currentSequenceValue
   * @return
   */
  public static byte nextSequenceValue(int currentSequenceValue) {
    if (currentSequenceValue == 0)
      return 1;
    return 0;
  }

  /**
   * Convert a MOTDPacket to a DatagramPacket
   * The returned packet is ready to be sent through a socket
   * 
   * @param motdPacket
   * @param address
   * @param port
   * @return
   */
  public static DatagramPacket createMOTDDatagram(MOTDPacket motdPacket, InetAddress address, int port) {
    byte[] buf = motdPacket.getData();
    return new DatagramPacket(buf, buf.length, address, port);
  }

  /**
   * Read data from a socket and attempt to wrap in a MOTDPacket
   * 
   * @param socket
   * @return
   * @throws IOException
   */
  public static MOTDPacket createMOTDFromSocket(DatagramSocket socket) throws IOException {
    byte[] buf = new byte[2 + 16];
    DatagramPacket datagram = new DatagramPacket(buf, buf.length);
    socket.receive(datagram);
    return MOTDPacket.fromPacket(datagram);
  }
}
