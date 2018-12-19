import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;

public class Router extends Node implements Runnable {

    static int portLeft;
    static int portRight;
    static int id;

    static final int SIZE = 5;
    static final int START_DEST = 0;
    static final int START_ROUTERNO = 9;
    static final int START_ROUTEPORT = 13;

    static final int END_OF_HEADER = 20;

    InetSocketAddress dstAddress;
    HashMap<String, String[]> table = new HashMap<>();

    Router(int portLeft, int portRight, int id, int srcPort, int hostPort, String host){
        this.portLeft = portLeft;
        this.portRight = portRight;
        this.id = id;

        dstAddress = new InetSocketAddress(host, hostPort);
        try {
            socket = new DatagramSocket(srcPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        listener.go();
        //dest in 0 out 1
    }

    @Override
    public void onReceipt(DatagramPacket packet) {
        if (packet.getSocketAddress() != dstAddress) {
            byte[] data = packet.getData();
            String dest = getInfo(data, START_DEST);

            if (table.get(dest) == null) {
                byte[] header = getHeader(data);

            }
        } else {

        }
    }

    public static

    public static byte[] getHeader(byte[] data){
        byte[] header = new byte[END_OF_HEADER];
        for (int i = 0; i < END_OF_HEADER; i++) {
            header[i] = data[i];
        }
        return header;
    }

    public static byte[] setInfo(byte[] returnPacket, int startNumber, String info){
        byte[] msg = info.getBytes();
        int counter = 0;
        for (int i = startNumber; i < startNumber + SIZE; i++){
            returnPacket[counter] = msg[i];
            counter++;
        }
        return returnPacket;
    }

    public static String getInfo(byte[] packetData, int startNumber){
        byte[] tmp = new byte[SIZE];
        int counter = 0;
        for(int i = startNumber; i < startNumber + SIZE; i++){
            tmp[counter] = packetData[i];
            counter++;
        }
        return new String(tmp);
    }

    @Override
    public void userInput(String message) {

    }

    @Override
    public void run() {

    }
}

