import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MOTDProtocol {
  public static boolean establishHandshake(DatagramSocket socket, Helpers.Session session) throws IOException {
    System.out.println("Starting handshake");
    MOTDPacket packet;
    // Read a SYN MOTDPacket packet
    packet = createMOTDFromSocket(socket);
    // Save the inbound connection information
    session = new Helpers.Session(packet.address, packet.port);

    if (packet.getType() != MOTDPacket.MOTDProtocolFlags.FLAG_TYPE_SYN) {
      // Handle retransmission and closing the connection
    }

    // Send a SYNACK MOTDPacket packet
    packet = new MOTDPacket(MOTDPacket.MOTDProtocolFlags.FLAG_TYPE_SYNACK);
    socket.send(MOTDProtocol.createMOTDDatagram(packet, session.dAddress, session.dPort));

    // Read a REQUEST MOTDPacket packet
    packet = MOTDProtocol.createMOTDFromSocket(socket);
    if (packet.getType() != MOTDPacket.MOTDProtocolFlags.FLAG_TYPE_REQUEST) {
      // Handle retransmission and closing the connection
    }

    System.out.println("Handshake complete");
    // A connection is established and the data is ready to send
    // Return true if everything passed
    return true;
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
