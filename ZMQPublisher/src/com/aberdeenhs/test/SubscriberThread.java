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

    //private ZMQ.Socket subscriber;
    byte id = 0;

    public SubscriberThread(byte id){
        this.id = id;
    }
    public void run(){

        Utils.LOG("Starting Subscriber");

        ZContext context = new ZContext();
        ZMQ.Socket subscriber = context.createSocket(ZMQ.SUB);
        subscriber.connect("tcp://127.0.0.1:5561");

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
                        int freqID = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        Utils.LOG("frequency ID: "+freqID);
                    }
                }
            }catch(Exception e){

            }
        }

        subscriber.disconnect("tcp://127.0.0.1:5560");
        subscriber.close();

    }

    public void finish(){
        Utils.LOG("Interrupting Subscriber");
        this.interrupt();

    }

}
