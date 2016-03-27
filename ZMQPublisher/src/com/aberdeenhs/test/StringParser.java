package com.aberdeenhs.test;

public class StringParser {

    public static String parse(String full){
        int x = 0;
        String sender = "";
        String recip = "";
        String time = "";
        String message = "";

        for(int i = 0; i < full.length(); i++){

            if(full.charAt(i) == '%'){
                x++;
            }
            else{
                if(x==0){
                    sender = sender + full.charAt(i);
                }
                if(x==1){
                    recip = recip + full.charAt(i);
                }
                if(x==2){
                    time = time + full.charAt(i);
                }
                if(x==3){
                    message = message + full.charAt(i);
                }
            }
        }
        return "Sender: "+sender+"\nRecipient: "+recip+"\nTime: "+time+"\nMessage: "+message;
    }

}
