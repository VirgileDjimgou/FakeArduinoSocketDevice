package net.sf.sockettest;

import java.awt.*;
import java.applet.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import net.sf.sockettest.swing.About;
import net.sf.sockettest.swing.SocketTestClient;
import net.sf.sockettest.swing.SocketTestServer;
import net.sf.sockettest.swing.SocketTestUdp;
import net.sf.sockettest.swing.SplashScreen;
import net.sf.sockettest.swing.Arduino;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

/**
 *
 * @author Djimgou Patrick Virgile
 */
public class SocketTest extends JFrame {
    private ClassLoader cl = getClass().getClassLoader();
    public ImageIcon logo = new ImageIcon(
            cl.getResource("icons/logo.gif"));
    public ImageIcon ball = new ImageIcon(
            cl.getResource("icons/ball.gif"));
    public static  JTabbedPane tabbedPane;
    public static int AnzahlVirtualArduino = 0;
    
    // 
    
       private void createMenuBar() {

        JMenuBar menubar = new JMenuBar();
        ImageIcon icon = new ImageIcon("exit.png");

        JMenu file = new JMenu("Options");
        file.setMnemonic(KeyEvent.VK_F);

        JMenuItem eMenuItem = new JMenuItem("Exit", icon);
        eMenuItem.setMnemonic(KeyEvent.VK_E);
        eMenuItem.setToolTipText("Exit application");
        eMenuItem.addActionListener((ActionEvent event) -> {
            System.exit(0);
        });
        
          JMenuItem VirtualDeviceMenuItem = new JMenuItem("Create new Virtual Device ", icon);
        VirtualDeviceMenuItem .setMnemonic(KeyEvent.VK_E);
        VirtualDeviceMenuItem .setToolTipText("Create Virtual Device ");
        VirtualDeviceMenuItem .addActionListener((ActionEvent event) -> {
            
            CreateNewArduino();
        });

        file.add(eMenuItem);
        file.add(VirtualDeviceMenuItem );

        menubar.add(file);

        setJMenuBar(menubar);
    }
       
    
    
    /** Creates a new instance of SocketTest */
    public SocketTest() {
        Container cp = getContentPane();
        
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        SocketTestClient client = new SocketTestClient(this);
        SocketTestServer server = new SocketTestServer(this);
        SocketTestUdp udp = new SocketTestUdp(this);
        // About about = new About();
        
        
        tabbedPane.addTab("Client", ball, (Component)client, "Test any server");
        tabbedPane.addTab("Server", ball, server, "Test any client");
        tabbedPane.addTab("Udp", ball, udp, "Test any UDP Client or Server");
        // tabbedPane.addTab("About", ball, about, "About SocketTest");
        AnzahlVirtualArduino ++;
        Arduino Arduino = new Arduino("Virtual Device "+Integer.toHexString(AnzahlVirtualArduino ));
        tabbedPane.addTab("Arduino Number : "+Integer.toString(AnzahlVirtualArduino) , ball, Arduino, "Fake Socket  Arduino ");
        
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        cp.add(tabbedPane);
        createMenuBar();
    }
 
    
    private void CreateNewArduino(){
            AnzahlVirtualArduino ++;
             Arduino Arduino = new Arduino("Virtual Device "+Integer.toHexString(AnzahlVirtualArduino ));
            tabbedPane.addTab("Arduino Number : "+Integer.toString(AnzahlVirtualArduino) , ball, Arduino, "Fake Socket  Arduino ");
    } 
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("net.sourceforge.mlf.metouia.MetouiaLookAndFeel");
        } catch(Exception e) {
            //e.printStackTrace();
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch(Exception ee) {
                System.out.println("Error setting native LAF: " + ee);
            }
        }
		
		boolean toSplash = true;
		if(args.length>0) {
			if("nosplash".equals(args[0])) toSplash = false;
		}
        
		SplashScreen splash = null;
		if(toSplash) splash = new SplashScreen();
        
        SocketTest st = new SocketTest();
        st.setTitle("Arduino Socket Tools  3.0.0");
       
        st.setSize(800,700);
        Util.centerWindow(st);
        st.setDefaultCloseOperation(EXIT_ON_CLOSE);
        st.setIconImage(st.logo.getImage());
        if(toSplash) splash.kill();
        st.setVisible(true);
    }
    
}
