/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketP;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pedro
 */
public class ToDBconnection extends Thread {

    String externalIP;
    String action;
    String ibt;
    String name;
    String lugar;
    String datetime;
    String pass;

    //ToDBconnection(    0 _accion,   1 _externalIP,      2 _idBT,     3 _nombre,      4 _lugar,       5 _fecha,       6 _pass)
    public ToDBconnection(String _accion, String _externalIP, String _idBT, String _nombre, String _lugar, String _fecha, String _pass) {
        this.action = _accion;
        this.externalIP = _externalIP;
        this.ibt = _idBT;
        this.name = _nombre;
        this.lugar = _lugar;
        this.datetime = _fecha;
        this.pass = _pass;
    }

    @Override
    public void run() {
        String msgToSend = "";
        String response = "";
        try {
            if (!action.equals("synchronize")) {
                Socket socket = new Socket();
                DataOutputStream dataOutputStream = null;
                DataInputStream dataInputStream = null;
                socket.connect(new InetSocketAddress(externalIP, 4053), 10000);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                msgToSend = action + "#" + externalIP + "#" + ibt + "#" + name + "#" + lugar + "#" + datetime + "#" + pass;
                dataOutputStream.writeUTF(msgToSend);

                response = dataInputStream.readUTF();

                socket.close();

                switch (response) {
                    case "Acknowledge":
                        System.out.println("External query sucessfully...[OK]");
                        break;
                    case "Fallo":
                        System.out.println("External query sucessfully...[ERROR]");
                        break;
                }
            } else if (action.equals("synchronize")) {
                ResultSet rs = null;
                try {
                    ConnectBD cbd = new ConnectBD();
                    Connection con = cbd.connectBD();
                    Statement stmt = con.createStatement();

                    rs = stmt.executeQuery("select * from devices");
                    while (rs.next()) {
                        Socket socket = new Socket();
                        DataOutputStream dataOutputStream = null;
                        DataInputStream dataInputStream = null;
                        socket.connect(new InetSocketAddress(externalIP, 4052), 10000);
                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataInputStream = new DataInputStream(socket.getInputStream());
                        msgToSend = "localSynchronize" + "#" + externalIP + "#" + rs.getString(2) + "#" + rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5) + "#" + rs.getString(6);
                        dataOutputStream.writeUTF(msgToSend);
                        response = dataInputStream.readUTF();
                        socket.close();
                    }

                    con.close();
                    if (!ibt.equals("vuelta")) {
                        ((ToDBconnection) new ToDBconnection("SincVuelta#", externalIP, "", "", "", "", "")).start();
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(ToDBconnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (IOException ex) {
            System.out.println("Fallo conexion a socket, BD externa ... Socket P");
        }
    }
}
