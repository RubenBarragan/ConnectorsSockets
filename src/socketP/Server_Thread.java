/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package socketP;

import common.RMI_Interface;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

/**
 *
 * @author Pedro
 */
public class Server_Thread extends Thread {

    private Socket theClient;
    private DataOutputStream toClient;
    private DataInputStream fromClient;
    private String externalIP;
    private Server_Socket PreviusClass;

    public Server_Thread(Socket _theClient, String _address, Server_Socket _serverSocketClass) {
        this.theClient = _theClient;
        this.externalIP = _address;
        this.PreviusClass = _serverSocketClass;

        try {
            toClient = new DataOutputStream(_theClient.getOutputStream());
            fromClient = new DataInputStream(_theClient.getInputStream());
        } catch (IOException ex) {
            System.out.println("Initializing input and output stream ..... FAILED");
        }
    }

    public void sincronizarBDsEnviar() {
        ((ToDBconnection) new ToDBconnection("synchronize", externalIP, "", "", "", "", "")).start();
    }

    public void sincronizarBDsDeVuelta() {
        ((ToDBconnection) new ToDBconnection("synchronize", externalIP, "vuelta", "", "", "", "")).start();
    }

    public void disconnectClient() {
        try {
            theClient.close();
            System.out.println("closed socket: " + theClient.toString());
        } catch (IOException ex) {
            System.out.println("Closing Socket " + theClient.toString() + " ... FAILED");
        }
    }

    public String insertPerson(String ibt, String name, String password) {

        String returnedQuery = "Acnowledge";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();
            //devices is the table's name.
            stmt.executeUpdate("INSERT INTO `locator`.`devices` (`id_bluetooth`, `name`, `password`) VALUES ('" + ibt + "', '" + name + "', '" + password + "')");
            System.out.println("Person inserted");

            con.close();
        } catch (SQLException ex) {
            returnedQuery = "Error";
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnedQuery;
    }

    public boolean checkID(String idBT, String name, String password) {
        boolean trueID = false;

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + idBT + "'");

            while (rs.next()) {
                if (rs.getString(2).equals(idBT) && rs.getString(3).equals(name) && rs.getString(6).equals(password)) {
                    trueID = true;
                }
            }
            con.close();

            return trueID;
        } catch (SQLException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return trueID;
    }

    public String buscarPersona(String name) {
        String ID_Info = "NoExiste";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection sqlConn = cbd.connectBD();
            Statement stmt = sqlConn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from devices where name = '" + name + "'");
            while (rs.next()) {
                ID_Info = rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5) + "//";
            }
            sqlConn.close();
        } catch (SQLException ex) {
            ID_Info = "Cambiar";
            System.out.println("Buscar Persona ... FAILED");
        }
        return ID_Info;
    }

    public String searchArea(String area) {
        String data = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where lugar = '" + area + "'");

            while (rs.next()) {
                data += rs.getString(2) + "#" + rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5) + "#";
            }
            data = deleteLastChar(data);
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }

        return data; //id_bluetooth#name#lugar#date#id_bluetooth#name#lugar#date...
    }

    public String deleteLastChar(String s) {
        if (!s.isEmpty()) {
            return s.substring(0, s.length() - 1);
        } else {
            return s;
        }
    }

    public String myUbicacion(String idBT) {
        String ID_Info = "NoExiste";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection sqlConn = cbd.connectBD();
            Statement stmt = sqlConn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + idBT + "'");
            while (rs.next()) {
                ID_Info = rs.getString(4);
            }
            sqlConn.close();
        } catch (SQLException ex) {
            ID_Info = "Cambiar";
            System.out.println("Buscar Persona ... FAILED");
        }
        return ID_Info;
    }

    public String buscarTodos() {
        String IDs_Info = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection sqlConn = cbd.connectBD();
            Statement stmt = sqlConn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from devices");
            int i = 0;
            while (rs.next()) {
                IDs_Info = IDs_Info + rs.getString(3) + "    " + rs.getString(4) + "\n" + rs.getString(5) + "//";
                i++;
            }
            if (IDs_Info.equals("")) {
                IDs_Info = "NoInfo";
            }
            sqlConn.close();
        } catch (SQLException ex) {
            IDs_Info = "Cambiar";
            System.out.println("Buscar Todos ... FAILED");
        }
        return IDs_Info;
    }

    public boolean checkIDexist(String idBT, String name, String password) {
        boolean trueID = false;

        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            //stmt is the statement's object. It's used to create statements or queries.
            Statement stmt = con.createStatement();

            //devices is the table's name.
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + idBT + "' or name='" + name + "'");

            if (rs.next()) {
                trueID = true;
            }
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(Server_Thread.class.getName()).log(Level.SEVERE, null, ex);
        }

        return trueID;
    }

    public String getDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        //System.out.println(sdf.format(cal.getTime()));
        return sdf.format(cal.getTime());
    }

    @Override
    public void run() {
        String msg = "";
        try {
            //Receive the incomming message from the client
            msg = fromClient.readUTF();
            System.out.println("Message Recived: " + msg);
            String[] dataSet = msg.split("#");

            switch (dataSet[0]) {
                case "ingresar":
                    //Check if the user and password match.
                    if (checkID(dataSet[1], dataSet[2], dataSet[3])) {
                        toClient.writeUTF("ingresa");
                    } else {
                        toClient.writeUTF("noIngresa");
                    }
                    break;
                case "registrar":
                    //0_registrar  1_idbt   2_nombre    3_contrasena
                    //Check if the person already exists.
                    if (!checkIDexist(dataSet[1], dataSet[2], dataSet[3])) {
                        insertPerson(dataSet[1], dataSet[2], dataSet[3]);
                        toClient.writeUTF("signUp");
                        ((ToDBconnection) new ToDBconnection("registerExternalBD", externalIP, dataSet[1], dataSet[2], "", "", dataSet[3])).start();
                    } else {
                        toClient.writeUTF("userExists");
                    }
                    break;
                case "searchPerson":
                    String persona = buscarPersona(dataSet[1]);
                    toClient.writeUTF(persona); //notFound if user doesn't exist and id_bluetooth#name#lugar#date
                    break;
                case "searchAll":
                    String Todos = buscarTodos();
                    toClient.writeUTF(Todos); //id_bluetooth#name#lugar#date#id_bluetooth#name#lugar#date...
                    break;
                case "searchArea":
                    String userInArea = searchArea(dataSet[1]);
                    toClient.writeUTF(userInArea); //id_bluetooth#name#lugar#date#id_bluetooth#name#lugar#date...
                    break;
                /*
                 case "Servicios":
                 String myUbicacion = myUbicacion(argsw[1]);

                 switch (myUbicacion) {
                 case "Laboratorio":
                 toClient.writeUTF("Laboratorio\n//nImpresora//Biblioteca Virtual//Escaner//");
                 break;
                 case "Biblioteca":
                 toClient.writeUTF("Bilbioteca\n//Impresora//Biblioteca Virtual//Escaner//");
                 break;
                 case "Comedor":
                 toClient.writeUTF("Comedor\n//Biblioteca Virtual//");
                 break;
                 case "Salon":
                 toClient.writeUTF("Salon\n//Biblioteca Virtual//Proyector//");
                 break;
                 default:
                 toClient.writeUTF("Cambiar");
                 break;
                 }
                 break;
                 */
                case "SincVuelta":
                    sincronizarBDsDeVuelta();
                    toClient.writeUTF("Cambiar");
                    break;
                case "localSynchronize":
                    ServerRMI theServer = new ServerRMI("", externalIP);
                    //ToDBconnection(    0 _accion,   1 _externalIP,      2 _idBT,     3 _nombre,      4 _lugar,       5 _fecha,       6 _pass)
                    theServer.recoveryBD(dataSet[2], dataSet[3], dataSet[4], dataSet[5], dataSet[6]);

                    toClient.writeUTF("Cambiar");
                    break;

                case "updateExternalDB":
                    //ToDBconnection(0 _accion,      1 _externalIP,      2 _idBT,     3 _nombre,     4 _lugar,     5 _fecha,      6 _pass)
                    if (PreviusClass.conn != null) {
                        Statement stmt = PreviusClass.conn.createStatement();
                        stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + dataSet[4] + "',`datetime`='" + dataSet[5] + "' WHERE id_bluetooth='" + dataSet[2] + "'");
                        System.out.println("Local from eternal update performed ... OK.");
                        toClient.writeUTF("Acknowledge");
                    } else {
                        Connection newCon = PreviusClass.cbd.connectBD();
                        PreviusClass.conn = newCon;
                        Statement stmt = PreviusClass.conn.createStatement();
                        stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + dataSet[4] + "',`datetime`='" + dataSet[5] + "' WHERE id_bluetooth='" + dataSet[2] + "'");
                        toClient.writeUTF("Acknowledge");
                    }
                    break;

                case "registerExternalBD":
                    //checkIDexist   idbt   nombre    contrasena  pide...
                    //ToDBconnection(0 _accion,      1 _externalIP,      2 _idBT,     3 _nombre,     4 _lugar,     5 _fecha,      6 _pass)

                    if (!checkIDexist(dataSet[2], dataSet[3], dataSet[6])) {
                        insertPerson(dataSet[2], dataSet[3], dataSet[6]);
                        toClient.writeUTF("signUp");
                        //toClient.writeUTF("Registrado");
                    } else {
                        toClient.writeUTF("userExists");
                        //toClient.writeUTF("yaExiste");
                    }
                    break;

                case "updateLocation":
                    //System.out.println("Message Recived: " + msg);
                    String ibt = dataSet[1];
                    String lugar = dataSet[2];
                    String datetime = getDate();
                    //Create the query to the local database.
                    if (PreviusClass.conn != null) {
                        Statement stmt = PreviusClass.conn.createStatement();
                        stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + lugar + "',`datetime`='" + datetime + "' WHERE id_bluetooth='" + ibt + "'");
                        ((ToDBconnection) new ToDBconnection("updateExternalDB", externalIP, ibt, "", lugar, datetime, "")).start(); //Check this.
                        System.out.println("Local update performed ... OK.");
                        toClient.writeUTF("Acknowledge");
                    } else {
                        Connection newCon = PreviusClass.cbd.connectBD();
                        PreviusClass.conn = newCon;
                        Statement stmt = PreviusClass.conn.createStatement();
                        stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + lugar + "',`datetime`='" + datetime + "' WHERE id_bluetooth='" + ibt + "'");
                        ((ToDBconnection) new ToDBconnection("updateExternalDB", externalIP, dataSet[1], "", dataSet[2], datetime, "")).start(); //Check this.
                        toClient.writeUTF("Acknowledge");
                    }

                    PreviusClass.backupLapse++;

                    if (PreviusClass.backupLapse > 15) {
                        PreviusClass.backupLapse = 0;
                        sincronizarBDsEnviar();
                    }

                    break;

            }

        } catch (IOException ex) {
            System.out.println("External query ... FAILED");

        } catch (SQLException ex) {
            //DB Conection failed... trying to re conect
            try {
                System.out.println("DB connection ... FAILED");
                toClient.writeUTF("No DB Connection");
                Connection conn = PreviusClass.cbd.connectBD();
                PreviusClass.conn = conn;
            } catch (IOException ex1) {
                System.out.println("Cambiar");
            }
        }

        disconnectClient();
    }
}
