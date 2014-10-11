import java.io.*; 
import java.net.*; 

class FTPClient { 

    public static void main(String argv[]) throws Exception 
    { 
        if(argv.length<3){
        	System.out.println("Args missing");
        	System.exit(0);
        }
        String serverReply = "";
        String ipAddress = argv[0];
        int serverPort = Integer.parseInt(argv[1]);
        String command = argv[2];

        // Socket clientSocket = new Socket("suna.comp.nus.edu.sg", 6789); 
        Socket clientSocket = new Socket("localhost", serverport); 

        DataOutputStream outToServer = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream())); 
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
        
         = inFromServer.readLine(); 
        outToServer.writeBytes(sentence + '\r\n'); 

        System.out.println("CLIENT (READ FROM SERVER) " + modifiedSentence); 

        inFromServer.close();
        outToServer.close();
        clientSocket.close(); 
    } 
}

 

