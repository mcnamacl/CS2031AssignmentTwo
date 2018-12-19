import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Controller extends Node implements Runnable {

    static final int DEFAULT_PORT = 50000;
    static final int START_DEST = 0;
    static final int START_SRC = 4;
    static final int START_ROUTERNO = 9;
    static final int START_ROUTEPORT = 13;
    static final int SIZE = 5;


    HashMap<String, HashMap<String, HashMap<String, String[]>>> table;

    Controller(HashMap<String, HashMap<String, HashMap<String, String[]>>> table, int port) {
        try {
            this.table = table;
            socket = new DatagramSocket(port);
            listener.go();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        HashMap<String, HashMap<String, HashMap<String, String[]>>> table = createTable();
        Thread controller = new Thread(new Controller(table, DEFAULT_PORT));
        controller.start();
    }

    public static HashMap<String, HashMap<String, HashMap<String, String[]>>> createTable() {
        //dest, src, router no, in, out
        //000 001, 100 101 110
        HashMap<String, HashMap<String, HashMap<String, String[]>>> table = new HashMap<>();
        HashMap<String, HashMap<String, String[]>> userMap = new HashMap<>();
        HashMap<String, String[]> routerMap = new HashMap<>();

        //come back and add extra user
        table = configure(table, "50001", "50002", "00100", "49998", routerMap, userMap, "49999");
        table = configure(table, "50002", "50003", "00101", "49998", routerMap, userMap, "49999");
        table = configure(table, "50003", "50004", "00110", "49998", routerMap, userMap, "49999");
        return table;
    }

    public static HashMap<String, HashMap<String, HashMap<String, String[]>>> configure(HashMap<String, HashMap<String, HashMap<String, String[]>>> table, String in, String out, String router, String user, HashMap<String, String[]> routerMap, HashMap<String, HashMap<String, String[]>> userMap, String otherUser) {

        String[] inOut = new String[2];

        inOut[0] = in;
        inOut[1] = out;

        routerMap.put(router, inOut);
        userMap.put(user, routerMap);
        table.put(otherUser, userMap);

        return table;
    }

    //
    //dest src routerno portnoofrin/out
    //3    3    3        3
    //001 000 100 011/100
    @Override
    public void onReceipt(DatagramPacket packet) {
        byte[] data = packet.getData();
        String dest = getInfo(data, START_DEST);
        String source = getInfo(data, START_SRC);
        String routerNo = getInfo(data, START_ROUTERNO);

        String out;

        if (Integer.valueOf(dest) > Integer.valueOf(source)){
            HashMap<String, HashMap<String, String[]>> sourceMap = table.get(dest);
            HashMap<String, String[]> routerMap = sourceMap.get(source);
            out = routerMap.get(routerNo)[1];
        } else {
            HashMap<String, HashMap<String, String[]>> sourceMap = table.get(source);
            HashMap<String, String[]> routerMap = sourceMap.get(dest);
            out = routerMap.get(routerNo)[0];
        }

        byte[] returnPacketHeader = new byte[data.length];

        returnPacketHeader = setInfo(returnPacketHeader, START_DEST, dest);
        returnPacketHeader = setInfo(returnPacketHeader, START_SRC, source);
        returnPacketHeader = setInfo(returnPacketHeader, START_ROUTERNO, routerNo);
        returnPacketHeader = setInfo(returnPacketHeader, START_ROUTEPORT, out);

        DatagramPacket returnPacket = new DatagramPacket(returnPacketHeader, returnPacketHeader.length);
        returnPacket.setSocketAddress(packet.getSocketAddress());
        try {
            socket.send(returnPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void run() {
        System.out.println("Waiting for contact");
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void userInput(String message) {
    }
}
