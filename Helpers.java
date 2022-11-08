import java.net.InetAddress;

public class Helpers {
  public static String bytesToString(byte[] data) {
    return new String(data);
  }

  public static boolean bytesAreEqual(byte[] a, byte[] b) {
    if (a.length != b.length) return false;
    for(int i = 0; i < a.length; i++) {
      if(a[i] != b[i]) return false;
    }
    return true;
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
