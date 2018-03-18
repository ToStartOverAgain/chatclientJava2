/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import entity.AccountEntity;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;


/**
 *
 * @author thanhSon
 */
public class ClientChat {

    public static void main(String[] args) {

        handlMenu();
    }

    // menu
    public static void handlMenu() {

        while (true) {
            Scanner scanner = new Scanner(System.in);

            System.out.println("===================Connect Server Chat=================");

            System.out.println("1. Please enter Address Server: ");
            String adServer = scanner.nextLine();
            System.out.println("2. Please enter Number Port: ");
            int numPort = scanner.nextInt();
            scanner.nextLine();

            Socket socket = handlConnection(adServer, numPort);

            if (socket != null) {
                boolean logout = false;
                while (!logout) {
                    System.out.println("************** Messenger ***************");
                    System.out.println("1. Login.");
                    System.out.println("2. Signup.");
                    System.out.println("3. Logout.");
                    System.out.println("Please enter your choise: ");
                    int choise = scanner.nextInt();
                    scanner.nextLine();
                    switch (choise) {
                        case 1:
                            handlLogin(socket);
                            break;
                        case 2:
                            handlSignUp(socket);
                            break;
                        case 3:
                            try {
                                socket.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            logout = true;
                            break;
                        default:
                            System.out.println("Please enter your choise from 1 to 3.");
                            break;
                    }
                    System.out.println("****************************************");
                }
            }
        }
    }

    // Connect to server
    public static Socket handlConnection(String adServer, int numPort) {

        Socket socket = null;
        try {
            socket = new Socket(adServer, numPort);
            System.out.println("Connection Success!!!");
        } catch (IOException ex) {
            System.out.println("Address Server or Number Port incorrect. Please check again!!!");
        }
        return socket;
    }

    // login
    public static void handlLogin(Socket socket) {

        System.out.println("---------------Login---------------");

        String type = "login";
        String success = "Login Success!!!";
        String fail = "UserName or Password incorrect. Please check again!";

        AccountEntity acc = handlAccount(type);
        sendRequest(socket, acc);
        receiveResponse(socket, type, success, fail);
        
        System.out.println("------------------------------");
    }

    // signup
    public static void handlSignUp(Socket socket) {

        System.out.println("--------------SignUp--------------");

        String type = "signup";
        String success = "SignUp Success!!!";
        String fail = "UserName existed. Please use UserName other!";

        AccountEntity acc = handlAccount(type);
        sendRequest(socket, acc);
        receiveResponse(socket, type, success, fail);

        System.out.println("-------------------------------");
    }

    // enter UserName and Password of Account
    public static AccountEntity handlAccount(String type) {

        Scanner scanner = new Scanner(System.in);
        AccountEntity acc = new AccountEntity();
        acc.setType(type);

        while (true) {
            System.out.println("1. Please enter Username:");
            String userName = scanner.nextLine();
            if (userName != null && userName.length() >= 6) {
                acc.setUserName(userName);
                break;
            }
            System.out.println("Username cannot empty and must be at least 6 characters. Please enter again!");
        }

        while (true) {
            System.out.println("2. Please enter Password: ");
            String password = scanner.nextLine();
            if (password != null && password.length() >= 6) {
                acc.setPassword(password);
                break;
            }
            System.out.println("Password cannot empty and must be at least 6 characters. Please enter again!");
        }

        return acc;
    }
    
    // send request to server
    public static void sendRequest(Socket socket, AccountEntity acc) {

        try {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(acc);
            oos.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    //receive response from server
    public static void receiveResponse(Socket socket, String type, String success, String fail) {

        try {
            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String tmp;
            while (true) {
                tmp = br.readLine();
                if ("success".equals(tmp) || "fail".equals(tmp)) {
                    break;
                }
            }

            if ("success".equals(tmp)) {

                System.out.println(success);

                if ("login".equals(type)) {
                    handlMessage(socket);
                } else if ("signup".equals(type)) {
                    handlLogin(socket);
                }

            } else if ("fail".equals(tmp)) {
                System.out.println(fail);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
   
    // send and receive message 
    public static void handlMessage(Socket socket) {

        Scanner scanner = new Scanner(System.in);
        try {
            OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
            BufferedWriter bw = new BufferedWriter(osw);

            InputStreamReader isr = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            System.out.println("Enter something to chat or 'bye' to stop.");

            //write message to server
            Thread writer = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String text = scanner.nextLine();
                            bw.write(text);
                            bw.newLine();
                            bw.flush();
                            if ("bye".equalsIgnoreCase(text)) {
                                break;
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            };

            //read message from server
            Thread reader = new Thread() {
                @Override
                public void run() {
                    String tmp;
                    while (true) {
                        try {
                            tmp = br.readLine();
                            if ("stop".equals(tmp)) {
                                break;
                            }
                            System.out.println(tmp);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }

                    }
                }

            };

            writer.start();
            reader.start();
            writer.join();
            reader.join();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }

    }

}
