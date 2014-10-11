import java.io.*; 
import java.net.*; 

class FTPServer { 

	public static void main(String argv[]) throws Exception 
	{ 
		String commandFromClient; 
		String responseFromServer;
		int controlport = Integer.parseInt(argv[0]);
		int dataport = controlport + 1;
		String ipAddress = Inet4Address.getLocalHost().getHostAddress();
		ServerSocket welcomeSocket = new ServerSocket(controlport);

		if(!argv[1].isEmpty()){
			dataport = Integer.parseInt(argv[1]);
		}

		while(true) {

			Socket connectionSocket = welcomeSocket.accept(); 
			System.out.println(" accepted socket ... \n");

			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			commandFromClient = inFromClient.readLine(); 
			System.out.println("client command: " + commandFromClient + "\n");

			if(commandFromClient.compareTo("PASV")){
				responseFromServer = String.format("200 PORT %s %d \r\n",ipAddress,dataport);
				outToClient.writeBytes(responseFromServer);
				commandFromClient = inFromClient.readLine();
			}

			String[] splittedCommand = commandFromClient.split(" ");

			if(splittedCommand[0].compareTo("DIR")){
				if(splittedCommand.length == 1){
					outToClient.writeBytes("200 DIR COMMAND OK\r\n");

					String path = new File(".").getAbsolutePath();
					File dir = new File(path);
					String[] listFiles = dir.list();

					Arrays.sort(listFiles);

					responseFromServer = "";
					for (int i = 0; i < listFiles.length; i++) {
						responseFromServer.concat(listFiles[i]+"\r\n");
					}
					outToClient.writeBytes(responseFromServer);

					outToClient.writeBytes("200 OK\r\n");
				}
				else{
					outToClient.writeBytes("501 INVALID ARGUMENTS\r\n");
				}
			}
			else if(splittedCommand[0].compareTo("GET")){
				if(splittedCommand.length == 2){
					outToClient.writeBytes("200 GET COMMAND OK\r\n");

					outToClient.writeBytes("200 OK\r\n");
				}
				else{
					outToClient.writeBytes("501 INVALID ARGUMENTS\r\n");
				}
			}
			else if(splittedCommand[0].compareTo("PUT")){
				if(splittedCommand.length >= 2){
					outToClient.writeBytes("200 PUT COMMAND OK\r\n");

					outToClient.writeBytes("200 OK\r\n");
				}
				else{
					outToClient.writeBytes("501 INVALID ARGUMENTS\r\n");
				}
			}
			else {
				outToClient.writeBytes("500 UNKNOWN COMMAND\r\n");
			}
			outToClient.close();
			inFromClient.close();
			connectionSocket.close();
		}
		welcomeSocket.close();
	}
} 






