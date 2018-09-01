import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
//Nick Kowalsky
public class ChatFrame extends Frame{
	ChatFrame() {
		setSize(500, 500);
		setTitle("Chat Frame");
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				System.exit(0);
			}
		});
		add(new ChatPanel(), BorderLayout.CENTER);
		setVisible(true);
	}
	public static void main(String[] args) {
		new ChatFrame();
	}
}

class ChatPanel extends Panel implements ActionListener, Runnable {
	TextArea ta;
	TextField tf;
	Button connect, disconnect;
	Thread t;
	java.awt.List list;
	Socket s;
	ObjectOutputStream out;
	ObjectInputStream in;
	DataObject objOut;
	DataObject objIn;
	String userName;

	ChatPanel() {
		setLayout(new BorderLayout());
		tf = new TextField();
		tf.addActionListener(this);
		ta = new TextArea();
		add(tf, BorderLayout.NORTH);
		add(ta, BorderLayout.CENTER);
		connect = new Button("Connect");
		connect.addActionListener(this);
		disconnect = new Button("Disconnect");
		disconnect.addActionListener(this);
		Panel buttonPanel = new Panel();
		buttonPanel.add(connect);
		buttonPanel.add(disconnect);
		add(buttonPanel, BorderLayout.SOUTH);
		list = new java.awt.List(4, false);
		list.addActionListener(this);
		add(list, BorderLayout.EAST);
		disconnect.setEnabled(false);
		ta.setEditable(false);
	}

	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == connect){
			try {
				s = new Socket("127.0.0.1", 3000);
				System.out.println("Connection Established");
				out = new ObjectOutputStream(s.getOutputStream());
				in = new ObjectInputStream(s.getInputStream());
				t = new Thread(this);
				t.start();
				objOut = new DataObject();
				objOut.setMessage(tf.getText() + " Connected!");
				out.writeObject(objOut);
				out.reset();
				userName = tf.getText();
				tf.setText("");
				connect.setEnabled(false);
				disconnect.setEnabled(true);
			}catch(UnknownHostException uhe){
				System.out.println(uhe.getMessage());
			}catch(IOException ioe){
				System.out.println(ioe.getMessage());
			}
		} else if(ae.getSource() == disconnect) {
			try {
				objOut = new DataObject();
				objOut.setMessage(userName + " Disconnected");
				out.writeObject(objOut);
				out.reset();
				s.close();
				System.out.println(" Disconnected");
				ta.append(userName + " Disconnected\n");
				list.removeAll();
				ta.setText("");
				connect.setEnabled(true);
				disconnect.setEnabled(false);
			}catch(UnknownHostException uhe){
				System.out.println(uhe.getMessage());
			}catch(IOException ioe){
				System.out.println(ioe.getMessage());
			}
		} else if(ae.getSource() == tf) {
			try {
				objOut = new DataObject();
				objOut.setMessage(userName + ": " + tf.getText());
				out.writeObject(objOut);
				out.reset();
			} catch(IOException ioe) {
				System.out.println(ioe.getMessage());
			}
			tf.setText("");
		} else if(ae.getSource() == list){ 
			tf.setText("To " + list.getSelectedItem() + ": ");
		}
	}

	public void run() {
		try {
			while(t != null) {
				DataObject objIn = (DataObject)in.readObject();
				ta.append(objIn.getMessage() + "\n");
				System.out.println("Users:" + objIn.getNames());
				list.removeAll();
				for(String name : objIn.getNames()) list.add(name);
			}
		}catch(ClassNotFoundException cnfe){
			System.out.println(cnfe.getMessage());
		}catch(IOException ioe){
			System.out.println(ioe.getMessage());
		}
	}
}