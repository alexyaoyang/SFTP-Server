import java.io.*; 
import java.net.*; 

class FTPClient { 
	public static String WRITEPATH = "./client-directory/";
	public int controlPort;
	public int dataPort;
	public String ipAddress;
	public String command;
	public String commandWithParam;
	public Socket serverControlSocket;
	public PrintWriter outToControlSocket;
	public BufferedReader inFromControlSocket;
	public String logMessage;

	public static void main(String argv[]) throws IOException{ 
		FTPClient FC = new FTPClient();
		FC.setupClient(argv);
		FC.runClient();
	}

	public void runClient() throws IOException{
		// Socket clientSocket = new Socket("suna.comp.nus.edu.sg", 6789); 
		serverControlSocket = new Socket(ipAddress, controlPort); 

		outToControlSocket = new PrintWriter(serverControlSocket.getOutputStream(), true);
		inFromControlSocket = new BufferedReader(new InputStreamReader(serverControlSocket.getInputStream()));

		System.out.println("Sending PASV");
		outToControlSocket.println("PASV");

		String serverReply = inFromControlSocket.readLine();
		System.out.println("received from server: "+serverReply);
		String[] splitReplyString = serverReply.split(" ");

		createWriteDirectory();

		if(splitReplyString[0].equals("200")){
			ipAddress = splitReplyString[2];
			dataPort = Integer.parseInt(splitReplyString[3]);

			outToControlSocket.println(commandWithParam);
			serverReply = inFromControlSocket.readLine();
			if(serverReply.split(" ")[0].equals("200")){
				if(command.equals("DIR")){
					renderDIR();
				}
				else if(command.equals("GET")){
					renderGET();
				}
				else if(command.equals("PUT")){

				}
			}
			else {
				System.out.println(serverReply);
				logMessage = serverReply;
			}
		}
		inFromControlSocket.close();
		outToControlSocket.close();
		serverControlSocket.close(); 
		writeToLogFile();
	}

	public void renderDIR() throws IOException{
		Socket serverDataSocket = new Socket(ipAddress, dataPort);
		BufferedReader inFromDataSocket = new BufferedReader(new InputStreamReader(serverDataSocket.getInputStream()));

		File dirList = new File(WRITEPATH+"directory_listing");

		if (!dirList.exists()) {
			dirList.createNewFile();
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(dirList.getAbsoluteFile()));
		
		String dirListString;
		while((dirListString = inFromDataSocket.readLine()) != null){
			System.out.println(dirListString);
			bw.write(dirListString+"\r\n");
		}
		bw.flush();
		bw.close();
		
		if(inFromControlSocket.readLine().split(" ")[0].equals("200")){
			inFromDataSocket.close();
			serverDataSocket.close();
		}
	}
	
	public void renderGET() throws IOException{
		Socket serverDataSocket = new Socket(ipAddress, dataPort);
		BufferedReader inFromDataSocket = new BufferedReader(new InputStreamReader(serverDataSocket.getInputStream()));
		
		if(inFromDataSocket.readLine().split(" ")[0].equals("401")){
			System.out.println("401 FILE NOT FOUND");
		}
		
		
		
		if(inFromControlSocket.readLine().split(" ")[0].equals("200")){
			inFromDataSocket.close();
			serverDataSocket.close();
		}
	}

	public void createWriteDirectory() throws IOException{
		File writeDir = new File(WRITEPATH);

		if (!writeDir.exists()) {
			writeDir.mkdir();
		}
	}

	public void setupClient(String argv[]) throws IOException{
		ipAddress = argv[0];
		controlPort = Integer.parseInt(argv[1]);
		command = argv[2];
		commandWithParam = "";
		for (int i=2; i<argv.length; i++) {
			commandWithParam += argv[i] + " ";
		}
	}
	
	public void writeToLogFile() throws IOException{
		File logFile = new File("./log");
		
		if (!logFile.exists()) {
			logFile.createNewFile();
		}
		
		FileWriter logWriter = new FileWriter(logFile);
		logWriter.write(logMessage);
		
		logWriter.close();
	}
}
