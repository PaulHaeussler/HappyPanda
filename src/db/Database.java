package db;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import exh.ExhentaiAlbum;
import exh.ExhentaiImage;
import main.Main;
import util.Printer;
import util.Utility;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    public void addAlbum(ExhentaiAlbum ea) throws Exception {
        String fav_id = "0";
        if(ea.fav_id != null){
            fav_id = ea.fav_id;
        }
        runInsertNoDuplicateError("INSERT INTO " + Main.db_schema + ".albums(ex_id, album_name, album_name_jp, posted, parent, language, file_size, length, favourited, rating_total, rating_avg, category, uploader, fav_id, added) VALUES ('" +
                ea.ex_id + "', '" +
                strIfNull(ea.album_name) + "', '" +
                strIfNull(ea.album_name_jp) + "', '" +
                ea.posted + "', '" +
                ea.parent + "', '" +
                ea.language + "', '" +
                ea.file_size + "', " +
                ea.length + ", " +
                ea.favourited + ", " +
                ea.rating_total + ", " +
                ea.rating_avg.replace(",", ".") + ", '" +
                ea.category + "', '" +
                ea.uploader + "', " +
                fav_id + ", '" +
                Printer.getTimeNow() + "');"
        );
        ResultSet rs = runQuery("SELECT album_id FROM " + Main.db_schema + ".albums WHERE ex_id='" + ea.ex_id + "';");
        rs.next();
        int albumId = rs.getInt("album_id");
        addImages(ea.images);
        linkImgs(ea.images, albumId);
        addTags(ea.tags, albumId);
    }

    private String strIfNull(String str){
        if(str == null){
            return "";
        } else {
            return str;
        }
    }

    public void addTags(HashMap<String, String> tags, int album_id) throws Exception {
        for(Map.Entry<String, String> entry : tags.entrySet()){
            int id = getTagId(entry.getKey());
            runInsertNoDuplicateError("INSERT INTO " + Main.db_schema + ".alb_tag(tag_id, album_id, tag_category) VALUES(" + id + ", " + album_id + ",'" + entry.getValue() + "');");
        }
    }

    public int getTagId(String tagName) throws Exception {
        ResultSet rs = runQuery("SELECT tag_id FROM " + Main.db_schema + ".tags WHERE tag_name='" + tagName + "';");
        if(!(Utility.getSize(rs) > 0)){
            runInsertNoDuplicateError("INSERT INTO " + Main.db_schema + ".tags(tag_name) VALUES('" + tagName + "');");
            rs = runQuery("SELECT tag_id FROM " + Main.db_schema + ".tags WHERE tag_name='" + tagName + "';");
        }
        rs.next();
        return rs.getInt("tag_id");
    }

    public ArrayList<ExhentaiImage> addImages(ArrayList<ExhentaiImage> imgs) throws Exception {
        ArrayList<ExhentaiImage> result = new ArrayList<>();
        for(ExhentaiImage img : imgs){
            runInsertNoDuplicateError("INSERT INTO " + Main.db_schema + ".imgs(ex_id, img_page, file_size, img_width, img_height, file_type, added) VALUES('" +
                    img.ex_id + "', '" +
                    img.img_page + "', '" +
                    img.filesize + "', '" +
                    img.img_width + "', '" +
                    img.img_height + "', '" +
                    img.file_type + "', '" +
                    Printer.getTimeNow() + "');");
            ResultSet rs = runQuery("SELECT img_id FROM " + Main.db_schema + ".imgs WHERE ex_id='" + img.ex_id + "';");
            rs.next();
            img.img_id = rs.getString("img_id");
            result.add(img);
        }
        return result;
    }

    public void linkImgs(ArrayList<ExhentaiImage> imgs, int album_id){
        for(ExhentaiImage img : imgs){
            runInsertNoDuplicateError("INSERT INTO " + Main.db_schema + ".alb_img(img_id, album_id, order_pos) VALUES(" + img.img_id + ", " + album_id + ", " + img.order_pos + ");");
        }
    }

    public int getFavId(String favName) throws Exception {
        ResultSet rs = runQuery("SELECT fav_id FROM " + Main.db_schema + ".favs WHERE fav_name='" + favName + "';");
        if(!(Utility.getSize(rs) > 0)){
            addNewFav(favName);
            rs = runQuery("SELECT fav_id FROM " + Main.db_schema + ".favs WHERE fav_name='" + favName + "';");
        }
        rs.next();
        return rs.getInt("fav_id");
    }

    private void addNewFav(String favName){
        runInsertNoDuplicateError("INSERT INTO " + Main.db_schema + ".favs(fav_name) VALUES('" + favName + "');");
    }
}

