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

    private ZMQ.Socket subscriber;
    byte id = 0;

    public SubscriberThread(byte id){
        this.id = id;
    }
    public void run(){

        Utils.LOG("Starting Subscriber");

        ZContext context = new ZContext();
        subscriber = context.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://127.0.0.1:5560");

        subscriber.subscribe("".getBytes());

        ZMsg msg = null;
        Long time = null;
        long timeLast = 0;
        int messageReceiveCount = 0;

        while(!Thread.currentThread().isInterrupted()){

            try {
                msg = ZMsg.recvMsg(subscriber);
                if (msg != null){
                    ZFrame frame = msg.getFirst();
                    if(frame != null){

                        byte[] bytes = frame.getData(); // gets full 17 bytes

                        if(bytes[0]!=0){
                            ByteBuffer buffer = ByteBuffer.wrap(bytes);

                            byte[] remainingStuff = new byte[13];

                            buffer.get(remainingStuff);
                            String message = new String(remainingStuff);

                            if(message.equals("BADMESSAGE")){
                                Utils.LOG("Publisher not working right");
                            }
                        }
                    }
                }
            }catch(Exception e){

            }
        }

        subscriber.disconnect("tcp://localhost:5560");
        subscriber.close();

    }

    public void finish(){
        Utils.LOG("Interrupting Subscriber");
        this.interrupt();

    }

}
