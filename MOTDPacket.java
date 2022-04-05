import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 0-4: type
 * 4-20: data
 * 
 */

public class MOTDPacket {

  private byte[] data = new byte[16 + 1 + 1];

  public InetAddress address;
  public int port;

  public MOTDPacket(byte type) {
    this.data[0] = type;
  }

  public MOTDPacket(byte[] data) {
    this(MOTDProtocolFlags.FLAG_TYPE_DATA);
    this.setData(data);
  }

  public void setData(byte[] data) { 
    for(int i = 2; i < 18; i++) {
      if (i-1 >= data.length) break;
      this.data[i] = data[i-2];
    }
  }
  public byte[] getData() { return this.data; }

  public void setSequence(byte value) { this.data[1] = value;}
  public int getSequence() { return this.data[1]; }

  public void setType(byte type) { this.data[0] = type;}
  public int getType() { return this.data[0]; }

  public static MOTDPacket fromPacket(DatagramPacket datagramPacket) {
    // if (data.length != 16) throw new RuntimeException("input data is larger than 16 bytes");
    byte[] data = datagramPacket.getData();
    MOTDPacket packet = new MOTDPacket(data[0]);
    packet.address = datagramPacket.getAddress();
    packet.port = datagramPacket.getPort();

    if(data[0] == MOTDProtocolFlags.FLAG_TYPE_DATA) packet.setData(data);
    return packet;
  }

  public final static class MOTDProtocolFlags {
    public static byte FLAG_TYPE_SYN = 0x00;
    public static byte FLAG_TYPE_SYNACK = 0x01;
    public static byte FLAG_TYPE_REQUEST = 0x02;
    public static byte FLAG_TYPE_DATA = 0x03;
  }
}