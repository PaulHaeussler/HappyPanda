package main;

import db.Database;
import db.DatabaseSetup;
import exh.ExhentaiConnection;
import exh.ExhentaiParser;
import util.Printer;
import util.Utility;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static Database db;
    public static ExhentaiConnection ec;
    public static ExhentaiParser ep;
    public static String repositoryPath = "";
    public static String pathToReqHeaders = "";
    public static String db_user;
    public static String db_pass;
    public static String db_host;
    public static String db_schema;

    public static HashMap<String, ArrayList<String>> cookies = new HashMap<>();

    public static boolean getFavourites = false;
    public static String getAlbum = "";


    public static void main(String[] args){
        Printer.checkSetup();
        Utility.checkStartupArgs(args);
        Utility.readReqHeaders();
        try{
            db = new Database(db_user, db_pass, db_host, db_schema);
            ec = new ExhentaiConnection();
            ep = new ExhentaiParser();
            DatabaseSetup.checkSetup();

            if(!getAlbum.equals("")) ep.getAlbum(Main.getAlbum);
            if(getFavourites) ep.getFavs();

        } catch (Exception e){
            e.printStackTrace();
            Printer.printException(e);
        }
    }
}

