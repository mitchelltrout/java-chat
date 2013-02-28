//ChatServer.java
//By: Mitchell Trout
//January 22, 2008

package com.mitchelltrout.ChatServer;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;


public class ChatServer implements Runnable
{
	private ChatServerThread clients[] = new ChatServerThread[50];
	private ServerSocket server = null;
	private Thread       thread = null;
	private int clientCount = 0;
	private String disconUser = "";
	private static String admPass = "no_one_will_ever_guess_this";

	public ChatServer(int port)
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
		
		try
     {  
			System.out.println("Binding to port " + port + ", please wait  ...");
			toLog("Binding to port " + port + ", please wait  ...");
			server = new ServerSocket(port);  
			System.out.println("Server started: " + server);
			toLog(sdf.format(cal.getTime()) + " -- Server started: " + server);
			start();
		}
		catch(IOException ioe)
		{
			System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
			toLog("Can not bind to port " + port + ": " + ioe.getMessage());
		}
 }
	
	public void toLog(String log)
	{
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter("server.log", true));
			out.write(log + "\r\n");
			out.close();
		}
		catch(IOException ioe)
		{
			System.out.println("There was an error while trying to append to the log file. " + ioe);
			stop();
		}
	}
	
 public void run()
 {
		while (thread != null)
		{
			try
			{
				//System.out.println("Waiting for a client ...");
				//toLog("Waiting for a client ...");
				addThread(server.accept());
			}
			catch(IOException ioe)
			{
				System.out.println("Server accept error: " + ioe);
				toLog("Server accept error: " + ioe);
				stop();
			}
		}
	}
	
	public void start()
	{
		if (thread == null)
   	{  
	      	thread = new Thread(this); 
      	thread.start();
   	}
	}
	
	public void stop()
	{
		if (thread != null)
   	{  
	      	thread.stop(); 
      	thread = null;
   	}
	}
	
	private int findClient(int ID)
	{
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getID() == ID)
				return i;
			return -1;
	}
	
	private int findUser(String user)
	{
		for (int i = 0; i < clientCount; i++)
			if (clients[i].getUsername().equals(user))
				return i;
			return -1;
	}

	public void sendUserList()
	{
		String userList = "";				
		for (int i = 0; i < clientCount; i++)
			userList += "[" + i + "]" + clients[i].getUsername() + ":-:";
		for (int i = 0; i < clientCount; i++)
			clients[i].send("/!users " + userList);
	}
	
	public synchronized void handle(int ID, String input)
	{
		//gets the current time
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
		
		System.out.println(sdf.format(cal.getTime()) + "[" + findClient(ID) + "]" + clients[findClient(ID)].getUsername() + ": " + input); //prints what was sent to the server
		toLog(sdf.format(cal.getTime()) + "[" + findClient(ID) + "]" + clients[findClient(ID)].getUsername() + ": " + input);
		
		if (input.substring(0, 1).equals("/")) //client sent a command to the server
		{
			if (input.length() > 11 && input.substring(0, 12).equals("/!newjoined ")) //for new joined users
			{
				String newUser = input.substring(12, input.length());
				clients[clientCount-1].setUsername(newUser);
				
				//send users list to each user
				sendUserList();
				
				for (int i = 0; i < clientCount; i++)
					clients[i].send(newUser + " joined the chat.");
			}
			else if (input.length() > 5 && input.substring(0, 6).equals("/users")) //sends a userlist to the user
			{
				String toClient = "User List:\n";
				
				for (int i = 0; i < clientCount; i++)
				{
					toClient += "[" + i + "]" + clients[i].getUsername() + "\n";
				}
				
				clients[findClient(ID)].send(toClient);
			}
			else if (input.length() > 4 && input.substring(0, 4).equals("/me ")) //emotion
			{
				String meMess = " " + input.substring(4, input.length());
				for (int i = 0; i < clientCount; i++)
					clients[i].send(clients[findClient(ID)].getUsername() + meMess);
			}
			else if (input.length() > 6 && input.substring(0, 6).equals("/name ")) //change name
			{
				String oldUser = clients[findClient(ID)].getUsername();
				String newUsern = input.substring(6, input.length());
				clients[findClient(ID)].setUsername(newUsern);
				
				for (int i = 0; i < clientCount; i++)
					clients[i].send(oldUser + " changed name to " + newUsern + ".");
					
				sendUserList();
			}
			else if (input.length() > 5 && input.substring(0, 5).equals("/adm "))
			{
				String admCmd = input.substring(5, input.length());
				admFunctions(ID, admCmd);
			}
			else //what ever they sent didnt match any of the commands above so they are wrong... lulz
			{
				clients[findClient(ID)].send("Unknown command sent.");
			}
		}
		else //no server command sent so it was just regular chat
		{
			for (int i = 0; i < clientCount; i++)
				clients[i].send(clients[findClient(ID)].getUsername() + ": " + input); //sends what was said to the clients
		}
	}
	
	public void admFunctions(int ID, String admCmd)
	{		
		try
		{
			if (admCmd.length() > 9 && admCmd.substring(0, 9).equals("password ")) //check the password the user entered
			{
				String userPass = admCmd.substring(9, admCmd.length());
				if(userPass.equals(admPass))
				{
					clients[findClient(ID)].setAdm(true);
					clients[findClient(ID)].send("You are now an administrator.");
				}
				else
					clients[findClient(ID)].send("Bad password.");
			}
			else if (admCmd.length() > 5 && admCmd.substring(0, 5).equals("kick ") && clients[findClient(ID)].checkAdm()) //kicks a client from the server
			{
				try
				{
					int kickUser = Integer.parseInt(admCmd.substring(5, admCmd.length()));
					clients[kickUser].send("You have been kicked from the server.");
					clients[kickUser].send("/!kicked");
					remove(clients[kickUser].getID());
					
					for (int i = 0; i < clientCount; i++)
						clients[i].send(clients[kickUser].getUsername() + " has been kicked from the server."); //tell everyone the user was kicked
				}
				catch(InputMismatchException imme)
				{
					clients[findClient(ID)].send("Cannot do command. Useage: '/adm kick [#id]'");
				}
			}
			else if (admCmd.length() > 10 && admCmd.substring(0, 10).equals("forcename ") && clients[findClient(ID)].checkAdm())
			{
				int user = Integer.parseInt(admCmd.substring(10, 11));
				String newName = admCmd.substring(12, admCmd.length());
				clients[user].setUsername(newName);
				sendUserList();
			}
			else if (admCmd.length() > 9 && admCmd.substring(0, 9).equals("forcesay ") && clients[findClient(ID)].checkAdm())
			{
				int user = Integer.parseInt(admCmd.substring(9, 10));
				String say = admCmd.substring(11, admCmd.length());
				for (int i = 0; i < clientCount; i++)
					clients[i].send(clients[user].getUsername() + ": " + say);
			}
			else if (admCmd.length() > 8 && admCmd.substring(0, 8).equals("giveadm ") && clients[findClient(ID)].checkAdm())
			{
				try
				{
					int toWho = Integer.parseInt(admCmd.substring(8, 9));
					clients[toWho].setAdm(true);
					clients[toWho].send("You have been given administrator rights.");
					clients[findClient(ID)].send(clients[toWho].getUsername() + " has been given adm rights.");
				}
				catch(InputMismatchException imme)
				{
					clients[findClient(ID)].send("Cannot do command. Useage: '/adm giveadm [#id]'");
				}
			}
			else
			{
				if(clients[findClient(ID)].checkAdm())
					clients[findClient(ID)].send("Unknown adm command.");
				else
					clients[findClient(ID)].send("You are not an admin.");
				
			}
		}
		catch(ArrayIndexOutOfBoundsException aioobe)
		{
			clients[findClient(ID)].send("There was an error with your command. Useage: '/adm command [#id] extra_options'");
		}
	}
	
	public synchronized void remove(int ID)
	{
		int pos = findClient(ID);
		if (pos >= 0)
		{
			disconUser = clients[findClient(ID)].getUsername();
			ChatServerThread toTerminate = clients[pos];
			System.out.println("Removing client thread " + ID + " at " + pos);
			toLog("Removing client thread " + ID + " at " + pos);
			if (pos < clientCount-1)
				for (int i = pos+1; i < clientCount; i++)
					clients[i-1] = clients[i];
			clientCount--;
			try
			{
				toTerminate.close();
				String userList2 = "";				
				for (int i = 0; i < clientCount; i++)
					userList2 += "[" + i + "]" + clients[i].getUsername() + ":-:";
				for (int i = 0; i < clientCount; i++)
					clients[i].send("/!users " + userList2);
				
				for (int i = 0; i < clientCount; i++)
					clients[i].send(disconUser + " disconnected.");
				disconUser = "";
			}
			catch(IOException ioe)
			{
				System.out.println("Error closing thread: " + ioe);
				toLog("Error closing thread: " + ioe);
			}
			toTerminate.stop();
		}
	}
	
	private void addThread(Socket socket)
	{
		if (clientCount < clients.length)
		{
			//gets the current time
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
			
			System.out.println(sdf.format(cal.getTime()) + " Client accepted: " + socket);
			toLog(sdf.format(cal.getTime()) + " Client accepted: " + socket);
			clients[clientCount] = new ChatServerThread(this, socket);
			try
			{	
				clients[clientCount].open(); 
				clients[clientCount].start();  
				clientCount++;
			}
			catch(IOException ioe)
			{
				System.out.println("Error opening thread: " + ioe);
				toLog("Error opening thread: " + ioe);
			}
		}
		else
		{
			System.out.println("Client refused: maximum " + clients.length + " reached.");
			toLog("Client refused: maximum " + clients.length + " reached.");
		}
	}

	public static void main(String args[])
	{
		ChatServer server = null;
		boolean error = true; //to start the while loop
		
   	System.out.println("Thank you for using Mitchell's Java Chat");
		System.out.println("If you have any problems please read the user's manual.\n");
		
		if (args.length != 2)
		{
			while(error)
			{
				Scanner s = new Scanner(System.in);
				int port = 0;
				try
				{
					System.out.print("\nPlease specify a port for the chat server to use (9800 is default): ");
					port = s.nextInt();
					error = true;
					System.out.print("\nPlease enter an administrator password: ");
					admPass = s.next();
					System.out.println();
					server = new ChatServer(port); //starts the server
					error = false;
				}
				catch(InputMismatchException ime)
				{
					System.out.println("Error. Please enter a vaild port.");
					error = true;
				}
			}
		}
   	else
		{
     	admPass = args[1];
			server = new ChatServer(Integer.parseInt(args[0]));
		}
	}
}