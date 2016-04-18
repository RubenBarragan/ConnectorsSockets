/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketP;

import java.sql.Connection;

/**
 *
 * @author Pedro
 */
public class Main_SP {

    static String externalIP = "192.168.1.72";
    static String localIP = "192.168.1.69";

    /**
     * @param args the command line arguments
     */
    private Main_SP() {
    }

    public static void testBDConnection() {
        ConnectBD cbd = new ConnectBD();
        Connection con = null;

        con = cbd.connectBD();
        while (con == null) {
            con = cbd.connectBD();
        }

    }

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];

        //ServerRMI theServer = new ServerRMI(localIP, externalIP);
        //theServer.startServer();
        //To connect with the RMI Server.
        
        testBDConnection();
        
        //socket para conectarse con la otra base de datos.
        Server_Socket otraBD = new Server_Socket(4053, externalIP, localIP);
        //socket para conectarse con la otra base de datos.
        Server_Socket otraBDrecovery = new Server_Socket(4052, externalIP, localIP);
        
        //socket para recibir peticiones
        Server_Socket peticiones = new Server_Socket(4050, externalIP, localIP);
        //Socket to receive updates.
        Server_Socket actualizadorBT = new Server_Socket(4051, externalIP, localIP);
    }
}