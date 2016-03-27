package com.aberdeenhs.test;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.net.Socket;
import java.nio.*;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;


public class SubscriberThread extends Thread{

    static ArrayList<Integer> latencies = new ArrayList<Integer>();
    static ArrayList<Long> lastReceiveds = new ArrayList<Long>();

    private ZMQ.Socket subscriber;
    byte id = 0;

    public SubscriberThread(byte id){
        this.id = id;
    }
    public void run(){

        Utils.LOG("Starting Subscriber");

        ZContext context = new ZContext();
        subscriber = context.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://127.0.0.1:5557");

        subscriber.subscribe("".getBytes());

        ZMsg msg = null;
        Long time = null;
        long timeLast = 0;
        int messageReceiveCount = 0;

        while(!Thread.currentThread().isInterrupted()){

            try {
                Utils.LOG("Subscriber: listening");
                msg = ZMsg.recvMsg(subscriber);
                //msg.popString();
                if (msg != null){
                    messageReceiveCount = messageReceiveCount + 1;
                    Utils.LOG(" - Subscriber: msg # " + messageReceiveCount + " received");
                ZFrame frame = msg.getFirst();
                    if(frame != null){

                        byte[] bytes = frame.getData(); // gets full 17 bytes

                        if(bytes[0]!=0){
                            ByteBuffer buffer = ByteBuffer.wrap(bytes);


                            time = System.currentTimeMillis();
                            long receiveDelta = time - timeLast;
                            timeLast = time;
                            lastReceiveds.add(receiveDelta);
                            int now = time.intValue();
                            int then = buffer.getInt();
                            Utils.LOG(" - Latency = " + (now - then));
                            latencies.add(now-then);

                            byte[] remainingStuff = new byte[Main.BUFFER_SIZE-4];

                            buffer.get(remainingStuff);
                            String message = new String(remainingStuff);

                            //String message = msg.toString();
                            Utils.LOG(" - Subscriber: received message: " + message);
                            //System.out.println(StringParser.parse(message));
                            //Utils.LOG("Received message: " + msg.popString());

                            // check error completion rate: full message/some errors/no message
                            // receiver synchronization GRC stuff
                        }
                    }
                }
            }catch(Exception e){

            }
        }

        subscriber.disconnect("tcp://localhost:5557");
        subscriber.close();

    }

    public void finish(){
        Utils.LOG("Interrupting Subscriber");
        this.interrupt();

    }

    public static void displayLatencies(){
        Utils.LOG("Latencies ("+latencies.size()+"): "+latencies.toString());
        Utils.LOG("Time Since Last Received: "+lastReceiveds.toString());
    }

}
