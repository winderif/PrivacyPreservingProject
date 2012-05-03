package util;

public class MessageBox {
    private String message;

    synchronized void messageIn(String msg) {
    	try {
            while( message != null ) {
                wait();
            }

            message = msg;

            notifyAll();

        }catch(Exception e) {
            System.err.println("messageIn:Error:"+e);
            System.exit(1);
        }
    }

    synchronized String messageOut() {
        try{
       	    while(message == null) {
                wait();
            }

            String s = message;
            message = null;

            notifyAll();
            return s;

        }catch(Exception e) {
        	System.err.println("messageOut:Error");
        	System.exit(1);
        	return "";
        }       
    }
}