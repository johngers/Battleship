// Server.java
import java.net.*;
import java.io.*;
import java.text.*;
import java.time.*;
import java.util.*;

public class Server implements Runnable
{
	private Socket player1Socket = null;
	private Socket player2Socket = null;
	private BattleShipTable player1pTable = new BattleShipTable();
	private BattleShipTable player1fTable = null;
	private BattleShipTable player2pTable = new BattleShipTable();
	private BattleShipTable player2fTable = null;

	public static void main(String argv[]) throws Exception
	{
		ServerSocket welcomeSocket = new ServerSocket(5000);
		System.out.println("Server started...");
		while(true) {
			Socket p1Socket = welcomeSocket.accept();//get a connection from client
			System.out.println("Player 1 connected. Waiting for player 2...");
			Socket p2Socket = welcomeSocket.accept();
			Thread gameThread = new Thread(new Server(p1Socket, p2Socket));//create a thread
			gameThread.start(); //start the thread
			System.out.println("New game started on thread: " + gameThread.getId());
		}
	}

	public Server (Socket p1Socket, Socket p2Socket){
		this.player1Socket = p1Socket;
		this.player2Socket = p2Socket;
	}

	public void printTables() {
		System.out.println("Player 1's table: ");
		System.out.println(this.player1pTable.toString());

		System.out.println("Player 1's ftable: ");
		System.out.println(this.player1fTable.toString());

		System.out.println("Player 2's table");
		System.out.println(this.player2pTable.toString());

		System.out.println("Player 2's ftable: ");
		System.out.println(this.player2fTable.toString());
	}

	public void run(){
		String clientSentence=null;
		String capitalizedSentence=null;
		ObjectInputStream inFromPlayer1=null;
		ObjectOutputStream  outToPlayer1=null;
		ObjectInputStream inFromPlayer2=null;
		ObjectOutputStream  outToPlayer2=null;
		String table = null;
 		Message message = null;
		int roundCounter = 1;

		try{
			outToPlayer1 = new ObjectOutputStream(player1Socket.getOutputStream());
			inFromPlayer1 = new ObjectInputStream(player1Socket.getInputStream()); //Object input and output stream
			outToPlayer2 = new ObjectOutputStream(player2Socket.getOutputStream());
			inFromPlayer2 = new ObjectInputStream(player2Socket.getInputStream()); //Object input and output stream

			message = new Message();
			message.setMsgType(1);
			outToPlayer1.writeObject(message);
			outToPlayer1.reset();

			message = new Message();
			message.setMsgType(1);
			outToPlayer2.writeObject(message);
			outToPlayer2.reset();

			message = (Message)inFromPlayer1.readObject();
			if (message.getMsgType() == 2) {
				this.player1fTable = message.Ftable;
			}

			message = (Message)inFromPlayer2.readObject();
			if (message.getMsgType() == 2) {
				this.player2fTable = message.Ftable;
			}

			while (true) {
	//			System.out.println("Round " + Integer.toString(roundCounter) + ": ");
				//Player 1's turn
				message = new Message();
				message.setMsgType(3);
				// Set tables for correct player
				message.Ftable = this.player1fTable;
				message.Ptable = this.player1pTable;
				//Write tables to correct player
				outToPlayer1.writeObject(message);
				outToPlayer1.reset();
				//Read in modified message
				message = (Message)inFromPlayer1.readObject();

				if(this.player2fTable.checkHit(Character.toString(message.blockBombXY[0]) + Character.toString(message.blockBombXY[1]))) {
				//	System.out.println("Player 1 hit player 2");
					if (this.player2fTable.AIRCRAFT_CARRIER_SIZE2 == 0 || this.player2fTable.DESTROYER_SIZE2 == 0 || this.player2fTable.SUBMARINE_SIZE2 == 0) {
						message.setMsg("You sunk their battleship!");
						if (this.player2fTable.AIRCRAFT_CARRIER_SIZE2 == 0) {
							this.player2fTable.AIRCRAFT_CARRIER_SIZE2 += 1;
						}
						if (this.player2fTable.DESTROYER_SIZE2 == 0) {
							this.player2fTable.DESTROYER_SIZE2 += 1;
						}
						if (this.player2fTable.SUBMARINE_SIZE2 == 0) {
							this.player2fTable.SUBMARINE_SIZE2 += 1;
						}
					} else {
						message.setMsg("HIT!!!");
					}
					message.setMsgType(6);
					outToPlayer1.writeObject(message);
					outToPlayer1.reset();
				} else {
					message.setMsgType(3);
					message.setMsg("MISS");
					outToPlayer1.writeObject(message);
					outToPlayer1.reset();
				}
				message = (Message)inFromPlayer1.readObject();
				message.setMsg(null);
				// Set the f table for player 1 equal to modified table
				this.player1pTable =  message.Ptable;

				// Update player 2's table
				message.Ftable = this.player2fTable;

				this.player2fTable.insertHit(Character.toString(message.blockBombXY[0]) + Character.toString(message.blockBombXY[1]), "X");
				message.Ftable = this.player2fTable;
				outToPlayer2.writeObject(message);
				outToPlayer2.reset();

				if(!this.player2fTable.checkLoss()) {

				} else {

					message.setMsgType(5);
					message.setMsg("You won! Game over\nAttacks: ");
					message.Ptable = player1pTable;
					outToPlayer1.writeObject(message);
					message.setMsg("Game over player 1 wins\nYou were attacked at: ");
					message.Ptable = player2fTable;
					outToPlayer2.writeObject(message);
					break;
				}

				//Player 2's turn
				message = new Message();
				message.setMsgType(3);
				// Set tables for correct player
				message.Ftable = this.player2fTable;
				message.Ptable = this.player2pTable;
				//Write tables to correct player
				outToPlayer2.writeObject(message);
				outToPlayer2.reset();
				//Read in modified message
				message = (Message)inFromPlayer2.readObject();

				if(this.player1fTable.checkHit(Character.toString(message.blockBombXY[0]) + Character.toString(message.blockBombXY[1]))) {
				//	System.out.println("Player 2 hit player 1");
					if (this.player1fTable.AIRCRAFT_CARRIER_SIZE2 == 0 || this.player1fTable.DESTROYER_SIZE2 == 0 || this.player1fTable.SUBMARINE_SIZE2 == 0) {
						message.setMsg("You sunk their battleship!");
						if (this.player1fTable.AIRCRAFT_CARRIER_SIZE2 == 0) {
							this.player1fTable.AIRCRAFT_CARRIER_SIZE2 += 1;
						}
						if (this.player1fTable.DESTROYER_SIZE2 == 0) {
							this.player1fTable.DESTROYER_SIZE2 += 1;
						}
						if (this.player1fTable.SUBMARINE_SIZE2 == 0) {
							this.player1fTable.SUBMARINE_SIZE2 += 1;
						}
					} else {
						message.setMsg("HIT!!!");
					}
					message.setMsgType(6);
					outToPlayer2.writeObject(message);
					outToPlayer2.reset();
				} else {
					message.setMsgType(3);
					message.setMsg("MISS");
					outToPlayer2.writeObject(message);
					outToPlayer2.reset();
				}
				message = (Message)inFromPlayer2.readObject();
				message.setMsg(null);
				// Set the f table for player 2 equal to modified table
				this.player2pTable =  message.Ptable;

				// Update player 1's table
				this.player1fTable.insertHit(Character.toString(message.blockBombXY[0]) + Character.toString(message.blockBombXY[1]), "X");
				message.Ftable = this.player1fTable;
				outToPlayer1.writeObject(message);
				outToPlayer1.reset();

				if(!this.player1fTable.checkLoss()) {

				} else {
					message.setMsgType(5);
					message.setMsg("Game over player 2 wins\nYou were attacked at: ");
					message.Ptable = player2pTable;
					outToPlayer1.writeObject(message);
					message.setMsg("You won! Game over\nAttacks: ");
					message.Ptable = player1fTable;
					outToPlayer2.writeObject(message);
					break;
				}
				roundCounter = roundCounter + 1;

			}

		}catch(Exception ex){
		System.out.println(ex.getMessage());
		}

	}
}
