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
        ((ToDBconnection) new ToDBconnection("Sincronizar", externalIP, "", "", "", "", "")).start();
    }

    public void sincronizarBDsDeVuelta() {
        ((ToDBconnection) new ToDBconnection("Sincronizar", externalIP, "vuelta", "", "", "", "")).start();
    }

    public void disconnectClient() {
        try {
            theClient.close();
            System.out.println("closed socket: " + theClient.toString());
        } catch (IOException ex) {
            System.out.println("Closing Socket " + theClient.toString() + " ... FAILED");
        }
    }

    public String insertPersonLocal(String ibt, String name, String password) {
        String Act = "Acnowledge";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection sqlConn = cbd.connectBD();
            Statement stmt = sqlConn.createStatement();
            stmt.executeUpdate("INSERT INTO `locator`.`devices` (`id_bluetooth`, `name`, `password`) VALUES ('" + ibt + "', '" + name + "', '" + password + "')");
            sqlConn.close();
        } catch (SQLException ex) {
            Act = "FalloLocal";
            System.out.println("Inserting Person ... FAILED");
        }
        return Act;
    }

    public String checkID(String idBT, String name, String password) {
        String trueID = "false";

        ConnectBD cbd = new ConnectBD();
        try {
            Connection sqlConn = cbd.connectBD();
            Statement stmt = sqlConn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + idBT + "'");
            while (rs.next()) {
                if (rs.getString(2).equals(idBT) && rs.getString(3).equals(name) && rs.getString(6).equals(password)) {
                    trueID = "true";
                }
            }
            sqlConn.close();
            return trueID;
        } catch (SQLException ex) {
            trueID = "Cambiar";
            System.out.println("Check ID ... FAILED");
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

    public String BuscarPorSeccion(String lugar) {
        String ID_Info = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection sqlConn = cbd.connectBD();
            Statement stmt = sqlConn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from devices where lugar = '" + lugar + "'");
            while (rs.next()) {
                ID_Info = ID_Info + rs.getString(3) + "#" + rs.getString(4) + "#" + rs.getString(5) + "//";
            }
            if (ID_Info.equals("")) {
                ID_Info = "NoExiste";
            }
            sqlConn.close();
        } catch (SQLException ex) {
            ID_Info = "Cambiar";
            System.out.println("Buscar Persona ... FAILED");
        }
        return ID_Info;
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

    public String checkIDexist(String idBT, String name, String password) {
        String trueID = "false";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + idBT + "'");
            if (rs.next()) {
                trueID = "true";
            }
            Statement stmt2 = con.createStatement();
            ResultSet rs2 = stmt2.executeQuery("select * from devices where name = '" + name + "'");
            if (rs2.next()) {
                trueID = "true";
            }
            con.close();
        } catch (SQLException ex) {
            trueID = "Cambiar";
            System.out.println("Check ID Exist ... FAILED");
        }
        return trueID;
    }

    @Override
    public void run() {
        String incommingMsg = "";
        try {
            //Receive the incomming message from the client
            incommingMsg = fromClient.readUTF();
            System.out.println("Message Recived: " + incommingMsg);
            String[] argsw = incommingMsg.split("#");

            switch (argsw[0]) {
                case "Ingresar":
                    String validID = checkID(argsw[1], argsw[2], argsw[3]);
                    if (validID.equals("true")) {
                        toClient.writeUTF("Ingresa");
                    } else if (validID.equals("false")) {
                        toClient.writeUTF("noIngresa");
                    } else if (validID.equals("Cambiar")) {
                        toClient.writeUTF("Cambiar");
                    }
                    break;
                case "Registrar":
                    //0_registrar  1_idbt   2_nombre    3_contrasena
                    String existID = checkIDexist(argsw[1], argsw[2], argsw[3]);
                    if (existID.equals("false")) {
                        insertPersonLocal(argsw[1], argsw[2], argsw[3]);
                        toClient.writeUTF("Registrado");
                        //                      ToDBconnection( _accion,       _ externalIP, _idBT,   _nombre,  _lugar,  _fecha,  _pass)
                        ((ToDBconnection) new ToDBconnection("RegistrarExterno", externalIP, argsw[1], argsw[2], "", "", argsw[3])).start();

                    } else if (existID.equals("true")) {
                        toClient.writeUTF("yaExiste");
                    } else if (existID.equals("Cambiar")) {
                        toClient.writeUTF("Cambiar");
                    }
                    break;
                case "BuscarPersona":
                    String personaInfo = buscarPersona(argsw[1]);
                    toClient.writeUTF(personaInfo);
                    break;
                case "BuscarTodos":
                    String Todos = buscarTodos();
                    toClient.writeUTF(Todos);
                    break;
                case "Seccion":

                    String Seccion = BuscarPorSeccion(argsw[1]);
                    toClient.writeUTF(Seccion);
                    break;

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
                    
                case "SincVuelta":
                    sincronizarBDsDeVuelta();
                    toClient.writeUTF("Cambiar");
                    break;

                case "SincronizarLocal":
                    ServerRMI theServer = new ServerRMI("", externalIP);
                    //ToDBconnection(    0 _accion,   1 _externalIP,      2 _idBT,     3 _nombre,      4 _lugar,       5 _fecha,       6 _pass)
                    theServer.recoveryBD(argsw[2], argsw[3], argsw[4], argsw[5], argsw[6]);

                    toClient.writeUTF("Cambiar");
                    break;

                case "ActualizarExterno":
                    //ToDBconnection(0 _accion,      1 _externalIP,      2 _idBT,     3 _nombre,     4 _lugar,     5 _fecha,      6 _pass)
                    if (PreviusClass.conn != null) {
                        Statement stmt = PreviusClass.conn.createStatement();
                        stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + argsw[4] + "',`datetime`='" + argsw[5] + "' WHERE id_bluetooth='" + argsw[2] + "'");
                        System.out.println("Local from eternal update performed ... OK.");
                        toClient.writeUTF("Acknowledge");
                    } else {
                        Connection newCon = PreviusClass.cbd.connectBD();
                        PreviusClass.conn = newCon;
                        Statement stmt = PreviusClass.conn.createStatement();
                        stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + argsw[4] + "',`datetime`='" + argsw[5] + "' WHERE id_bluetooth='" + argsw[2] + "'");
                        toClient.writeUTF("Acknowledge");
                    }
                    break;

                case "RegistrarExterno":
                    //checkIDexist   idbt   nombre    contrasena  pide...
                    //ToDBconnection(0 _accion,      1 _externalIP,      2 _idBT,     3 _nombre,     4 _lugar,     5 _fecha,      6 _pass)
                    String itExistID = checkIDexist(argsw[2], argsw[3], argsw[6]);
                    if (itExistID.equals("false")) {
                        insertPersonLocal(argsw[2], argsw[3], argsw[6]);
                        toClient.writeUTF("Registrado");
                    } else if (itExistID.equals("true")) {
                        toClient.writeUTF("yaExiste");
                    } else if (itExistID.equals("Cambiar")) {
                        toClient.writeUTF("Cambiar");
                    }
                    break;

                case "Actualizar":
                    java.util.Date date = new java.util.Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm:ss");
                    String formattedDate = sdf.format(date);

                    //Create the query to the local database.
                    if (PreviusClass.conn != null) {
                        Statement stmt = PreviusClass.conn.createStatement();
                        if (argsw.length > 2) {
                            stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + argsw[2] + "',`datetime`='" + formattedDate + "' WHERE id_bluetooth='" + argsw[1] + "'"
                            );
                        }
                        //                      ToDBconnection( _accion,       _ externalIP, _idBT,   _nombre,  _lugar,  _fecha,  _pass)
                        ((ToDBconnection) new ToDBconnection("ActualizarExterno", externalIP, argsw[1], "", argsw[2], formattedDate, "")).start();
                        System.out.println("Local update performed ... OK.");
                        toClient.writeUTF("Acknowledge");
                    } else {
                        Connection newCon = PreviusClass.cbd.connectBD();
                        PreviusClass.conn = newCon;
                        Statement stmt = PreviusClass.conn.createStatement();
                        if (argsw.length > 2) {
                            stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + argsw[2] + "',`datetime`='" + formattedDate + "' WHERE id_bluetooth='" + argsw[1] + "'"
                            );
                        }
                        //                      ToDBconnection( _accion,       _ externalIP, _idBT,   _nombre,  _lugar,  _fecha,  _pass)
                        ((ToDBconnection) new ToDBconnection("ActualizarExterno", externalIP, argsw[1], "", argsw[2], formattedDate, "")).start();
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
                toClient.writeUTF("Cambiar");
                Connection conn = PreviusClass.cbd.connectBD();
                PreviusClass.conn = conn;
            } catch (IOException ex1) {
                System.out.println("Cambiar");
            }
        }

        disconnectClient();
    }
}
