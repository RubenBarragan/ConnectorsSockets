/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

/**
 *
 * @author Pedro
 */
public class Server_Socket extends Thread {

    ServerSocket theServer;
    String externalIP = "";
    String localIP = "";
    int counter = 0;
    private DataOutputStream toClient;
    private DataInputStream fromClient;
    boolean RMIconnected = false;
    boolean sendBD = false;
    ConnectBD cbd;
    Connection conn;
    int port;
    int backupLapse = 5;

    public Server_Socket(int _puerto, String externalIP, String _localIP) {
        this.externalIP = externalIP;
        this.port = _puerto;
        this.localIP = _localIP;
        this.start();
    }

    public void run() {

        try {
            theServer = new ServerSocket(port);
            System.out.println("Server socket initializing in port " + port + "... [OK]");

            //try {
            cbd = new ConnectBD();
            conn = cbd.connectBD();

            while (true) {
                Socket theClient;
                System.out.println("Waiting for connection in port "+port+"...[OK]");
                theClient = theServer.accept();

                System.out.println("New incoming connection " + theClient);
                ((Server_Thread) new Server_Thread(theClient, externalIP, this)).start();
            }
        } catch (IOException ex) {
            System.out.println("Server socket initializing ... FAILED.. The port is not available..");
        }
    }
}
