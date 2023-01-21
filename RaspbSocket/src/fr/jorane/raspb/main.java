package fr.jorane.raspb;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class main {

	public static main Instance;
	
	
	public ServerSocket server;
	private int port;
	public main(int port) {
		Instance = this;
		this.port = port;
		try {
			this.server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		startServer();
	}
	
	public static void main(String[] args) {
		//todo recuperer le port qui est en ligne de commande
		new main(46008);
		

	}

private List<connClient> clients = new ArrayList<connClient>();

private connClient[] clients2 = new connClient[0];

	public void startServer() {
		
		while(true) {
			
			for(int i = 0; i<clients.size(); i++) {
				if(clients.get(i).isInterrupted()) {
					clients.remove(i);
				}
			}
			
			
			if(this.server.isClosed()) {
				try {
					this.server = new ServerSocket(port);
				} catch (IOException e) {

				}
			}
			
			try {
				System.out.println("WAIT CLIENT");

				clients.add(new connClient(this.server.accept()));

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(clients != null && clients.isEmpty()) {
					for(connClient c:clients) {
						try {
							c.client.close();
						} catch (IOException e) {}
					}
				}
			}
		}
		
	}
	
	class connClient extends Thread
	{
		
		private DataInputStream input;
		private DataOutputStream output;
		public Socket client;
		
		public connClient(Socket client) {
			this.client = client;
			try {
				this.input = new DataInputStream(client.getInputStream());
				this.output = new DataOutputStream(client.getOutputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.start();
		}
		
		
		
		
		
		@Override
		public void run() {
			while(!this.isInterrupted()) {
				if(this.client.isClosed()) {

					this.interrupt();
					break;
				}
				
				try {
					if(this.input.available() > 0) {
						String cmd = this.input.readUTF();
						String[] values = cmd.split("-");
						
						
						Map<String, String> parameters = new HashMap<>();
						parameters.put("temperature", values[0]);
						parameters.put("tension", values[1]);
						parameters.put("ampere", values[2]);
						parameters.put("vibration", values[3]);
						parameters.put("vitesse", values[4]);
						parameters.put("longitude", values[5]);
						parameters.put("latitude", values[6]);
						
						//Temperature, tension, ampere, vibration, vitesse, longitute, latitude
						
						URL url = new URL("https://jorane-ripiego-etu.pedaweb.univ-amu.fr/extranet/upload.php?"+ParameterStringBuilder.getParamsString(parameters));
						HttpURLConnection con = (HttpURLConnection) url.openConnection();
						con.setRequestMethod("GET");

						con.setDoOutput(true);

						BufferedReader in = new BufferedReader(
						  new InputStreamReader(con.getInputStream()));
						String inputLine;
						StringBuffer content = new StringBuffer();
						while ((inputLine = in.readLine()) != null) {
						    content.append(inputLine);
						}
						System.out.println(content.toString());
						in.close();
						con.disconnect();
						
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
