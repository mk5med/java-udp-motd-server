
// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MOTDClient {
    private final static int SERVER_PORT = 5000;
    private final static boolean DEBUG = false;

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

        // Handshake has been established

        StringBuilder stringBuilder = new StringBuilder();
        MOTDPacket ackPacket = new MOTDPacket(MOTDProtocolFlags.FLAG_TYPE_ACK);
        MOTDPacket lastPacket = motdPacket;
        byte currentSequence = 0;

        // Load data and check for errors
        while ((motdPacket = MOTDProtocol.createMOTDFromSocket(socket)).getType() != MOTDProtocolFlags.FLAG_TYPE_FIN) {
            if (motdPacket.getSequence() != currentSequence) {
                // If it gets a sequence number that is different from the expected
                // It can be a duplicate of the last
                // Otherwise it will be assumed to be the next
                // Set it to the last sequence number and resend it. This causes the last data
                // to be sent again
                ackPacket.setSequence(motdPacket.getSequence());
                if (Helpers.bytesAreEqual(lastPacket.getBytes(), motdPacket.getBytes())) {
                    System.out.println("Client: Received duplicate. Discarding.");
                } else {
                    System.out.println("Client: Missed packet.");
                }
                socket.send(MOTDProtocol.createMOTDDatagram(ackPacket, serverIP, SERVER_PORT));

                continue;
            }

            lastPacket = motdPacket;
            currentSequence = MOTDProtocol.nextSequenceValue(currentSequence);
            stringBuilder.append(new String(motdPacket.getData()));

            if (DEBUG && Math.random() < 0.5)
                continue;
            ackPacket.setSequence(currentSequence);
            socket.send(MOTDProtocol.createMOTDDatagram(ackPacket, serverIP, SERVER_PORT));
        }
        System.out.println("Client: MOTD -> " + stringBuilder.toString());

        // Send the final ACK
        socket.send(MOTDProtocol.createMOTDDatagram(ackPacket, serverIP, SERVER_PORT));
    }
}
