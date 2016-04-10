package com.aberdeenhs.test;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;


public class PublisherThread extends Thread {

    byte id = 0;

    public PublisherThread(byte id){
        this.id = id;
    }

    public void run() {

        Utils.LOG("Starting Publisher");

        ZContext context = new ZContext();
        ZMQ.Socket publisher = context.createSocket(ZMQ.PUB);
        publisher.bind("tcp://127.0.0.1:5558");

        int loopCount = 50;        // number
        int sleepTimer = 500;  // msec
//        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
//        String DateToStr = null;

        ZMsg sync3 = new ZMsg();
        ByteBuffer buff2 = ByteBuffer.allocate(Main.BUFFER_SIZE);
        buff2.putInt(Integer.MAX_VALUE);
        byte[] B2 = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        buff2.put(B2);

        sync3.add(buff2.array());
        sync3.send(publisher);
        Utils.LOG("Sending # of messages");


        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ZMsg numMessages = new ZMsg();

        ByteBuffer messageNumBuff = ByteBuffer.allocate(Main.BUFFER_SIZE); //sends number of messages to receiver for MCR calculations

        Long firstTime = System.currentTimeMillis(); // Long 64 bits (8 bytes)
        messageNumBuff.putInt(firstTime.intValue()); // int 32 bits (4 bytes)
        String firstMessage = ("MESSAGETOTAL"+((loopCount<10)?"00"+loopCount:((loopCount < 100)?"0"+loopCount:loopCount)));
        //Utils.LOG("msg length" + message.getBytes().length);
        messageNumBuff.put(firstMessage.getBytes());

        numMessages.add(messageNumBuff.array());
        numMessages.send(publisher);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < loopCount; i++) {
            //long time = System.currentTimeMillis();
            ZMsg sync = new ZMsg(); //SYNC

            ZMsg msg = new ZMsg();
            //Date currentDate = new Date();
            //DateToStr = format.format(currentDate);

//           /* msg.add("Q"); //smaller tag for the subscriber to look for
//            msg.add("Computer 1");  //get the sender
//            msg.add("%");
//            msg.add("Computer 2");  //get the receiver
//            msg.add("%");
//            msg.add(DateToStr);  //get the time
//            msg.add("%");
//            msg.add("This is data.");  //get the message*/

            //try and put this all in 20 bytes. Time is 8. Look at bitmasking/bitshifting
            //int ~ = (stuff on the left << size of stuff on right)|stuff on right
            // adding another one onto that ^^^, b = a|(new number<<spaces the previous numbers are taking up)
            //look up how to reverse this, though

            //could use google protocol buffers to categorize the packet data instead

            ByteBuffer buffer = ByteBuffer.allocate(Main.BUFFER_SIZE);

            Long time = System.currentTimeMillis(); // Long 64 bits (8 bytes)
            buffer.putInt(time.intValue()); // int 32 bits (4 bytes)
            //buffer.putInt(Integer.MAX_VALUE);




//            if (!initializeTime) {
//                buffer.putLong(System.currentTimeMillis());
//                initializeTime = true;
//            } else {


//                String newTime = convertTime(DateToStr);
//                //buffer.
//                int a = (Integer.parseInt(newTime) << 1) | 1;
//                int b = a | (2 << newTime.length() + 1);

                //int b = (((Integer.parseInt(newTime) << 1)|1) << 1)|2;



                //String message = "data: "+ ((i<10)?"00"+i:((i < 100)?"0"+i:i));
                String message = ("This is data"+((i<10)?"00"+i:((i < 100)?"0"+i:i)));
                //Utils.LOG("msg length" + message.getBytes().length);
                buffer.put(message.getBytes());
                Utils.LOG("Sending Message # " + i) ;//+ ": "+ new String(buffer.array()));

                //Utils.LOG("Message: " + message);
                //Utils.LOG("Bitshift 1: " + a);
                //Utils.LOG("Bitshift 2: " + b);

                // String message = "Computer 1%Computer 2%" + DateToStr + "%This is data";
                // System.out.printf("Message was %d bytes\n", message.getBytes().length);




//                msg.add();
                msg.add(buffer.array());

             //SYNC
            //byte[] B = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            ByteBuffer buff = ByteBuffer.allocate(Main.BUFFER_SIZE);
            buff.putInt(Integer.MAX_VALUE);
            //byte[] B = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
            byte[] B = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
            buff.put(B);

            sync.add(buff.array());
            sync.send(publisher); //SYNC

            // MESSAGE
           // byte[] A = {1,2,3,4,5,6,7,8,9,10};
           // msg.add(A);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
                msg.send(publisher);
                //Utils.LOG(String.format("Publisher: sent message %d", i));

            try {
                Thread.sleep(sleepTimer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            }
        }
        ZMsg sync2 = new ZMsg();
        ByteBuffer buff = ByteBuffer.allocate(Main.BUFFER_SIZE);
        buff.putInt(Integer.MAX_VALUE);
        byte[] B = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,};
        buff.put(B);

        sync2.add(buff.array());
        sync2.send(publisher);
        Utils.LOG("Final sync");

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ZMsg terminate = new ZMsg();

        ByteBuffer terminateBuffer = ByteBuffer.allocate(Main.BUFFER_SIZE);

        Long time = System.currentTimeMillis(); // Long 64 bits (8 bytes)
        terminateBuffer.putInt(time.intValue()); // int 32 bits (4 bytes)
        String message = ("ENDMESSAGE     ");
        //Utils.LOG("msg length" + message.getBytes().length);
        terminateBuffer.put(message.getBytes());

        terminate.add(terminateBuffer.array());

        Utils.LOG("Terminate message");
        terminate.send(publisher);

        //SubscriberThread.displayLatencies();

        publisher.unbind("tcp://localhost:5558");
        publisher.close();
    }

//    public String convertTime(String time) {
//        int y = 0;
//        String date = "";
//        String hours = "";
//
//        for (int i = 0; i < time.length(); i++) {
//
//            if (time.charAt(i) == '/' || time.charAt(i) == ' ') {
//            }
//            if (time.charAt(i) == ':') {
//                y++;
//            } else {
//                if (y == 0) {
//                    date += time.charAt(i);
//                }
//                if (y > 0) {
//                    hours += time.charAt(i);
//
//                }
//            }
//            return date + "" + hours;
//        }
//        return "";
//    }

}
