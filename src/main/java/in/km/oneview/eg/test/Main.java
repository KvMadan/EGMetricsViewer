/**
 *  EG Commands: GET_MONITOR - default"; GET_FULL_MONITOR";
 *  GET_APP_STAT"; GET_REPORT"; PRINT_REPORT"; SET_LOG_LEVEL:[0,1,2]";
 *  PAUSE_LOAD:[SECONDS]"; RESUME_LOAD"; PRINT_WARN:[0,1]";
 */
package in.km.oneview.eg.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * @author Madan Kavarthapu
 *
 */

public class Main {

	final static Logger log = Logger.getLogger(Main.class);
	
	private String hostname = "10.237.48.238";
	private String port = "30010";
	private String line;
	
	private Socket socket, reportSocket;
	
	private InputStream input, reportInput;
	private BufferedReader reader, reportReader;
	
	private OutputStream output, reportOutput;
	private PrintWriter writer, reportWriter;
	
	//private StringBuffer metricsReceived;

	public Main(String hostname, String port){
		this.hostname = hostname;
		this.port = port;
	}
	
	public boolean init(){
		
		try{
			log.debug("Connecting to EG Server @ " + hostname + ":" + port);
			
			socket = new Socket(InetAddress.getByName(hostname), Integer.parseInt(port));
			reportSocket = new Socket(InetAddress.getByName(hostname), Integer.parseInt(port));
			// read data from server.
			input = socket.getInputStream();
			reader = new BufferedReader(
					new InputStreamReader(input));
			// read report data from server
			reportInput = reportSocket.getInputStream();
			reportReader = new BufferedReader(
					new InputStreamReader(reportInput));
			
			// send data to server
			output = socket.getOutputStream();
			writer = new PrintWriter(output, true);
			//send report data to server
			reportOutput = reportSocket.getOutputStream();
			reportWriter = new PrintWriter(reportOutput, true);
			
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	
	public void startReader(){
		
		try{
			Thread receiverThread = new Thread(new Runnable(){

				public void run() {
					Thread.currentThread().setName("EG Reader");
					try {
						//metricsReceived = new StringBuffer();
						log.debug("Receiving data from Server:");
						while ((line = reader.readLine()) != null) {
							log.debug(line);
							//metricsReceived.append(line + System.lineSeparator());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			receiverThread.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void startWriter(){
		Thread senderThread = new Thread(new Runnable(){
			public void run() {
				Thread.currentThread().setName("EG Writer");
				while(true){
					// This is a message sent to the server
					writer.print("GET_MONITOR"); // change it to print while working with real EG Server. 
					writer.flush();
					
					//Checking the server side socket availability. 
					if (writer.checkError()){
						try {
							socket.close();
							log.debug("Exiting...");
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.exit(1);
					}
						
					
					log.debug("Sent Message: GET_MONITOR");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		senderThread.start();
	}


	public void startReportReader(){
		
		try{
			Thread receiverThread = new Thread(new Runnable(){

				public void run() {
					Thread.currentThread().setName("EG Report Reader");
					try {
						log.debug("Receiving data from Server:");
						while ((line = reportReader.readLine()) != null) {
							log.debug(line);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			receiverThread.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	public void startReportWriter(){
		Thread senderThread = new Thread(new Runnable(){
			public void run() {
				Thread.currentThread().setName("EG Report Writer");
				while(true){
					// This is a message sent to the server
					reportWriter.print("GET_REPORT"); // change it to print while working with real EG Server. 
					reportWriter.flush();
					log.debug("Sent Message: GET_REPORT");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		senderThread.start();
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		//Main eg = new Main("10.237.48.238","30010");
		//Main eg = new Main("127.0.0.1","30010");

		
		String egserver = System.getProperty("eg.server");
		String egport = System.getProperty("eg.port");
		
		
		if (egserver == null || egport == null){
			
			System.out.println("\nMissing Required parameters!\n");
			
			System.out.println("*****************************************************");
			System.out.println("Usage: ");
			System.out.println("java -Deg.server=10.237.48.238 -Deg.port=30010");
			System.out.println("-jar EGMetricsViewer_v1.1.jar");
			System.out.println("*****************************************************");
			
			System.exit(1);
		}
		else{
			log.debug("EG Server: " + System.getProperty("eg.server"));
		}
		
		
		Main eg = new Main(System.getProperty("eg.server"), System.getProperty("eg.port"));

		if(eg.init()){
			
			eg.startWriter();
			eg.startReader();
			
			eg.startReportWriter();
			eg.startReportReader();
		}
	}
}