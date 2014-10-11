import java.io.*; 

import java.net.*; 

class FTPClient { 

    public static void main(String argv[]) throws Exception 
    { 
        String sentence; 
        String modifiedSentence; 

        BufferedReader inFromUser = 
          new BufferedReader(new InputStreamReader(System.in)); 

        // Socket clientSocket = new Socket("suna.comp.nus.edu.sg", 6789); 
        Socket clientSocket = new Socket("localhost", 64998); 

        DataOutputStream outToServer = 
          new DataOutputStream(clientSocket.getOutputStream()); 

        BufferedReader inFromServer = new BufferedReader(
          new InputStreamReader(clientSocket.getInputStream())); 

        sentence = inFromUser.readLine(); 
        System.out.println("CLIENT (READ FROM PROMPT) " + sentence); 
        outToServer.writeBytes(sentence + '\n'); 

        modifiedSentence = inFromServer.readLine(); 

        System.out.println("CLIENT (READ FROM SERVER) " + modifiedSentence); 

        clientSocket.close(); 

    } 
}

 

