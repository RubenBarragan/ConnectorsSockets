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

    static String externalIP = "10.0.5.232";
    static String localIP = "10.0.5.127";

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
        
        //socket para recibir peticiones
        Server_Socket peticiones = new Server_Socket(4050, externalIP, localIP);

        //Socket para recibir actualizaciones
        Server_Socket actualizadorBT = new Server_Socket(4060, externalIP, localIP);
        
        //socket para conectarse con la otra base de datos.
        Server_Socket otraBD = new Server_Socket(4070, externalIP, localIP);
        
        //socket para conectarse con la otra base de datos.
        Server_Socket otraBDrecovery = new Server_Socket(4080, externalIP, localIP);
    }
}
