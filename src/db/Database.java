package db;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import util.Printer;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.sql.*;

import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;    import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

public class Database {

    private Connection conn;
    private Statement stmt;



    public Database(String user, String password, String databaseServer, String schemaName) {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setEncoding("UTF-8");
        dataSource.setUser(user);
        dataSource.setPassword(password);
        dataSource.setServerName(databaseServer);
        dataSource.setDatabaseName(schemaName);

        try {
            conn = dataSource.getConnection();
            Printer.printToLog("Database connection established", Printer.LOGTYPE.INFO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResultSet runQuery(String cmd){
        ResultSet rs = null;
        try {
            Printer.printToLog("Executing " + cmd, Printer.LOGTYPE.SQL);
            stmt = conn.createStatement();
            stmt.execute(cmd);
            rs = stmt.getResultSet();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(cmd);
            Printer.printException(e);
        }
        return rs;
    }

    public void runBigInsert(String cmd, String param){
        try{
            Printer.printToLog(cmd, Printer.LOGTYPE.DEBUG);
            PreparedStatement ps = conn.prepareStatement(cmd);
            ps.setString(1, param);
            ps.executeUpdate();
        } catch (Exception e){
            e.printStackTrace();
            System.err.println(cmd);
            System.err.println(param);
            Printer.printError(cmd);
            Printer.printException(e);
        }
    }

    public void runInsertNoDuplicateError(String cmd) {
        try {
            Printer.printToLog("Executing " + cmd, Printer.LOGTYPE.SQL);
            stmt = conn.createStatement();
            stmt.executeUpdate(cmd);
        } catch (MySQLIntegrityConstraintViolationException e){
            if(e.getErrorCode() != 1062){
                e.printStackTrace();
                Printer.printException(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(cmd);
            Printer.printException(e);
        }
    }

    public void runInsert(String cmd) {
        try {
            Printer.printToLog("Executing " + cmd, Printer.LOGTYPE.SQL);
            stmt = conn.createStatement();
            stmt.executeUpdate(cmd);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(cmd);
            Printer.printException(e);
        }
    }

    public void closeConnections() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

