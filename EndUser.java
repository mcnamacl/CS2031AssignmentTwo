import java.io.IOException;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.*;

public class EndUser extends Node implements Runnable {
    static final String DEFAULT_DST_NODE = "localhost";

    static final int SIZE = 20;

    InetSocketAddress routerAddress;
    int destPort;

    EndUser(int routerPort, int srcPort, int destPort) {
        try {
            routerAddress = new InetSocketAddress(DEFAULT_DST_NODE, routerPort);
            socket = new DatagramSocket(srcPort);
            this.destPort = destPort;
            listener.go();
            inputListener.start();
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void onReceipt(DatagramPacket packet) {
        byte[] data = packet.getData();
        System.out.println(data.toString());
    }

    @Override
    public void userInput(String message) {
        if (message.equals(0)){
            System.out.println("What message would you like to send?");
            Scanner input = new Scanner(System.in);
            if (input.hasNext()){
                String text = input.next();
                byte[] msg = new byte[SIZE + text.length()];


                DatagramPacket output = new DatagramPacket(msg, msg.length, routerAddress);
            }
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

    public void subToNewTopic(byte[] topicBytes) {
        topics.add(new String(topicBytes));
        byte[] buffer = new byte[DATA_BEGIN_POS + topicBytes.length + 1];
        buffer[TYPE_OF_PACKET_POS] = (byte) SUB;
        buffer[TOPIC_LENGTH_POS] = (byte) topicBytes.length;
        for (int i = 0; i < topicBytes.length; i++) {
            buffer[DATA_BEGIN_POS + i + 1] = topicBytes[i];
        }
        System.out.println("Sending packet...");
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, dstAddress);
        try {
            socket.send(packet);
            System.out.println("Packet has been sent.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void run() {
        DatagramPacket packet;
        byte[] topicBytes = topics.get(0).getBytes();
        byte[] buffer = new byte[DATA_BEGIN_POS + topicBytes.length + 1];
        buffer[TYPE_OF_PACKET_POS] = (byte) SUB;
        buffer[TOPIC_LENGTH_POS] = (byte) topicBytes.length;
        for (int i = 0; i < topicBytes.length; i++) {
            buffer[DATA_BEGIN_POS + i + 1] = topicBytes[i];
        }
        System.out.println("Sending packet...");
        packet = new DatagramPacket(buffer, buffer.length, dstAddress);
        try {
            socket.send(packet);
            this.wait();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws BindException {
        System.out.println("What topic do you want to subscribe to? Press 1 to unsubscribe to a topic, press 2 to subscribe to another topic, press " +
                "3 to see all topics you are currently subscribed to.");
        Scanner input = new Scanner(System.in);
        if (input.hasNext()) {
            String topic = input.next();
            Thread sub = new Thread(new EndUser(DEFAULT_DST_NODE, DEFAULT_DST_PORT, DEFAULT_SRC_PORT, topic));
            sub.start();
        }
    }
}