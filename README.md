# udp-motd-server
An experimental MOTD server over UDP with a UDP version of TCP handshake mechanism.

## Try it on your machine
1. Clone the repository
2. Run `javac *.java`
3. Open a new terminal session (or TMUX) and run `java MOTDServer`
4. Run `java MOTDClient` to see the communication

**Expected result**
```
$ java MOTDServer
MOTDServer: Ready to connect.
Handshake: Waiting
Handshake: Complete
MOTDServer: Starting to send MOTD.
MOTDServer: Ready to connect.
Handshake: Waiting
```
```
$ java MOTDClient
Client: MOTD -> Good day. It has been 86400000s since the server started.
```
## Protocol
The MOTD server and a MOTD Client communicate with this packet format.
```
A protocol for the MOTD server.
This protocol establishes a connection over UDP and
sends packets of data in 16 byte blocks.

---------------------------
| t | s | data            |
---------------------------

t = 1 byte. type of the transmission
  public static byte FLAG_TYPE_SYN = 0b01;
  public static byte FLAG_TYPE_ACK = 0b10;
  public static byte FLAG_TYPE_SYNACK = (byte) (FLAG_TYPE_SYN | FLAG_TYPE_ACK) = 0b11;
  public static byte FLAG_TYPE_REQUEST = 0b100;
  public static byte FLAG_TYPE_DATA = 0b1000;
  public static byte FLAG_TYPE_FIN = 0b10000;
s = 1 byte. sequence number. Can be 1 or 0
data = 16 bytes.
```