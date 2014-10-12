import java.io.*; 
import java.net.*;
import java.util.Collections;
import java.util.Vector;

class FTPServer {
	public static String WRITEPATH = "./server-directory/";
	public int controlPort;
	public int dataPort;
	public String ipAddress;
	public String commandFromControlSocket; 
	public String commandFromDataSocket; 
	public String responseToControlSocket;
	public String responseToDataSocket;
	public ServerSocket controlSocket;
	public ServerSocket dataSocket;
	public BufferedReader inFromControlSocket;
	public PrintWriter outToControlSocket;
	public BufferedReader inFromDataSocket;
	public Vector<String> dirList;

	public static void main(String argv[]) throws Exception{ 
		FTPServer FS = new FTPServer();
		FS.setupServer(argv);
		FS.runServer();
	}

	public void runServer() throws IOException{
		while(true) {
			Socket clientConnectionControlSocket = controlSocket.accept();

			inFromControlSocket = new BufferedReader(new InputStreamReader(clientConnectionControlSocket.getInputStream()));
			outToControlSocket = new PrintWriter(clientConnectionControlSocket.getOutputStream(), true);

			if(inFromControlSocket.readLine().equals("PASV")){
				dataSocket = createServerSocket(dataPort);
				outToControlSocket.println(String.format("200 PORT %s %d",ipAddress,dataPort));
			}
			else {
				outToControlSocket.println("502 PASV NOT RECEIVED");
				continue;
			}

			String[] splittedCommand = inFromControlSocket.readLine().split(" ");

			if(splittedCommand[0].equals("DIR")){
				renderDIR(splittedCommand);
			}
			else if(splittedCommand[0].equals("GET")){
				renderGET(splittedCommand);
			}
			else if(splittedCommand[0].equals("PUT")){
				renderPUT(splittedCommand);
			}
			else {
				outToControlSocket.println("500 UNKNOWN COMMAND");
			}
			dataSocket.close();
			outToControlSocket.close();
			inFromControlSocket.close();
			clientConnectionControlSocket.close();
		}
	}

	public void renderDIR(String[] splittedCommand) throws IOException{
		if(splittedCommand.length == 1){
			outToControlSocket.println("200 DIR COMMAND OK");

			Socket clientConnectionDataSocket = dataSocket.accept();
			DataOutputStream outToDataSocket = new DataOutputStream(clientConnectionDataSocket.getOutputStream());

			dirList = new Vector<String>();

			createWriteDirectory();
			getDir(WRITEPATH);
			
			String lineFeed = System.getProperty("line.separator");

			if(dirList.size()==0){
				outToDataSocket.write(("− − −the server directory is empty− − −"+lineFeed).getBytes());
			}
			else{
				Collections.sort(dirList);
				responseToDataSocket = "";
				for (int i = 0; i < dirList.size(); i++) {
					responseToDataSocket += dirList.get(i)+lineFeed;
				}
				outToDataSocket.write(responseToDataSocket.substring(0,responseToDataSocket.length()-1).getBytes());
				System.out.println(responseToDataSocket);
			}
			outToDataSocket.flush();
			outToDataSocket.close();
			clientConnectionDataSocket.close();
			outToControlSocket.println("200 OK");
		}
		else{
			outToControlSocket.println("501 INVALID ARGUMENTS");
		}
	}

	public void getDir(String path){
		File dir = new File(path);
		File[] listFiles = dir.listFiles();

		if(listFiles.length==0){ dirList.add(path.replace(WRITEPATH, "")+"/"); }
		else{
			for(int i=0;i<listFiles.length;i++){
				String subPath = listFiles[i].getPath();
				if(listFiles[i].isDirectory()){ getDir(subPath); }
				else{ dirList.add(subPath.replace(WRITEPATH, "")); }
			}
		}
	}

	public void createWriteDirectory() throws IOException{
		File writeDir = new File(WRITEPATH);
		if (!writeDir.exists()) { writeDir.mkdir(); }
	}

	public void renderGET(String[] splittedCommand) throws IOException{
		if(splittedCommand.length == 2){
			File fileToGet = new File(WRITEPATH+splittedCommand[1]);
			if(!fileToGet.exists()){
				outToControlSocket.println("401 FILE NOT FOUND");
			}
			else{
				outToControlSocket.println("200 GET COMMAND OK");

				Socket clientConnectionDataSocket = dataSocket.accept();
				BufferedInputStream inFromFile = new BufferedInputStream(new FileInputStream(fileToGet));
				BufferedOutputStream outToDataSocket = new BufferedOutputStream(clientConnectionDataSocket.getOutputStream());
				
				int i;
				while ((i = inFromFile.read()) != -1) {
					outToDataSocket.write(i);
				}
				
				inFromFile.close();
				outToDataSocket.flush();
				outToDataSocket.close();
				clientConnectionDataSocket.close();
				outToControlSocket.println("200 OK");
			}
		}
		else{
			outToControlSocket.println("501 INVALID ARGUMENTS");
		}
	}

	public void renderPUT(String[] splittedCommand) throws IOException{
		if(splittedCommand.length >= 2 && splittedCommand.length<4){
			outToControlSocket.println("200 PUT COMMAND OK");
			String pathToWrite = WRITEPATH;
			if(splittedCommand.length==3){
				File writeDir = new File(WRITEPATH+splittedCommand[2]+"/");
				if (!writeDir.exists()) { writeDir.mkdir(); }
				pathToWrite += splittedCommand[2]+"/";
			}
			File fileToWrite = new File(pathToWrite+(splittedCommand[1].substring(splittedCommand[1].lastIndexOf("/"))));
			if (!fileToWrite.exists()) { fileToWrite.createNewFile(); }

			Socket clientConnectionDataSocket = dataSocket.accept();
			BufferedInputStream inFromDataSocket = new BufferedInputStream(clientConnectionDataSocket.getInputStream());
			BufferedOutputStream outToFile = new BufferedOutputStream(new FileOutputStream(fileToWrite));

			int i;
			while ((i = inFromDataSocket.read()) != -1) {
				outToFile.write(i);
			}
			
			inFromDataSocket.close();
			outToFile.flush();
			outToFile.close();
			clientConnectionDataSocket.close();
			outToControlSocket.println("200 OK");
		}
		else{
			outToControlSocket.println("501 INVALID ARGUMENTS");
		}
	}

	public void setupServer(String argv[]) throws IOException{
		try{
			controlPort = Integer.parseInt(argv[0]);
			ipAddress = Inet4Address.getLocalHost().getHostAddress();
			//String ipAddress = "127.0.0.1";
			System.out.println("ipAddress: "+ipAddress);
			dataPort = argv.length>1?Integer.parseInt(argv[1]):controlPort+1;

			controlSocket = createServerSocket(controlPort);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public ServerSocket createServerSocket(int port) throws IOException{
		final ServerSocket newSocket = new ServerSocket(port);

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try{
					newSocket.close();
				}
				catch (IOException e){
					e.printStackTrace();
				}
			}
		}, "Shutdown-thread"));
		return newSocket;
	}
} 

