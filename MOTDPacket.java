import java.net.DatagramPacket;
import java.net.InetAddress;

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

  public void setData(byte[] newData) { 
    
    for(int i = 2; i < this.data.length; i++) {
      if (i-2 >= newData.length) {
        // Fill the remainder of the data with 0
        this.data[i] = 0;
        continue;
      }

      this.data[i] = newData[i-2];
    }
  }
  public void setBytes(byte[] bytes) { this.data = bytes; }
  public byte[] getBytes() { return this.data; }
  
  public byte[] getData() {
    byte[] innerData = new byte[16];
    for(int i = 2; i < data.length; i++) {
      innerData[i-2] = data[i];
    }
    
    return innerData;
  }
  public void setSequence(byte value) { this.data[1] = value;}
  public byte getSequence() { return this.data[1]; }

  public void setType(byte type) { this.data[0] = type;}
  public int getType() { return this.data[0]; }

  public static MOTDPacket fromPacket(DatagramPacket datagramPacket) {
    // if (data.length != 16) throw new RuntimeException("input data is larger than 16 bytes");
    byte[] data = datagramPacket.getData();
    MOTDPacket packet = new MOTDPacket(data[0]);
    packet.address = datagramPacket.getAddress();
    packet.port = datagramPacket.getPort();

    if(data[0] == MOTDProtocolFlags.FLAG_TYPE_DATA) packet.setBytes(data);
    return packet;
  }
}