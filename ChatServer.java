import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer
{
    private ServerSocket serverSocket;
    private Hashtable<Socket, PrintWriter> outputStreams = new Hashtable<Socket, PrintWriter>();
    private int count = 0;

    public ChatServer( int port ) throws IOException 
    {
        chat(port);
    }

    private void chat( int port ) throws IOException 
    {
     
        serverSocket = new ServerSocket( port );   
        while (true)                                        //loop forever
        {    
            Socket socket = serverSocket.accept();
            count ++;

            System.out.println( "Client :" + count);        //count number of users as they join

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            outputStreams.put(socket, out);
            new ServerThread( this, socket);                //add this current thread to socket


        }
    }

    // Get an enumeration of all the OutputStreams, one for each client connected

    Enumeration<PrintWriter> getOutputStreams() 
    {
        return outputStreams.elements();
    }

    // Send a chat message to all clients (utility routine)

    void sendToAll( String message ) 
    {
        synchronized( outputStreams ) 
        {
            for (Enumeration<PrintWriter> e = getOutputStreams();e.hasMoreElements(); ) 
            {
                PrintWriter out = (PrintWriter)e.nextElement();
                out.println( message );     
            }    
        }
    }
   

    void removeConnection( String name , Socket socket)         //remove connection when client exits
    {
        synchronized( outputStreams ) 
        {

            count --;
            System.out.println( "Client :" + count);

            outputStreams.remove(socket);
     
            try 
            {
                sendToAll(name + " just left the chatroom..." );
                socket.close();
            } 
            catch( IOException ie ) {}
        }   
    }
   
                                                                //main 
    public static void main( String args[] ) throws Exception 
    {
        int port = 7777;
        new ChatServer( port );
    } 
}

class ServerThread extends Thread
{
    private ChatServer server;
    private Socket socket;
    private String name ="";
  
    public ServerThread( ChatServer server, Socket socket ) 
    {

        this.server = server;
        this.socket = socket;

        start(); 
    }

    public void run() 
    {
        try                                                     //create buffer
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            name = in.readLine();
            server.sendToAll( name + " just joined the chatroom..." );      // send to all clients
            String message;

            while ((message = in.readLine()) != null)
            {
                server.sendToAll(name + " says : " +  message );            // send messages to all clients
            }
        } 


        catch( EOFException ie ) {} 
        catch( IOException ie ) {}

        finally{
            server.removeConnection(name,socket);
        }
    }
} // add some comments
