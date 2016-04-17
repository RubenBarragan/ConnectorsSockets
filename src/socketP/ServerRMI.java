package socketP;

import common.RMI_Interface;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ServerRMI implements RMI_Interface {

    private String externalIP;
    private String localIP;
    private int localPort = 1099;

    public ServerRMI(String _myIP , String _externalIP) {
        this.localIP = _myIP; 
        this.externalIP = _externalIP;        
    }

    public String sayHello() {
        return "RMI status ... OK";
    }

    public String selectAll() {
        String returnedQuery = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from devices");
            while (rs.next()) {
                returnedQuery += rs.getInt(1) + "  " + rs.getString(2) + "  " + rs.getString(3) + " " + rs.getString(4) + " " + rs.getString(5);
            }
            con.close();
        } catch (SQLException ex) {
            System.out.println("Select All ... FAILED");
        }
        return returnedQuery;
    }

    
public int selectRow(String id, String otherDate) {
        // 0 = no existe 1 = si existe y hacer cambio 2= si existe y no hacer cambio
        int returnedQuery = 0;
        String theDate = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from devices where id_bluetooth = '" + id + "'");
            while (rs.next()) {
                theDate = rs.getString(5);
                returnedQuery = compareDates(theDate, otherDate);
            }
            con.close();
        } catch (SQLException ex) {
            System.out.println("Select Row ... FAILED");
        }
        return returnedQuery;
    }

    public int compareDates(String theDate, String otherDate) {
        int result = -1;
        Timestamp timestamp2 = null;
        Timestamp timestamp = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parsedDate = dateFormat.parse(theDate);
            timestamp = new java.sql.Timestamp(parsedDate.getTime());
            Date parsedDate2 = dateFormat.parse(otherDate);
            timestamp2 = new java.sql.Timestamp(parsedDate2.getTime());

            if (timestamp.before(timestamp2)) {
                result = 1;
            } else {
                result = 2;
            }

        } catch (Exception e) {//this generic but you can control another types of exception
            System.out.println("Compare Dates ... FAILED");
        }
        return result;
    }

    @Override
    public String insertRow(String ibt, String name, String lugar, String datetime, String password) {
        String returnedQuery = "Cosa";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            Statement stmt = con.createStatement();
            stmt.executeUpdate("INSERT INTO `locator`.`devices` (`id_bluetooth`, `name`, `lugar`, `datetime`, `password`) VALUES ('" + ibt + "', '" + name + "', '" + lugar + "', '" + datetime + "','" + password + "')");
            con.close();
        } catch (SQLException ex) {
            System.out.println("Insert Row ... FAILED");
        }
        return returnedQuery;
    }
 
    @Override
    public String updateRow(String ibt, String lugar, String datetime, String password) {
        String returnedQuery = "";
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            Statement stmt = con.createStatement();
            stmt.executeUpdate("UPDATE `devices` SET `lugar`='" + lugar + "',`datetime`='" + datetime + "',`password`='" + password + "'  WHERE id_bluetooth='" + ibt + "'");
            con.close();
        } catch (SQLException ex) {
            System.out.println("Update Row ... FAILED");
        }
        return returnedQuery;
    }

    @Override
    public void recoveryBD(String ibt, String name, String lugar, String datetime, String pass) {
        int result = exists_idBT(ibt, datetime);
        if (result == 1) {
            updateRow(ibt, lugar, datetime, pass);
        } else if (result == 0) {
            insertRow(ibt, name, lugar, datetime, pass);
        }
    }

    public int exists_idBT(String id, String otherDate) {
        int s = selectRow(id, otherDate);
        return s;
    }

    public static void testBDConnection() {
        ConnectBD cbd = new ConnectBD();
        Connection con = null;

        con = cbd.connectBD();
        while (con == null) {
            con = cbd.connectBD();
        }

    }

   public boolean isEmpty() {
        boolean isEmpty = false;

        ConnectBD cbd = new ConnectBD();
        Connection con = null;
        try {
            con = cbd.connectBD();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM `devices`");
            if (!rs.isBeforeFirst()) {
                System.out.println("No data");
                isEmpty = true;
            }
            con.close();
        } catch (SQLException ex) {
            System.out.println("Is Empty ... FAILED");
        }
        return isEmpty;
    }

    @Override
    public void giveMeYourBD() {
        ResultSet rs = null;
        ConnectBD cbd = new ConnectBD();
        try {
            Connection con = cbd.connectBD();
            Statement stmt = con.createStatement();
            rs = stmt.executeQuery("select * from devices");
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(externalIP, 1099);
                RMI_Interface stub = (RMI_Interface) registry.lookup("rmi://" + externalIP + ":1099/RMI_Interface");
                while (rs.next()) {
                    stub.recoveryBD(rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),rs.getString(6));
                }
            } catch (RemoteException ex) {
                System.out.println("Give me you BD ... FAILED ...  remote Exception");
            } catch (NotBoundException ex) {
                System.out.println("Give me you BD ... FAILED ... not bound exception");
            } catch (SQLException ex) {
                System.out.println("Give me you BD ... FAILED ... no DB connection");
            }
            con.close();
        } catch (SQLException ex) {
            System.out.println("Give me you BD ... FAILED ... no DB connection");
        }
    }

    public void startServer() {
            testBDConnection();
    }
}
