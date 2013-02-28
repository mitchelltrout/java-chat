//ChatClient.java
//By: Mitchell Trout
//January 22, 2008

package com.mitchelltrout.JavaChat;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
import java.security.*;
import java.util.regex.*;

public class ChatClient implements ActionListener
{
 private Socket socket = null;
	private DataInputStream console = null;
	private DataOutputStream streamOut = null;
	private ChatClientThread client = null;
	private String userName = "";
	private String serverName = "localhost";
	private int serverPort = 9800;
	private boolean connected = false;
	
	private JFrame connFrame, ncFrame;
	private JTextArea display, usersBox;
	private JTextField input, serverAddress, userNameInput, ncField;
 private JScrollPane jspPane, userPane;
	private JButton send, connectbut, ncButton;
	private JLabel servAddLabel, userNameLabel, ncLabel;

 public static void main(String[] args)
	{
		JFrame.setDefaultLookAndFeelDecorated(false);

     //Create and set up the window.
     JFrame frame = new JFrame("Mitchell's Java Chat");
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

     ChatClient chat = new ChatClient();
		frame.setJMenuBar(chat.createJMenuBar());
     frame.setContentPane(chat.createContentPane());

     frame.setSize(500, 300);
		frame.setLocationRelativeTo(null);
     frame.setVisible(true);
 }
	
	public JMenuBar createJMenuBar()
	{
     JMenuBar menuBar;
     JMenu settings;
     JMenuItem connectTo, disconnectMI, nameChangeMI, exitClient;

     //ImageIcon icon = createImageIcon("jmenu.jpg");

     menuBar = new JMenuBar();

     settings = new JMenu("Settings");
     settings.setMnemonic(KeyEvent.VK_M);
     menuBar.add(settings);

     //Creating the MenuItems
     connectTo = new JMenuItem("Connect to:"/*, KeyEvent.VK_T*/);
		disconnectMI = new JMenuItem("Disconnect");
		nameChangeMI = new JMenuItem("Change Name");
		exitClient = new JMenuItem("Exit");
     
     //connectTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
     connectTo.addActionListener(this);
		disconnectMI.addActionListener(this);
		nameChangeMI.addActionListener(this);
		exitClient.addActionListener(this);
     settings.add(connectTo);
		settings.add(disconnectMI);
		settings.addSeparator();
		settings.add(nameChangeMI);
		settings.addSeparator();
		settings.add(exitClient);

     return menuBar;
 }

 public Container createContentPane()
	{
     //Create the content-pane-to-be.
     JPanel contentPane = new JPanel(new BorderLayout());
     contentPane.setOpaque(true);

     //Create a scrolled text area.
     display = new JTextArea();
     display.setEditable(false);
     jspPane = new JScrollPane(display);
		
		//create the input and send button
		JPanel bottomPane = new JPanel(new BorderLayout());
     bottomPane.setOpaque(true);		
		input = new JTextField();
		input.addActionListener(this);
		send = new JButton("Send");
		send.addActionListener(this);
		bottomPane.add(input, BorderLayout.CENTER);
		bottomPane.add(send, BorderLayout.EAST);
		
		//create user pane
     usersBox = new JTextArea(5, 8);
     usersBox.setEditable(false);
     userPane = new JScrollPane(usersBox);
		

     //Add the text area to the content pane.
     contentPane.add(jspPane, BorderLayout.CENTER);
		contentPane.add(userPane, BorderLayout.EAST);
		contentPane.add(bottomPane, BorderLayout.SOUTH);

     return contentPane;
 }
 
 public void connectFrame()
	{
		connFrame = new JFrame("Connect");
     connFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

     //Create the content-pane-to-be.
     JPanel connPane = new JPanel(new BorderLayout());
     connPane.setOpaque(true);

		//create the inputs and send button
		servAddLabel = new JLabel(" Server Address: ");
		serverAddress = new JTextField();
		
		userNameLabel = new JLabel(" Username: ");
		userNameInput = new JTextField();

		connectbut = new JButton("Connect");
		connectbut.addActionListener(this);
		
		//input text
		JPanel saddPane = new JPanel(new BorderLayout());
		saddPane.add(serverAddress, BorderLayout.CENTER);
		saddPane.add(servAddLabel, BorderLayout.WEST);
		JPanel unPane = new JPanel(new BorderLayout());
		unPane.add(userNameInput, BorderLayout.CENTER);
		unPane.add(userNameLabel, BorderLayout.WEST);
		
		//inputs together
		JPanel in2Pane = new JPanel(new BorderLayout());
		in2Pane.add(saddPane, BorderLayout.NORTH);
		in2Pane.add(unPane, BorderLayout.CENTER);
		
		JPanel inputPane = new JPanel(new BorderLayout());
		inputPane.add(in2Pane, BorderLayout.NORTH);
		
		//button
		JPanel buttonPane = new JPanel(new BorderLayout());
		buttonPane.add(connectbut, BorderLayout.EAST);
		
		connPane.add(inputPane, BorderLayout.CENTER);
		connPane.add(buttonPane, BorderLayout.SOUTH);
		
		connFrame.setContentPane(connPane);
		
		connFrame.setSize(250, 100);
		connFrame.setLocationRelativeTo(null);
     connFrame.setVisible(true);
		connFrame.setAlwaysOnTop(true);
	}
	
	public void nameChangeFrame()
	{
		ncFrame = new JFrame("Change Name");
     ncFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

     //Create the content-pane-to-be.
     JPanel ncPane = new JPanel(new BorderLayout());
     ncPane.setOpaque(true);
		Color noColor = new Color(0, 0, 0, 0);
		ncPane.setBorder(BorderFactory.createLineBorder (noColor, 2));
		
		//pane to hold the texfield
		JPanel ncTfPane = new JPanel(new BorderLayout());
		ncTfPane.setOpaque(true);

		//create the label textfield and button
		ncLabel = new JLabel("Change name to: ");
		ncField = new JTextField();
		ncButton = new JButton("Change Name!");
		ncButton.addActionListener(this);
		
		//add them to the layout
		ncPane.add(ncLabel, BorderLayout.WEST);
		ncTfPane.add(ncField, BorderLayout.NORTH);
		ncPane.add(ncTfPane, BorderLayout.CENTER);
		ncPane.add(ncButton, BorderLayout.SOUTH);
		
		ncFrame.setContentPane(ncPane);
		
		//finish off the frame and make it visible
		ncFrame.setSize(250, 90);
		ncFrame.setLocationRelativeTo(null);
     ncFrame.setVisible(true);
		ncFrame.setAlwaysOnTop(true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String target = e.getActionCommand();
		
		//invisible frame for use with the popup boxes
		JFrame frame2 = new JFrame("Mitchell Trout is awesome, lawlzorz!");
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
			if (target.equals("Connect"))
			{
				if (!serverAddress.getText().equals(""))
				{
					//get the ip and port
					String con = serverAddress.getText();
					String[] conList = con.split(":");
					serverName = conList[0];
					serverPort = Integer.parseInt(conList[1]);
					
					userName = userNameInput.getText();
					
					if (connected == false)
						connect(serverName, serverPort);
					else
					{
						input.setText("/bye");
						send();
						connect(serverName, serverPort); 
					}
					
					connFrame.setVisible(false);
				}
				else
				{
					JOptionPane.showMessageDialog(frame2, "You must enter a server address to connect."); //popup box
				}
			}
			else if (target.equals("Send"))
			{	
				if (connected == true)
				{
					if (!input.getText().equals(""))
					{
						send();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(frame2, "You are not connected to a server."); //popup box
				}
			}
			else if (target.equals("Change Name!")) //change name button
			{
				if (connected == true)
				{
					input.setText("/name " + ncField.getText());
					send();
					ncFrame.hide();
				}
				else
					JOptionPane.showMessageDialog(frame2, "You are not connected to a server."); //popup box
			}
			else if (target.equals("Connect to:")) //menu item
			{
				connectFrame();
			}
			else if (target.equals("Change Name")) //menu item
			{
				nameChangeFrame();
			}
			else if (target.equals("Disconnect"))
			{
				if (connected == true)
				{
					input.setText("/bye");
					send();
				}
			}
			else if (target.equals("Exit"))
			{
				System.exit(0);
			}
			else if (!input.getText().equals(""))
			{
				if (connected == true)
				{
					send();
				}
				else
				{
					JOptionPane.showMessageDialog(frame2, "You are not connected to a server."); //popup box
				}
			}
			else
			{
				println("Unknown action performed.");
			}
		
		//kill the invisible frame as it is no longer needed
		frame2.dispose();
 }
	
	public void connect(String serverName, int serverPort)
	{ 
		println("Establishing connection. Please wait ...");
		try
		{ 
			socket = new Socket(serverName, serverPort);
			println("Connected!");
			open();

			// sets the username
			if (userName.length() < 1)
			{
				userName = "UnNamed";
			}
			input.setText("");
			streamOut.writeUTF("/!newjoined " + userName);
			connected = true;
		}
		catch(UnknownHostException uhe)
		{ 
			println("Host unknown: " + uhe.getMessage()); 
		}
		catch(IOException ioe)
		{ 
			println("Unexpected exception: " + ioe.getMessage());
		}
		catch(AccessControlException ace)
		{ 
			println("Access denied. You do not have the proper permissions to connect.");
			println(ace.toString());
		}
	}
	
	private void send()
	{ 
		try
		{
			String text = input.getText();
			
			if (text.substring(0, 1).equals("/")) //command is entered so test to see which command
			{
				clientCommand(text);
			}
			else
			{
				streamOut.writeUTF(text);
			}
			
			streamOut.flush();
			input.setText("");
		}
		catch(IOException ioe)
		{
			println("Sending error: " + ioe.getMessage());
			close(); 
		}
	}
	
	public void handle(String msg)
	{
		if (msg.substring(0, 2).equals("/!"))
		{
			if (msg.length() > 8 && msg.substring(0, 8).equals("/!users "))
			{
				String userList = msg.substring(8, msg.length());
				parseUserList(userList);
			}
			else if (msg.length() > 7 && msg.substring(0, 8).equals("/!kicked"))
			{
				close();
			}
		}
		else
			println(msg);
	}
	
	public void parseUserList(String userList)
	{
		String[] list = userList.split(":-:");
		int listNumber = list.length;
		usersBox.setText("");
		for(int i = 0; i < listNumber; i++)
		{
			usersBox.append(list[i] + "\n");
			usersBox.setCaretPosition(usersBox.getDocument().getLength());
		}
	}
	
	public void open()
	{
		try
		{
			streamOut = new DataOutputStream(socket.getOutputStream());
			client = new ChatClientThread(this, socket);
		}
		catch(IOException ioe)
		{
			println("Error opening output stream: " + ioe);
		}
	}
	
	public void close()
	{
		try
		{
			if (streamOut != null)  
				streamOut.close();
			if (socket    != null)  
				socket.close(); 
			connected = false;
			usersBox.setText("");
		}
		catch(IOException ioe)
		{
			println("Error closing ..."); 
		}
		client.close(); 
		client.stop(); 
	}
	
	private void println(String msg)
	{
		display.append(msg + "\n");
		display.setCaretPosition(display.getDocument().getLength());
	}

	private void clientCommand(String cmd)
	{
		try
		{
			if (cmd.length() > 5 && cmd.substring(0, 5).equals("/con ")) //connect to another server
			{
				try
				{
					//get the ip and port
					String con = cmd.substring(5, cmd.length());
					String[] conList = con.split(":");
					serverName = conList[0];
					serverPort = Integer.parseInt(conList[1]);
					
					//disconnect from this server and connect to the other
					input.setText("/bye");
					send();
					connect(serverName, serverPort);
				}
				catch(ArrayIndexOutOfBoundsException aiobe)
				{
					println("Error. Must be in format \"/con host:port\" ex.( /con 127.0.0.1:9800 )");
				}
				catch(NumberFormatException nfe)
				{
					println("Error. Must be in format \"/con host:port\" ex.( /con 127.0.0.1:9800 )");
				}
			}
			else if (cmd.length() > 3 && cmd.substring(0, 4).equals("/bye")) //for disconnecting from the server
			{
				println("Good bye.");
				close();
			}
			else if (cmd.length() > 1 && cmd.substring(0, 2).equals("/!")) //entered a server command
			{
				println("Unknown command sent.");
			}
			else
			{
				streamOut.writeUTF(cmd);
			}
		}
		catch(IOException ioe)
		{
			println("Sending error: " + ioe.getMessage());
			close();
		}
	}
}

class ChatClientThread extends Thread
{ 
	private Socket           socket   = null;
	private ChatClient       client   = null;
	private DataInputStream  streamIn = null;

	public ChatClientThread(ChatClient _client, Socket _socket)
	{ 
		client   = _client;
		socket   = _socket;
		open();  
		start();
	}
	
	public void open()
	{ 
		try
		{ 
			streamIn  = new DataInputStream(socket.getInputStream());
		}
		catch(IOException ioe)
		{ 
			System.out.println("Error getting input stream: " + ioe);
			//client.stop();
		}
	}
	
	public void close()
	{
		try
		{
			if (streamIn != null) 
				streamIn.close();
		}
		catch(IOException ioe)
		{
			System.out.println("Error closing input stream: " + ioe);
		}
	}
	
	public void run()
	{
		while (true)
		{
			try
			{
				client.handle(streamIn.readUTF());
			}
			catch(IOException ioe)
			{
				System.out.println("Listening error: " + ioe.getMessage());
				client.close();
				//client.stop();
			}
		}
	}
}

