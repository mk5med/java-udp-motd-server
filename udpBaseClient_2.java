
// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
public class udpBaseClient_2 {
    private final static int SERVER_PORT = 5000;
    public static String msg;

    public static void main(String args[]) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        
        InetAddress serverIP = InetAddress.getLocalHost();
        MOTDPacket motdPacket = new MOTDPacket(MOTDPacket.MOTDProtocolFlags.FLAG_TYPE_SYN);

        // Send SYN
        socket.send(MOTDProtocol.createMOTDDatagram(motdPacket, serverIP, SERVER_PORT));
        
        // Read SYNACK
        motdPacket = MOTDProtocol.createMOTDFromSocket(socket);

        // Send REQUEST

        // Load data and check for errors
    }
}