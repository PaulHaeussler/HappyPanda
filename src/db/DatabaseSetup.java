package db;

import main.Main;
import util.Utility;

import java.sql.ResultSet;

public class DatabaseSetup {

    public static void checkSetup() throws Exception {

        if(!checkForTable("imgs")) createImgsTable();

    }

    private static boolean checkForTable(String tableName) throws Exception {
        ResultSet rs = Main.db.runQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + Main.db_schema + "' AND TABLE_NAME = '" + tableName + "';");
        return Utility.getSize(rs) > 0;
    }

    private static void createImgsTable(){

    }
}
