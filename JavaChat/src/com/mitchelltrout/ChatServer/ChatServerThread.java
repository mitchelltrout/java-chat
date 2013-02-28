//ChatServerThread.java
//By: Mitchell Trout
//January 22, 2008

package com.mitchelltrout.ChatServer;

import java.net.*;
import java.io.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;

public class ChatServerThread extends Thread
{
	private ChatServer       server    = null;
	private Socket           socket    = null;
	private int              ID        = -1;
	private String			 username  = "";
	private DataInputStream  streamIn  = null;
	private DataOutputStream streamOut = null;
	private boolean 		 isAdm	   = false;

	public ChatServerThread(ChatServer _server, Socket _socket)
	{
		super();
		server = _server;
		socket = _socket;
		ID     = socket.getPort();
	}
	
	public void send(String msg)
	{
		try
		{
			streamOut.writeUTF(msg);
			streamOut.flush();
		}
		catch(IOException ioe)
		{
			System.out.println(ID + " ERROR sending: " + ioe.getMessage());
			server.remove(ID);
			stop();
		}
	}
	
	public int getID()
	{
		return ID;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public void setUsername(String user)
	{
		username = user;
	}
	
	public boolean checkAdm()
	{
		if (isAdm == true)
			return true;
		else
			return false;
	}
	
	public void setAdm(boolean yon)
	{
		if(yon == true)
			isAdm = true;
		else
			isAdm = false;
	}
	
	public void run()
	{
		//gets the current time
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("H:mm:ss");
		
		System.out.println(sdf.format(cal.getTime()) + " Server Thread " + ID + " running.");
		while (true)
		{
			try
			{
				server.handle(ID, streamIn.readUTF());
			}
			catch(IOException ioe)
			{
				System.out.println(ID + " ERROR reading: " + ioe.getMessage());
				server.remove(ID);
				stop();
			}
		}
	}

	public void open() throws IOException
	{
		streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

	public void close() throws IOException
	{
		if (socket != null)
			socket.close();
		if (streamIn != null)
			streamIn.close();
		if (streamOut != null)
			streamOut.close();
	}
}
