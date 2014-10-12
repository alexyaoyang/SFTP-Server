import java.io.*; 
import java.net.*; 

class FTPClient { 
	public static String WRITEPATH = "./client-directory/";
	public int controlPort;
	public int dataPort;
	public String ipAddress;
	public String command;
	public Socket serverControlSocket;
	public PrintWriter outToControlSocket;
	public BufferedReader inFromControlSocket;

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

			outToControlSocket.println(command);
			serverReply = inFromControlSocket.readLine();
			if(serverReply.split(" ")[0].equals("200")){
				if(command.equals("DIR")){
					renderDIR();
				}
				else if(command.equals("GET")){

				}
				else if(command.equals("PUT")){

				}
			}
		}
		inFromControlSocket.close();
		outToControlSocket.close();
		serverControlSocket.close(); 
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

	public void createWriteDirectory() throws IOException{
		File writeDir = new File(WRITEPATH);

		if (!writeDir.exists()) {
			writeDir.mkdir();
		}
	}

	public void setupClient(String argv[]) throws IOException{
		if(argv.length<3){
			System.out.println("Args missing");
			System.exit(0);
		}
		ipAddress = argv[0];
		controlPort = Integer.parseInt(argv[1]);
		command = argv[2];
	}
}



