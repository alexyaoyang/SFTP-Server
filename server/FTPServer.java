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
		
		System.out.println(String.format("200 PORT $s %d \r\n",ipAddress,dataport));

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
					responseFromServer = "200 DIR COMMAND OK";
					outToClient.writeBytes(responseFromServer);
					
					
				}
				else{
					responseFromServer = "501 INVALID ARGUMENTS";
					outToClient.writeBytes(responseFromServer);
				}
			}
			else if(splittedCommand[0].compareTo("GET")){
				if(splittedCommand.length == 1){
					responseFromServer = "200 GET COMMAND OK";
					outToClient.writeBytes(responseFromServer);
					
					
				}
				else{
					responseFromServer = "501 INVALID ARGUMENTS";
					outToClient.writeBytes(responseFromServer);
				}
			}
			else if(splittedCommand[0].compareTo("PUT")){
				if(splittedCommand.length == 1){
					responseFromServer = "200 PUT COMMAND OK";
					outToClient.writeBytes(responseFromServer);
					
					
				}
				else{
					responseFromServer = "501 INVALID ARGUMENTS";
					outToClient.writeBytes(responseFromServer);
				}
			}
			else {
				responseFromServer = "500 UNKNOWN COMMAND";
				outToClient.writeBytes(responseFromServer);
			}
			outToClient.close();
			inFromClient.close();
			connectionSocket.close();
		}
		welcomeSocket.close();
	}
} 






