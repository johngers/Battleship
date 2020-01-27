// Client.java

import java.io.*;
import java.net.*;
import java.util.*;

class Client extends Thread {

    public static void main(String argv[]) throws Exception
    {
        String start, end;

        try (Socket serverSocket = new Socket("localhost",5000)) {
              System.out.println("Waiting for opponent...");
              ObjectInputStream inFromServer = new ObjectInputStream(serverSocket.getInputStream());
              ObjectOutputStream outToServer = new ObjectOutputStream(serverSocket.getOutputStream());
              Message message = null;
              BattleShipTable ptable = null;
              BattleShipTable ftable = null;
              String bomb = null;

              ArrayList<String> attacked = new ArrayList<String>();

              BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (serverSocket.isClosed() == false ) {
              message = (Message)inFromServer.readObject();
              System.out.println("---------------------------------------------------------------------------");
              ptable = message.Ptable;
              ftable = message.Ftable;
              if (message.getMsg() != null) {
                System.out.println(message.getMsg());
              }
              if (message.getMsgType() == 1) {
                System.out.println("Set up your board...");
                do {
                  System.out.println(ftable.toString());
                  System.out.print("Enter air carrier starting spot: ");
                  start = reader.readLine();
                  start = start.substring(0,2);
                  start = start.toUpperCase();
                  System.out.print("Enter air carrier second spot for direction: ");
                  end = reader.readLine();
                  end = end.substring(0,2);
                  end = end.toUpperCase();
                } while(!ftable.insertAirCarrier(start,end));

                do {
                  System.out.println(ftable.toString());
                  System.out.print("Enter destroyer starting spot: ");
                  start = reader.readLine();
                  start = start.substring(0,2);
                  start = start.toUpperCase();
                  System.out.print("Enter destroyer second spot for direction: ");
                  end = reader.readLine();
                  end = end.substring(0,2);
                  end = end.toUpperCase();
                } while(!ftable.insertDestroyer(start,end));

                do {
                  System.out.println(ftable.toString());
                  System.out.print("Enter submarine spot: ");
                  start = reader.readLine();
                  start = start.substring(0,2);
                  start = start.toUpperCase();
                }while(!ftable.insertSubmarine(start));

                System.out.println("Board set up: ");
          		  System.out.println(ftable.toString());
                message.Ptable = ptable;
                message.setMsgType(2);
                outToServer.writeObject(message);
                System.out.println("Waiting for opponent...");
              }
              else if (message.getMsgType() == 3) {
                System.out.println("Your board:");
                System.out.println(ftable.toString());
              //  do {
                System.out.println("Choose a spot to attack: ");
                System.out.println(ptable.toString());
                do {
                System.out.print("Enter coordinates: ");
                bomb = reader.readLine();
                bomb = bomb.substring(0,2);
                bomb = bomb.toUpperCase();
                //}while(!ftable.insertHit(test, "Q"));
                message.blockBombXY[0] = bomb.charAt(0);
                message.blockBombXY[1] = bomb.charAt(1);
                if (attacked.contains(bomb)) {
                  System.out.println("Error this spot has already been attacked. Please try again.");
                }
              } while(attacked.contains(bomb));
                attacked.add(bomb);
                outToServer.writeObject(message);
                outToServer.reset();
                message = (Message)inFromServer.readObject();
                if (message.getMsgType() == 6) {
                  ptable.insertHit(bomb, "X");
                  System.out.println(message.getMsg());
                }else {
                  ptable.insertHit(bomb, "O");
                  System.out.println(message.getMsg());
                }
                message.Ptable = ptable;
                message.setMsgType(4);
                System.out.println("Waiting for opponent...");
                outToServer.writeObject(message);
              }
              else if (message.getMsgType() == 4) {
                System.out.println("You were attacked at: " + Character.toString(message.blockBombXY[0]) + Character.toString(message.blockBombXY[1]));
              }
              else if (message.getMsgType() == 5) {
                System.out.println(message.Ptable.toString());
                serverSocket.close();
              }
              else {

              }
          }
        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }

        /**/
    }
}
