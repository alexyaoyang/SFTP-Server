import java.io.*; 
import java.net.*; 

class FTPClient { 
	public static String WRITEPATH = "./client-directory/";
	public int controlPort;
	public int dataPort;
	public String ipAddress;
	public String command;
	public String fourthParam;
	public String fifthParam;
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
		if(command.equals("PUT")){
			File fileToGet = new File(WRITEPATH+fourthParam);
			if(!fileToGet.exists()){
				System.out.println("FILE NOT FOUND");
				logMessage = "FILE NOT FOUND";
				writeToLogFile();
				System.exit(0);
			}
		}

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
					renderPUT();
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

		File dirList = createFile(WRITEPATH+"directory_listing");

		BufferedWriter outToFile = new BufferedWriter(new FileWriter(dirList.getAbsoluteFile()));

		int i;
		while ((i = inFromDataSocket.read()) != -1) {
			outToFile.write(i);
		}
		
		outToFile.flush();
		outToFile.close();
		String serverResponse = inFromControlSocket.readLine();
		if(serverResponse.split(" ")[0].equals("200")){
			System.out.println(serverResponse);
			logMessage = serverResponse;
			inFromDataSocket.close();
			serverDataSocket.close();
		}
	}

	public void renderGET() throws IOException{
		Socket serverDataSocket = new Socket(ipAddress, dataPort);
		File fileToWrite = createFile(WRITEPATH+fourthParam.substring(fourthParam.lastIndexOf('/')));
		BufferedInputStream inFromDataSocket = new BufferedInputStream(serverDataSocket.getInputStream());
		BufferedOutputStream outToFile = new BufferedOutputStream(new FileOutputStream(fileToWrite));

		int i;
		while ((i = inFromDataSocket.read()) != -1) {
			outToFile.write(i);
		}
		outToFile.flush();
		outToFile.close();

		String serverResponse = inFromControlSocket.readLine();
		if(serverResponse.split(" ")[0].equals("200")){
			System.out.println(serverResponse);
			logMessage = serverResponse;
			inFromDataSocket.close();
			serverDataSocket.close();
		}
	}

	public void renderPUT() throws IOException{
		File fileToGet = new File(WRITEPATH+fourthParam);
		Socket serverDataSocket = new Socket(ipAddress, dataPort);
		BufferedInputStream inFromFile = new BufferedInputStream(new FileInputStream(fileToGet));
		BufferedOutputStream outToDataSocket = new BufferedOutputStream(serverDataSocket.getOutputStream());

		int i;
		while ((i = inFromFile.read()) != -1) {
			outToDataSocket.write(i);
		}
		outToDataSocket.flush();
		outToDataSocket.close();

		String serverResponse = inFromControlSocket.readLine();
		if(serverResponse.split(" ")[0].equals("200")){
			System.out.println(serverResponse);
			logMessage = serverResponse;
			inFromFile.close();
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
		fourthParam = argv.length>3?argv[3]:"";
		fifthParam = argv.length>4?argv[4]:"";
		commandWithParam = "";
		for (int i=2; i<argv.length; i++) {
			commandWithParam += argv[i] + " ";
		}
	}

	public void writeToLogFile() throws IOException{
		File logFile = createFile("./log");
		FileWriter logWriter = new FileWriter(logFile);
		logWriter.write(logMessage);

		logWriter.close();
	}

	public File createFile(String path) throws IOException{
		File newFile = new File(path);

		if (!newFile.exists()) {
			newFile.createNewFile();
		}
		return newFile;
	}
}
