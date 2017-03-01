package net.sf.sockettest;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.sockettest.swing.Arduino;
import net.sf.sockettest.swing.SocketTestClient;
/**
 *
 * @author Djimgou Patrick Virgile
 */
public class SocketClientArduino extends Thread {
    
    private SocketClientArduino socketClient=null;
    private Socket socket=null;
    private Arduino parent;
    private BufferedInputStream in;
    private boolean desonnected=false;
    private boolean Pause_Send = false;
    String Initial_massage = "init:ArduinoName";
    private String IPAdresse = "null";
    
    public synchronized void setDesonnected(boolean cr) {
        desonnected=cr;
    }
    public synchronized void setPauseSend(boolean status){
      Pause_Send = status;
    
    }
    
    private SocketClientArduino(Arduino parent, Socket s) {
        super("SocketClient");
        this.parent = parent;
        socket=s;
        setDesonnected(false);
        start();
    }
    
    public SocketClientArduino handleConnection(Arduino parent, Socket s) {
        System.out.println("Socket : "+ s.getInetAddress().toString());
//        if(socketClient==null)
//            socketClient=new SocketClientArduino(parent, s);
//        else {
//            if(socketClient.socket!=null) {
//                try	{
//                    socketClient.socket.close();
//                } catch (Exception e)	{
//                    parent.error(e.getMessage());
//                }
//            }
//            socketClient.socket=null;
//            socketClient=new SocketClientArduino(parent,s);
//        }
//        System.out.println("Return of new Socket Client");
        return socketClient;
    }
    
    public void run() {
        System.out.println("Enter in RUn Method");
        InputStream is = null;
        try {
            is = socket.getInputStream();
            in = new BufferedInputStream(is);
        } catch(IOException e) {
            try {
                socket.close();
            } catch(IOException e2) {
                System.err.println("Socket not closed :"+e2);
            }
            parent.error("Could not open socket : "+e.getMessage());
            parent.disconnect();
            return;
        }
        
        //   
        // start von Thread Filter 
        FakeMeldungen Boucle =  new FakeMeldungen();
        Thread Boucle_Thread = new Thread(Boucle);
        Boucle_Thread.start();
        
        while(!desonnected) {
            try {
                String got = readInputStream(in); //in.readLine();
                if(got==null) {
                    //parent.error("Connection closed by client");
                    parent.disconnect();
                    break;
                }
                
                // parse the Response 
                Parse_Reponse(got);
                
                //got = got.replaceAll("\n","<LF>");
                //got = got.replaceAll("\r","<CR>");
                //parent.append("R: "+got);
                parent.appendnoNewLine(got);
            } catch(IOException e) {
                if(!desonnected) {
                    parent.error(e.getMessage(),"Connection lost");
                    parent.disconnect();
                }
                break;
            }
        }   //end of while
        
        try	{
            is.close();
            in.close();
            //socket.close();
        } catch (Exception err) {}
        socket=null;
    }//end of run

    
    private  String readInputStream(BufferedInputStream _in) throws IOException {
        String data = "";
        int s = _in.read();
        if(s==-1)
            return null;
        data += ""+(char)s;
        int len = _in.available();
        System.out.println("Len got : "+len);
        if(len > 0) {
            byte[] byteData = new byte[len];
            _in.read(byteData);
            data += new String(byteData);
        }
        return data;
    }
    
    // this Function muss be  started in seüarate Thread 
    private void Parse_Reponse(String Data_in){
    
           
    
    }
    
    
  public class FakeMeldungen implements Runnable {
		/**
		 * The socket connected to the client.
		 */
      // private final Queue<Message> queue;
       
      
      
       private  volatile String von_sensor = "echo" ;  
       private  int count = 0 ;
       String SendTemp = "temp<br>" + Integer.toString(78) + "<br>" + "0";
      
       private boolean suspended = false;

		public FakeMeldungen() {
			                        
		}

		public void run() {
                        try {
                             while (!desonnected) {
                                   //  Senden von Fake Meldung an den Server 
                                   //  with timer  1 Second  for example 
                                   Thread.sleep(3000);
                                   // Üause automatische Senden  zu dem Server
                                   if(!Pause_Send){
                                       // System.out.println(" thread Fake Meldung ");
                                   parent.sendMessage(SendTemp);
                                   
                                   }
                                   
                                   
                                 }
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                       
            
                 }// end 

               
	
  } // end  Classe
    
} // End Thread Fake While
  

} // End Klasse
