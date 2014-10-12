import java.io.*; 
import java.net.*; 
import java.util.Arrays;

class FTPServer {
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
	public PrintWriter outToDataSocket;

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
				outToDataSocket.println("502 PASV NOT RECEIVED");
				continue;
			}

			String[] splittedCommand = inFromDataSocket.readLine().split(" ");

			if(splittedCommand[0].compareTo("DIR")==0){
				renderDIR(splittedCommand);
			}
			else if(splittedCommand[0].compareTo("GET")==0){
				renderGET(splittedCommand);
			}
			else if(splittedCommand[0].compareTo("PUT")==0){
				renderPUT(splittedCommand);
			}
			else {
				outToDataSocket.println("500 UNKNOWN COMMAND");
			}
			outToDataSocket.close();
			inFromDataSocket.close();
			outToControlSocket.close();
			inFromControlSocket.close();
			clientConnectionControlSocket.close();
		}
	}

	public void renderDIR(String[] splittedCommand){
		if(splittedCommand.length == 1){
			outToControlSocket.println("200 DIR COMMAND OK");
			
			Socket clientConnectionDataSocket = dataSocket.accept();
			outToDataSocket = new PrintWriter(clientConnectionDataSocket.getOutputStream(), true);

			String path = new File(".").getAbsolutePath();
			File dir = new File(path);
			String[] listFiles = dir.list();

			if(listFiles.length==0){
				outToDataSocket.println("− − −the server directory is empty− − −");
			}
			else{
				Arrays.sort(listFiles);

				responseToDataSocket = "";
				for (int i = 0; i < listFiles.length; i++) {
					responseToDataSocket.concat(listFiles[i]+"\n");
				}
				outToDataSocket.println(responseToDataSocket);
			}
			outToDataSocket.flush();
			outToDataSocket.close();
			outToControlSocket.println("200 OK");
			outToControlSocket.close();
		}
		else{
			outToControlSocket.println("501 INVALID ARGUMENTS");
		}
	}
	
	public void renderGET(String[] splittedCommand){
		if(splittedCommand.length == 2){
			outToControlSocket.println("200 GET COMMAND OK");

			outToControlSocket.println("200 OK");
		}
		else{
			outToControlSocket.println("501 INVALID ARGUMENTS");
		}
	}
	
	public void renderPUT(String[] splittedCommand){
		if(splittedCommand.length >= 2){
			outToControlSocket.println("200 PUT COMMAND OK");

			outToControlSocket.println("200 OK");
		}
		else{
			outToControlSocket.println("501 INVALID ARGUMENTS");
		}
	}

	public void setupServer(String argv[]){
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
		ServerSocket newSocket = new ServerSocket(port);

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

