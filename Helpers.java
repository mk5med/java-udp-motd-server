import java.net.InetAddress;

public class Helpers {
  public static String bytesToString(byte[] data) {
    return new String(data);
  }

  public static class Session {
    public InetAddress dAddress;
    public int dPort;

    public Session(InetAddress address, int port) {
      this.dAddress = address;
      this.dPort = port;
    }
  }
}
