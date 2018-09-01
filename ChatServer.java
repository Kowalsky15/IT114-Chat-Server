import java.io.*;
import java.net.*;
import java.util.*;
//Nick Kowalsky
public class ChatServer{

	public static void main(String[] args ){
		ArrayList<ChatHandler> handlers = new ArrayList<>();
		try {
			ServerSocket s = new ServerSocket(3000);
			while(true) new ChatHandler(s.accept(), handlers).start();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}

class ChatHandler extends Thread {
	private Socket incoming;
	private ArrayList<ChatHandler> handlers;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private String name;

	ChatHandler(Socket i, ArrayList<ChatHandler> h) {
		incoming = i;
		handlers = h;
		handlers.add(this);
	}

	public void run() {
		try {
			in = new ObjectInputStream(incoming.getInputStream());
			out = new ObjectOutputStream(incoming.getOutputStream());

			while(true) {
				DataObject objIn = (DataObject)in.readObject();

				if (objIn.getMessage().endsWith(" Connected!")) name = objIn.getMessage().split(" ")[0];
				else if(objIn.getMessage().endsWith(" Disconnected")) handlers.remove(this);

				for(ChatHandler handler : handlers) objIn.getNames().add(handler.name);

				System.out.println("Message: " + objIn.getMessage() + ".");

				if(objIn.getMessage().contains("To ")) {
				    System.out.println(objIn.getMessage());
					
					String rcvName = objIn.getMessage().split(" ")[2];
                    
					rcvName = rcvName.substring(0, rcvName.length() - 1);
					
					for(ChatHandler h2 : handlers){
						if(h2.name.equals(rcvName) || h2.name.equals(name)){
							h2.out.writeObject(objIn);
						}
					}
					continue;
				}

				for(ChatHandler h : handlers) h.out.writeObject(objIn);
                if(objIn.getMessage().endsWith(" Disconnected")) break;
			}
			incoming.close();
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			handlers.remove(this);
		}
	}
}