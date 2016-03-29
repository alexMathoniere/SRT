import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.ByteBuffer;
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

        ZMQ.Socket publisher = context.createSocket(ZMQ.PUB);
        publisher.bind("tcp://127.0.0.1:5560");

        subscriber.subscribe("".getBytes());

        ZMsg msg = null;
        Long time = null;
        long timeLast = 0;
        int messageReceiveCount = 0;
        int totalMessages = 1;

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

                        byte[] bytes = frame.getData(); // gets full 19 bytes
                        Utils.LOG("Incoming buffer (size: "+bytes.length+" = " + new String(bytes));
                        //Utils.LOG(""+bytes[0]+""+bytes[1]+""+bytes[2]+""+bytes[3]+""+bytes[4]+""+bytes[5]+""+bytes[6]+""+bytes[7]+""+bytes[8]+""+bytes[9]);
                        //Utils.LOG(""+bytes.length);
                        byte[] zero = new byte[1];
                        zero[0] = 0;
                        ByteBuffer buffer = ByteBuffer.wrap(bytes);
                        Integer check = buffer.getInt();

                        Utils.LOG("Check = " + check + " MAX VALUE = " + Integer.MAX_VALUE);

                        if(!check.equals(Integer.MAX_VALUE)){
                            Utils.LOG("Non-Zero");
                            //Utils.LOG(""+bytes[0]+""+bytes[1]+""+bytes[2]+""+bytes[3]);


                            //ByteBuffer buffer = ByteBuffer.wrap(bytes);
                            //ByteBuffer buffer = ByteBuffer.allocate(Main.BUFFER_SIZE);


                            //buffer.put(bytes);
                            //Utils.LOG(""+bytes);

                            time = System.currentTimeMillis();
                            long receiveDelta = time - timeLast;
                            timeLast = time;
                            lastReceiveds.add(receiveDelta);
                            int now = time.intValue();
                            int then = check;
                            //int then = buffer.getInt();


                            Utils.LOG(" - Latency = " + (now - then-500));

                            latencies.add(now-then-500);

                            byte[] remainingStuff = new byte[buffer.remaining()];
                            Utils.LOG("RemainingStuff Size: " + remainingStuff.length);

                            //byte[] remainingStuff = new byte[Main.BUFFER_SIZE-4];
                            //byte[] remainingStuff = new byte[19];
                            //buffer.remaining()

                            buffer.get(remainingStuff);
                            Utils.LOG("Remaining stuff: "+remainingStuff);
                            String message = new String(remainingStuff);

                            if(message.substring(0,12).equals("MESSAGETOTAL")){
                                totalMessages = Integer.parseInt(message.substring(12,15));
                            }
                            if(message.equals("ENDMESSAGE     ")){
                                break;
                            }

                            //String message = msg.toString();
                            Utils.LOG(" - Subscriber: received message: " + message);
                            //System.out.println(StringParser.parse(message));
                            //Utils.LOG("Received message: " + msg.popString());

                            // check error completion rate: full message/some errors/no message
                            // receiver synchronization GRC stuff
                        }else{
                            Utils.LOG("Discarded message");

                            ZMsg badMessage = new ZMsg();
                            ByteBuffer buff = ByteBuffer.allocate(13);
                            String bad = "BADMESSAGE";
                            buff.put(bad.getBytes());

                            badMessage.add(buff.array());
                            badMessage.send(publisher);
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            //displayLatencies();
        }

        displayLatencies();
        Utils.LOG("Completion rate: "+latencies.size()/(1.0*totalMessages));
        subscriber.disconnect("tcp://localhost:5557");
        subscriber.close();

    }

    public void finish(){
        Utils.LOG("Interrupting Subscriber");
        this.interrupt();

    }

    public static void displayLatencies(){
        latencies.remove(0);
        lastReceiveds.remove(0);
        latencies.remove(latencies.size()-1);
        lastReceiveds.remove(lastReceiveds.size()-1);
        Utils.LOG("Latencies ("+latencies.size()+"): "+latencies.toString());
        Utils.LOG("Time Since Last Received: "+lastReceiveds.toString());
    }

}
