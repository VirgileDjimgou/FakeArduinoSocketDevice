/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sockettest.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.net.*;
import java.io.*;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.*;
import javax.net.ssl.*;
import javax.swing.JOptionPane;

import net.sf.sockettest.*;

/**
 *
 * @author virgile
 */
public class Arduino extends javax.swing.JPanel {

    /**
     * Creates new form Arduino
     */
    // sen Status
    String Status_Init = "status<br>Initialisierung";
    String Status_Bereit = "status<br>Bereit";
    String Status_Versuch = "status<br>Versuch";
    String Status_SonderMessunge = "status<br>Sondermessung";
    String Status_LiestDaten = "status<br>Liest Daten";
    
    
    
    private final String NEW_LINE = "\r\n";
    private Socket socket;
    // private SocketClientArduino  SocketClientArduino ;
    private boolean isSecure = false;
    private PrintWriter out;

    private BufferedInputStream in;
    private InputStream is = null;
    
    String Initial_massage = "init:";
    String status_message = ""+"<br>"+"";
    String SendTemp = "temp<br>" + Integer.toString(78) + "<br>" + "0";
    String Initialisierung  = "status<br>Initialisierung";
    private boolean Pause_Send = false;
    private volatile boolean desonnected_output=false;
    private volatile boolean desonnected_input=false;
    
    public Arduino(String ArduinoName) {
        initComponents();
        // Initialisierung mit Device NAme
        DeviceName.setText(ArduinoName);
       
    }
    
    public String getDeviceName(){
    
           return DeviceName.getText();
            }
    
     /////////////////
    //action methods
    //////////////////
    private void connect() {
        if(socket!=null) {
            System.out.println("Disconnecting  Socket ");
            disconnect();
            return;
        }
        String ip=ipField.getText();
        String port=portField.getText();
        if(ip==null || ip.equals("")) {
            JOptionPane.showMessageDialog(Arduino.this,
                    "No IP Address. Please enter IP Address",
                    "Error connecting", JOptionPane.ERROR_MESSAGE);
            ipField.requestFocus();
            ipField.selectAll();
            return;
        }
        if(port==null || port.equals("")) {
            JOptionPane.showMessageDialog(Arduino.this,
                    "No Port number. Please enter Port number",
                    "Error connecting", JOptionPane.ERROR_MESSAGE);
            portField.requestFocus();
            portField.selectAll();
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(!Util.checkHost(ip)) {
            JOptionPane.showMessageDialog(Arduino.this,
                    "Bad IP Address",
                    "Error connecting", JOptionPane.ERROR_MESSAGE);
            ipField.requestFocus();
            ipField.selectAll();
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        int portNo = 0;
        try	{
            portNo=Integer.parseInt(port);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(Arduino.this,
                    "Bad Port number. Please enter Port number",
                    "Error connecting", JOptionPane.ERROR_MESSAGE);
            portField.requestFocus();
            portField.selectAll();
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        try {
            if(isSecure==false) {
                System.out.println("Connectig in normal mode : "+ip+":"+portNo);
                socket = new Socket(ip,portNo);
            } else {
                System.out.println("Connectig in secure mode : "+ip+":"+portNo);
                //SocketFactory factory = SSLSocketFactory.getDefault();
				
				TrustManager[] tm = new TrustManager[] { new MyTrustManager(Arduino.this) }; 

                SSLContext context = SSLContext.getInstance("TLS");
                context.init(new KeyManager[0], tm, new SecureRandom());

                SSLSocketFactory factory = context.getSocketFactory();
                socket = factory.createSocket(ip,portNo);
            }
            
            ipField.setEditable(false);
            portField.setEditable(false);
            connectButton.setText("Disconnect");
            connectButton.setMnemonic('D');
            connectButton.setToolTipText("Stop Connection");
            sendButton.setEnabled(true);
            sendField.setEditable(true);
           
             DeviceName.setEditable(false);
            
            
        } catch (Exception e) {
			e.printStackTrace();
            error(e.getMessage(), "Opening connection");
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return;
        }
        
        System.err.println("Start Handle Connection ...  ");
        
        Status.setText(" "+socket.getInetAddress().getHostName()+
                " ["+socket.getInetAddress().getHostAddress()+"] ");
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        messagesField.setText("");
        
          //   
                        // start Thread Output zu den  Remote Server
                        OutputMeldungen Boucle =  new OutputMeldungen();
                        Thread Boucle_Thread = new Thread(Boucle);
                        Boucle_Thread.start();
                        
                        
                        // Start Thread Input von den Server 
                        InputMeldungen BoucleInput =  new InputMeldungen();
                        Thread Boucle_Thread_Input = new Thread(BoucleInput);
                        Boucle_Thread_Input.start();
//        
//        try {
//        SocketClientArduino = SocketClientArduino .handleConnection(this,socket);
//       
//        }catch(Exception ex){
//            ex.printStackTrace();
//            
//        }
        
         sendField.requestFocus();
    }
        
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
    
    public class OutputMeldungen implements Runnable {

       private  int count = 0 ;
       String SendTemp = "temp<br>" + Integer.toString(78) + "<br>" + "0";
       
       
       
       private boolean suspended = false;

		public OutputMeldungen() {
			                        
		}

		public void run() {
                    System.out.println("Enter in Out Put Methode ");
                    
                       try {
                              out = new PrintWriter(new BufferedWriter(
                               new OutputStreamWriter(socket.getOutputStream())), true);
                        } catch (IOException ex) {
                       Logger.getLogger(Arduino.class.getName()).log(Level.SEVERE, null, ex);
                         }
                    
           
                      // Send Initial Arduino     
                      sendMessage(Initial_massage + DeviceName.getText());
                      IPvirtualDevice.setText(socket.getInetAddress().toString());
                      Zustand.setBackground(Color.GREEN);
                      Zustand.setText("Zustand : Connected ");
                      
                        
                     
                        try {
                             while (!desonnected_output) {
                                   //  Senden von Fake Meldung an den Server 
                                   //  with timer  1 Second  for example 
                                   Thread.sleep(3000);
                                   // Üause automatische Senden  zu dem Server
                                   if(!Pause_Send){
                                       // System.out.println(" thread Fake Meldung ");
                                   sendMessage(SendTemp);
                                   
                                   }
                                   
                                   
                                 }
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        
                       // Ausschalten von Output Streaming 
                       
                       
            
                 }// end Catch 

   System.out.println("Aus von der output Stream ");      
   desonnected_output=false;
	
  } // end  Classe
  
    
} // End Thread Fake While
    
public synchronized void AuschaltenSocket(){
    


    // Auschalten Streaming  Input 
    
            try	{
                is.close();
                in.close();
                //socket.close();
            } catch (Exception err) {
               err.printStackTrace();
            }
   
     
    // Auschalten Streaming Output
                try {
                   
                        out.close();

                }
                 catch(Exception ex )
                         {
                            ex.printStackTrace();
                         }
                
     // und Endlich Auschlaten Socket  und Bereitstelle  fur eine neu Aus#fbau
      socket=null;
      Status.setText("");


} 
    
public class InputMeldungen implements Runnable {
       String SendTemp = "temp<br>" + Integer.toString(78) + "<br>" + "0";
             
       private boolean suspended = false;

		public InputMeldungen() {
			                        
		}
                public void run() {
                        System.out.println("Enter in Run  InputMeldung Method");
                        
                        try {
                            is = socket.getInputStream();
                            in = new BufferedInputStream(is);
                        } catch(IOException e) {
                            try {
                                socket.close();
                            } catch(IOException e2) {
                                System.err.println("Socket not closed :"+e2);
                            }
                            error("Could not open socket : "+e.getMessage());
                            disconnect();
                            return;
                        }

                      

                        while(!desonnected_input) {
                            try {
                                String got = readInputStream(in); //in.readLine();
                               // int readCount = readInputStream(in, got, 6000);  
                               // 6 second timeout
                                if(got==null) {
                                    //parent.error("Connection closed by client");
                                    disconnect();
                                    break;
                                }
                                
                                //got = got.replaceAll("\n","<LF>");
                                //got = got.replaceAll("\r","<CR>");
                                //parent.append("R: "+got);
                                appendnoNewLine(got);
                                // parse the Response 
                                Thread.sleep(2000);
                                InputParser(got);
                            } catch(IOException e) {
                                if(!desonnected_input) {
                                    error(e.getMessage(),"Connection lost");
                                    disconnect();
                                }
                                break;
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Arduino.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }   //end of while
                        
        System.out.println("Aus von der Input Stream ");  
        desonnected_input=false;

    }//end of run
                

  } // end  Classe
    
    public void  InputParser(String InputMessage){
        
        // Fake Ruckmeldung
        sendMessage("n");
        
        
        
        // Fake Rucmeldung .... wenn es um eine Versuch geht !!!!
        // sendMessage(this.Status_Init);
        // sendMessage(this.Status_Versuch);
        
//        
//        // Fake Ruckmeldung ...wenn es um eine Sondermessung geht
//        sendMessage(this.Status_Init);
//        sendMessage(this.Status_SonderMessunge);
//        
//        // Fake 
    }
    
    public String Fake_PCBToServer(int plate, byte pcb[], int value, int threshold , int temp) {
	String FakePCB = "event<br>" + Integer.toString(plate) + "<br>" + Byte.toString(pcb[0]) + "<br>" + Byte.toString(pcb[1]) + "<br>"
		+ Byte.toString(pcb[3]) + "<br>" + Integer.toString(value)  + "<br>" + Integer.toString(threshold) 
		+ "<br>" + Integer.toString(temp) ;
        
        return  FakePCB;
}
    public String  FakeError(String msg) {
	String FakeError = "error<br>" + msg;
        
        return FakeError;
}
    
    
    public synchronized void disconnect() {
        try {
            // SocketClientArduino .setDesonnected(true);
            // End for Input streaming und Output Streaming 
            desonnected_input=true;
            desonnected_output=true;
            AuschaltenSocket();
            
            // socket.close();
        } catch (Exception e) {
            System.err.println("Error closing client : "+e);
        }
        // socket=null;
        // out=null;
        // changeBorder(null);
        ipField.setEditable(true);
        portField.setEditable(true);
        this.DeviceName.setEditable(true);
       
        
        connectButton.setText("Connect");
        connectButton.setMnemonic('C');
        connectButton.setToolTipText("Start Connection");
        sendButton.setEnabled(false);
        sendField.setEditable(false);
        this.Zustand.setBackground(Color.red);
        this.Zustand.setText("Zustand Disconnected ");
    }
    
    public void error(String error) {
        if(error==null || error.equals(""))
            return;
        JOptionPane.showMessageDialog(Arduino.this,
                error, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void error(String error, String heading) {
        if(error==null || error.equals(""))
            return;
        JOptionPane.showMessageDialog(Arduino.this,
                error, heading, JOptionPane.ERROR_MESSAGE);
    }
    
    public void append(String msg) {
        messagesField.append(msg+NEW_LINE);
        messagesField.setCaretPosition(messagesField.getText().length());
    }
    
    public void appendnoNewLine(String msg) {
        messagesField.append("Server Input Message : "+msg);
        messagesField.setCaretPosition(messagesField.getText().length());
    }
    
    public void sendMessage(String s) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
      if(!this.desonnected_output){
        try	{
          
            
           
            append("Virtual Device Send : "+s);
            out.print(s+NEW_LINE);
            out.flush();
            sendField.setText("");
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } catch (Exception e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            JOptionPane.showMessageDialog(Arduino.this,
                    e.getMessage(),"Error Sending Message",
                    JOptionPane.ERROR_MESSAGE);
            disconnect();
        }
      
      }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        messagesField = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        sendField = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        sendButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        DeviceName = new javax.swing.JTextField();
        connectButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        IPvirtualDevice = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        ipField = new javax.swing.JTextField();
        portField = new javax.swing.JTextField();
        Status = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        Zustand = new javax.swing.JButton();

        messagesField.setColumns(20);
        messagesField.setRows(5);
        jScrollPane1.setViewportView(messagesField);

        jLabel4.setText("Custom Kommando Senden  : ");

        sendField.setText("init:ArduinoName");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "status<br>Initialisierung", "status<br>Bereit", "status<br>Versuchstatus<br>Sondermessung", "status<br>Liest Daten", "twinc" }));

        sendButton.setText("Kommando Senden ");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox1, 0, 569, Short.MAX_VALUE))
                    .addComponent(sendField)
                    .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sendField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(sendButton))
        );

        connectButton.setText("Arduino Connect ");
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Device Name ");

        jLabel1.setText("IP Server");

        jTextField5.setText("550");

        jLabel2.setText("Port");

        jLabel6.setText("Min. Ref. ");

        IPvirtualDevice.setEditable(false);

        jLabel7.setText("IP Virtual Device");

        ipField.setText("192.168.56.1");

        portField.setText("8888");

        Status.setText("Infos Connection    :    Connected  to  :   ");

        jTextField7.setText("600");

        jLabel8.setText("Max. Ref. ");

        jLabel9.setText("Virtual Mac-Adresse");

        jTextField8.setText("90-A2-DA-0D-F8-C4");

        Zustand.setBackground(new java.awt.Color(255, 51, 51));
        Zustand.setText("Zustand  :   DIsconnected");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(57, 57, 57)
                                .addComponent(ipField))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(57, 57, 57)
                                .addComponent(DeviceName))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(IPvirtualDevice)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8)
                            .addComponent(jLabel6)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(portField, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                            .addComponent(jTextField5)
                            .addComponent(jTextField7)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(Status))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(connectButton, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                            .addComponent(Zustand, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(3, 3, 3)))
                .addGap(6, 6, 6))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(ipField))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(portField)
                            .addComponent(jLabel2))
                        .addGap(3, 3, 3)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(jLabel5))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(16, 16, 16)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(DeviceName)
                                    .addComponent(jLabel6))))
                        .addGap(5, 5, 5))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jTextField5)
                        .addGap(12, 12, 12)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jLabel7))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(IPvirtualDevice))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField7)
                            .addComponent(jLabel8))
                        .addGap(1, 1, 1)))
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel9))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(connectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, Short.MAX_VALUE))
                .addGap(15, 15, 15)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(Status)
                    .addComponent(Zustand)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
        // TODO add your handling code here:
        connect();
    }//GEN-LAST:event_connectButtonActionPerformed

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        // TODO add your handling code here:
         String msg = sendField.getText();
                if(!msg.equals(""))
                    sendMessage(msg);
                else {
                    int value = JOptionPane.showConfirmDialog(
                            Arduino.this,  "Send Blank Line ?",
                            "Send Data To Server",
                            JOptionPane.YES_NO_OPTION);
                    if (value == JOptionPane.YES_OPTION)
                        sendMessage(msg);
                }
    }//GEN-LAST:event_sendButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField DeviceName;
    private javax.swing.JTextField IPvirtualDevice;
    private javax.swing.JLabel Status;
    private javax.swing.JButton Zustand;
    private javax.swing.JButton connectButton;
    private javax.swing.JTextField ipField;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextArea messagesField;
    private javax.swing.JTextField portField;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextField sendField;
    // End of variables declaration//GEN-END:variables
}
