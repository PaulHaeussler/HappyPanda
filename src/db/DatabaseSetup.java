package db;

import main.Main;
import util.Printer;
import util.Utility;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DatabaseSetup {

    public static void checkSetup() throws Exception {
        Printer.printToLog("Checking database table integrity and creating new tables if necessary...", Printer.LOGTYPE.INFO);
        if(!checkForTable("imgs")) createImgsTable();
        if(!checkForTable("albums")) createAlbumsTable();
        if(!checkForTable("alb_img")) createAlbImgTable();
        if(!checkForTable("alb_tag")) createAlbTagTable();
        if(!checkForTable("favs")) createFavsTable();
        if(!checkForTable("tags")) {
            createTagsTable();
            insertTags();
        }
        Printer.printToLog("Database is all set up and ready!", Printer.LOGTYPE.INFO);

        checkForFavs();
    }

    private static void checkForFavs() throws Exception  {
        ResultSet rs = Main.db.runQuery("SELECT COUNT(*) AS c FROM " + Main.db_schema + ".favs;");
        rs.next();
        int count = rs.getInt("c");
        Printer.printToLog("Found " + count + " favourite categories in database", Printer.LOGTYPE.INFO);
        if(count == 0){
            Printer.printToLog("Fetching categories from exhentai...", Printer.LOGTYPE.INFO);
            ArrayList<String> favs = Main.ep.getEHFavs();
            int c = 1;
            for(String str : favs){
                insertFav(c, str);
                c++;
            }
        } else if(count < 10){
            Printer.printToLog("This is very weird... Did you edit the database?", Printer.LOGTYPE.WARNING);
        }
    }

    private static void insertFav(int count, String favName){
        Main.db.runInsert("INSERT INTO " + Main.db_schema + ".favs(fav_id, fav_name) VALUES(" + count + ",'" + favName + "');");
    }

    private static boolean checkForTable(String tableName) throws Exception {
        ResultSet rs = Main.db.runQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '" + Main.db_schema + "' AND TABLE_NAME = '" + tableName + "';");
        return Utility.getSize(rs) > 0;
    }

    private static void createImgsTable(){
        Main.db.runInsert("CREATE TABLE " + Main.db_schema + ".`imgs` (\n" +
                "  `img_id` INT NOT NULL AUTO_INCREMENT,\n" +
                "  `ex_id` VARCHAR(10) NOT NULL,\n" +
                "  `img_page` VARCHAR(600) NOT NULL,\n" +
                "  `file_size` VARCHAR(10) NOT NULL,\n" +
                "  `img_width` INT NOT NULL,\n" +
                "  `img_height` INT NOT NULL,\n" +
                "  `file_type` VARCHAR(10) NOT NULL,\n" +
                "  `added` VARCHAR(100) NOT NULL,\n" +
                "  PRIMARY KEY (`img_id`, `ex_id`),\n" +
                "  UNIQUE INDEX `img_id_UNIQUE` (`img_id` ASC),\n" +
                "  UNIQUE INDEX `ex_id_UNIQUE` (`ex_id` ASC),\n" +
                "  UNIQUE INDEX `img_page_UNIQUE` (`img_page` ASC))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4\n" +
                "COLLATE = utf8mb4_unicode_ci;");
    }

    private static void createTagsTable(){
        Main.db.runInsert("CREATE TABLE " + Main.db_schema + ".`tags` (\n" +
                "  `tag_id` INT NOT NULL AUTO_INCREMENT,\n" +
                "  `tag_name` VARCHAR(100) NOT NULL,\n" +
                "  PRIMARY KEY (`tag_id`, `tag_name`),\n" +
                "  UNIQUE INDEX `tag_id_UNIQUE` (`tag_id` ASC),\n" +
                "  UNIQUE INDEX `tag_name_UNIQUE` (`tag_name` ASC))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4\n" +
                "COLLATE = utf8mb4_unicode_ci;");
    }

    private static void createAlbumsTable(){
        Main.db.runInsert("CREATE TABLE " + Main.db_schema + ".`albums` (\n" +
                "  `album_id` INT NOT NULL AUTO_INCREMENT,\n" +
                "  `ex_id` INT NOT NULL,\n" +
                "  `album_name` VARCHAR(600) NOT NULL,\n" +
                "  `album_name_jp` VARCHAR(600) NULL,\n" +
                "  `posted` VARCHAR(20) NOT NULL,\n" +
                "  `parent` INT NULL,\n" +
                "  `language` VARCHAR(20) NOT NULL,\n" +
                "  `file_size` VARCHAR(20) NOT NULL,\n" +
                "  `length` INT NOT NULL,\n" +
                "  `favourited` INT NOT NULL,\n" +
                "  `rating_total` INT NOT NULL,\n" +
                "  `rating_avg` FLOAT NOT NULL,\n" +
                "  `rating_local` FLOAT NULL,\n" +
                "  `category` VARCHAR(10) NOT NULL,\n" +
                "  `uploader` VARCHAR(100) NOT NULL,\n" +
                "  `fav_id` INT NULL,\n" +
                "  `added` VARCHAR(45) NOT NULL,\n" +
                "  PRIMARY KEY (`album_id`, `ex_id`),\n" +
                "  UNIQUE INDEX `album_id_UNIQUE` (`album_id` ASC),\n" +
                "  UNIQUE INDEX `ex_id_UNIQUE` (`ex_id` ASC))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4\n" +
                "COLLATE = utf8mb4_unicode_ci;");
    }

    private static void createAlbImgTable(){
        Main.db.runInsert("CREATE TABLE " + Main.db_schema + ".`alb_img` (\n" +
                "  `img_id` INT NOT NULL,\n" +
                "  `album_id` INT NOT NULL,\n" +
                "  `order_pos` INT NOT NULL,\n" +
                "  PRIMARY KEY (`img_id`, `album_id`))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4\n" +
                "COLLATE = utf8mb4_unicode_ci;\n");
    }

    private static void createAlbTagTable(){
        Main.db.runInsert("CREATE TABLE " + Main.db_schema + ".`alb_tag` (\n" +
                "  `tag_id` INT NOT NULL,\n" +
                "  `album_id` INT NOT NULL,\n" +
                "  `tag_category` VARCHAR(45) NOT NULL,\n" +
                "  PRIMARY KEY (`tag_id`, `album_id`))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4\n" +
                "COLLATE = utf8mb4_unicode_ci;");
    }

    private static void createFavsTable(){
        Main.db.runInsert("CREATE TABLE " + Main.db_schema + ".`favs` (\n" +
                "  `fav_id` INT NOT NULL AUTO_INCREMENT,\n" +
                "  `fav_name` VARCHAR(100) NOT NULL,\n" +
                "  PRIMARY KEY (`fav_id`, `fav_name`))\n" +
                "ENGINE = InnoDB\n" +
                "DEFAULT CHARACTER SET = utf8mb4\n" +
                "COLLATE = utf8mb4_unicode_ci;");
    }

    private static void insertTags() throws Exception {
        ArrayList<String> tags = extractTags();
        //remove duplicates
        Set<String> set = new HashSet<>(tags);
        tags.clear();
        tags.addAll(set);
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO " + Main.db_schema + ".tags(tag_name) VALUES ");
        for(int i = 0; i < tags.size(); i++){
            sb.append("('");
            sb.append(tags.get(i));
            sb.append("')");
            if(i+1 < tags.size()) {
                sb.append(",");
            } else {
                sb.append(";");
            }
        }
        Main.db.runInsert(sb.toString());
    }

    private static ArrayList<String> extractTags() throws Exception {
        String page = Main.ec.getPage("https://ehwiki.org/wiki/Fetish_Listing");
        String[] tmp = page.split("<a href=\"/wiki/");
        ArrayList<String> result = new ArrayList<>();

        //skip first
        for(int i = 1; i < tmp.length; i++){
            String s = tmp[i].split("\"")[0];
            result.add(s);
            if(s.equals("translated")) break; //last tag
        }
        return result;
    }
}
