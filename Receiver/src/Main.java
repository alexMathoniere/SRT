/**
 * Created by alex on 12/3/15.
 */
public class Main {

   public static final int BUFFER_SIZE = 19;


   public static void main(String[] args){


    SubscriberThread subscriberThread = new SubscriberThread((byte)1);
    subscriberThread.start();


       try {
           subscriberThread.join();
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
   }




//   static class Foo{
//
//       private String name;
//
//       Foo(String name) {
//           this.name = name;
//       }
//
//       public String getName() {
//           return name;
//       }
//
//       public void setName(String name) {
//           this.name = name;
//       }
//
//       public static void printFooObject(Foo f){
//           System.out.printf("This Foo's name is %s\n", f.getName());
//       }
//
//   }



}
