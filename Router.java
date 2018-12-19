import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;

public class Router extends Node implements Runnable {

    static String portLeft;
    static String portRight;
    static int id;

    static final int SIZE = 5;
    static final int START_DEST = 0;
    static final int START_ROUTERNO = 9;
    static final int START_ROUTEPORT = 13;

    static final int END_OF_HEADER = 20;

    static final String DEFAULT_DST_NODE = "localhost";

    DatagramPacket currentPacket;

    InetSocketAddress dstAddress;
    HashMap<String, String[]> table = new HashMap<>();

    Router(String portLeft, String portRight, int id, int srcPort, int hostPort) {
        this.portLeft = portLeft;
        this.portRight = portRight;
        this.id = id;

        dstAddress = new InetSocketAddress(DEFAULT_DST_NODE, hostPort);
        try {
            socket = new DatagramSocket(srcPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        listener.go();
        //dest in 0 out 1
    }

    //dest src routerno portnoofrin/out
    @Override
    public void onReceipt(DatagramPacket packet) {
        byte[] data = packet.getData();
        String dest = getInfo(data, START_DEST);

        //need to contact controller
        if (packet.getSocketAddress() != dstAddress && table.get(dest) == null) {
            currentPacket = packet;
            byte[] header = getHeader(data);
            String[] inOut = new String[2];
            inOut[0] = portLeft;
            inOut[1] = null;
            table.put(dest, inOut);
            DatagramPacket question = new DatagramPacket(header, header.length, dstAddress);
            try {
                socket.send(question);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //reply from controller
        else if (packet.getSocketAddress() == dstAddress) {
            String nextPort = getInfo(data, START_ROUTEPORT);
            String[] inOut = table.get(dest);
            inOut[1] = nextPort;
            //complete table for this route
            table.put(dest, inOut);
        }
        //already traversed this route before
        else {
            byte[] fullData = currentPacket.getData();
            String nextPort = table.get(dest)[1];
            fullData = setHeader(fullData, nextPort);
            InetSocketAddress nextRoute = new InetSocketAddress(Integer.valueOf(nextPort));
            DatagramPacket passOn = new DatagramPacket(fullData, fullData.length, nextRoute);
            try {
                socket.send(passOn);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static byte[] getHeader(byte[] data) {
        byte[] header = new byte[END_OF_HEADER];
        for (int i = 0; i < END_OF_HEADER; i++) {
            header[i] = data[i];
        }
        return header;
    }

    public static byte[] setHeader(byte[] data, String replace) {
        byte[] tmp = replace.getBytes();
        for (int i = 0; i < END_OF_HEADER; i++) {
            data[i] = tmp[i];
        }
        return data;
    }

    public static byte[] setInfo(byte[] returnPacket, int startNumber, String info) {
        byte[] msg = info.getBytes();
        int counter = 0;
        for (int i = startNumber; i < startNumber + SIZE; i++) {
            returnPacket[counter] = msg[i];
            counter++;
        }
        return returnPacket;
    }

    public static String getInfo(byte[] packetData, int startNumber) {
        byte[] tmp = new byte[SIZE];
        int counter = 0;
        for (int i = startNumber; i < startNumber + SIZE; i++) {
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

