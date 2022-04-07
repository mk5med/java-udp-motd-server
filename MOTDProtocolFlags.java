public class MOTDProtocolFlags {
  public static byte FLAG_TYPE_SYN = 0b01;
  public static byte FLAG_TYPE_ACK = 0b10;
  public static byte FLAG_TYPE_SYNACK = (byte) (FLAG_TYPE_SYN | FLAG_TYPE_ACK);
  public static byte FLAG_TYPE_REQUEST = 0b100;
  public static byte FLAG_TYPE_DATA = 0b1000;

  private MOTDProtocolFlags() {}
}