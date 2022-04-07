
// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
public class udpBaseClient_2 {
    private final static int SERVER_PORT = 5000;
    public static String msg;

    public static void main(String args[]) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        
        InetAddress serverIP = InetAddress.getLocalHost();
        MOTDPacket motdPacket = new MOTDPacket(MOTDProtocolFlags.FLAG_TYPE_SYN);
        DatagramPacket outPacket = MOTDProtocol.createMOTDDatagram(motdPacket, serverIP, SERVER_PORT);
		
        // Send SYN and expect SYNACK
        MOTDProtocol.connectionSend(socket, outPacket, MOTDProtocolFlags.FLAG_TYPE_SYNACK);

        // Send REQUEST
        motdPacket.setType(MOTDProtocolFlags.FLAG_TYPE_REQUEST);
        socket.send(MOTDProtocol.createMOTDDatagram(motdPacket, serverIP, SERVER_PORT));
		byte[] receive = new byte[65535];
		DatagramPacket DpReceive = null;
        // Load data and check for errors
		while(true){
			DpReceive = new DatagramPacket(receive, receive.length);
			socket.receive(DpReceive);
			System.out.println(data(receive));
    }
	}
	public static StringBuilder data(byte[] a)
		{
		if (a == null)
			return null;
		StringBuilder ret = new StringBuilder();
		int i = 0;
		while (a[i] != 0)
		{
			ret.append((char) a[i]);
			i++;
		}
		return ret;
		}
}
