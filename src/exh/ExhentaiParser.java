package exh;

import main.Main;
import util.Printer;
import util.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ExhentaiParser {

    private static boolean skipping = false;
    private static int counter = 0;
    private static int page_counter = 0;

    public void getFavs() throws Exception {
        String page = Main.ec.getPage("https://exhentai.org/favorites.php");

        Printer.printToLog("\u001b[36m Going through the favorites... \u001b[0m", Printer.LOGTYPE.INFO);

        counter = 1;

        page_counter = 1;

        if(!Main.startFrom.equals("")) skipping = true;

        while(true){
            Printer.printToLog("\u001b[36m Parsing page " + page_counter + " ... \u001b[0m", Printer.LOGTYPE.INFO);
            parseFavs(page);
            String nextPage = findNext(page);
            if(Objects.equals(nextPage, "")){
                break;
            }
            page = Main.ec.getPage(nextPage);
            page_counter++;
        }
        Printer.printToLog("Finished processing all favorites, total albums processed: " + (counter - 1), Printer.LOGTYPE.INFO);
    }

    private void parseFavs(String page) throws Exception {
        String[] tmp = getPassage(page, "<table class=\"itg gltc\">", "</table>").split("<td class=\"gl3c glname\" ");

        for(int i = 1; i < tmp.length; i++){
            Printer.printToLog("Processing album " + counter + " batch " + i + "/" + (tmp.length - 1), Printer.LOGTYPE.INFO);
            counter++;
            String tmp1 = tmp[i].split("</td>")[0];
            String url = getPassage(tmp1, "<a href=\"", "\">");
            String exId = getPassage(url, "https://exhentai.org/g/", "/");
            if(exId.equals(Main.startFrom)) skipping = false;
            if(skipping) continue;
            getAlbum(url);
            Printer.printToLog("Finished parsing fav page", Printer.LOGTYPE.INFO);
        }
    }

    private String findNoOfPages(String page){
        //find no. of pages
        String tmp = getPassage(page, "<table class=\"ptt\" style=\"margin:2px auto 0px\">", "</table>");
        String[] tmp2 = tmp.split("<td onclick=\"document\\.location=this\\.firstChild\\.href\">");
        if(tmp2.length <= 1){
            return "1";
        } else {
            String tmp3 = tmp2[tmp2.length - 2];
            String[] tmp4 = tmp3.split("</a>")[0].split(">");
            return tmp4[tmp4.length - 1];
        }
    }


    private String findNext(String page){
        String result = "";
        try {
            String tmp = getPassage(page, "<a id=\"unext\" href=\"", "\">Next ></a>");
            result = tmp;
        } catch (Exception ignored){

        }

        return result;
    }

    public void getAlbum(String url) throws Exception {
        String page = Main.ec.getPage(url);
        if(page.equals("")) {
            Printer.printToLog("Received empty page, skipping...", Printer.LOGTYPE.WARNING);
            if(Main.ec.lastResponse == 404) {
                Printer.printToLog("Response for this album was 404, setting status to removed...", Printer.LOGTYPE.INFO);
                if(!Main.nodb) Main.db.setStatus(url, "removed");
            }
            return;
        }

        ExhentaiAlbum ea = parseAlbum(page, url);

        if(!Main.nodb && ea != null) Main.db.addAlbum(ea);
    }

    public ArrayList<String> getEHFavs() throws Exception {
        String page = Main.ec.getPage("https://exhentai.org/favorites.php");
        ArrayList<String> result = new ArrayList<>();

        String tmp = page.split("<div class=\"nosel\" style=\"position:relative; width:825px; margin:10px auto 5px\">")[1];
        String tmp2 = tmp.split("<div class=\"fp fps\" onclick=\"document.location='https://exhentai.org/favorites.php'\" style=\"width:140px; padding:4px 0 0; margin:5px auto 0; text-align:center; font-weight:bold; position:relative; left:-8px\">Show All Favorites</div>")[0];
        String[] tmp3 = tmp2.split("<div class=\"fp\" onclick=\"document.location='https://exhentai.org/favorites.php\\?favcat=");
        for(int i = 1; i < tmp3.length; i++){
            String tmp4 = tmp3[i].split("<div style=\"float:left; text-align:left; height:20px; padding:2px 0 0 3px\">")[1];
            result.add(tmp4.split("</div>")[0]);
        }
        return result;
    }

    private String removeIllegal(String str){
        String illegal = "<>?\"|*:\\/";
        for(char c : illegal.toCharArray()){
            str = str.replace(c, ' ');
        }
        if(str.length() > 100) str = str.substring(0, 100);
        return str;
    }

    private String removeIllegalNoTrim(String str){
        String illegal = "<>?\"|*:\\/";
        for(char c : illegal.toCharArray()){
            str = str.replace(c, ' ');
        }
        return str;
    }

    private ExhentaiAlbum parseAlbum(String page, String url) throws Exception  {
        ExhentaiAlbum result = new ExhentaiAlbum();
        result.album_url = url;

        String header = getPassage(page, "<div id" +
                "=\"gd2\">", "</div>");
        result.album_name = getPassage(header, "<h1 id=\"gn\">", "</h1>");
        result.album_name_jp = getPassage(header, "<h1 id=\"gj\">", "</h1>");

        String category = getPassage(page, "<div id=\"gdc\">", "</div>");
        result.category = category.split(">")[1];

        String uploader = getPassage(page, "<div id=\"gdn\">", "</a>");
        result.uploader = uploader.split(">")[1];

        String metadata = getPassage(page, "<div id=\"gdd\">", "</div>");
        String[] table = metadata.split("<td class=\"gdt2\">");
        result.posted = table[1].split("</td>")[0];

        String parent = table[2].split("</td>")[0];
        if(!parent.equals("None")){
            result.parent = parent.split(">")[1].split("<")[0];
        } else {
            result.parent = parent;
        }

        if(table[4].contains("<span")){
            result.language = table[4].split("&nbsp;<span")[0];
        } else {
            result.language = table[4].split("&nbsp;</td>")[0];
        }

        result.file_size = table[5].split("</td>")[0];
        result.length = table[6].split(" pages</td>")[0];
        result.favourited = table[6].split("<td class=\"gdt2\" id=\"favcount\">")[1].split(" times</td>")[0];

        String rating = getPassage(page, "<div id=\"gdr\" onmouseout=\"rating_reset\\(\\)\">", "</tbody>");
        result.rating_total = getPassage(rating, "<span id=\"rating_count\">", "</span>");
        result.rating_avg = getPassage(rating, "<td id=\"rating_label\" colspan=\"3\">Average: ", "</td>");

        String[] tmp = getPassage(page, "<div id=\"gdf\" onclick=\"return pop_fav\\(\\)\">", "</a>").split(">");
        String fav = tmp[tmp.length-1];

        if(!fav.equals(" Add to Favorites") && !Main.nodb){
            result.fav_id = Main.db.getFavId(fav) + "";
        }

        result.ex_id = getPassage(url, "exhentai.org/g/", "/");
        String[] t = url.split("/");
        if(t[t.length - 1].equals("")){
            result.ex_hash = t[t.length - 2];
        } else {
            result.ex_hash = t[t.length - 1];
        }


        result.tags = extractTags(getPassage(page, "<div id=\"taglist\"", "<div id=\"tagmenu_act\" style=\"display:none\"></div>"));


        File noTrim = new File(Main.repositoryPath + "/" + removeIllegalNoTrim(result.album_name) + "_" + result.ex_id);
        if(noTrim.exists() && result.album_name.length() > 100){
            upgradeDir(result, noTrim);
        }


        File oldDir = new File(Main.repositoryPath + "/" + removeIllegal(result.album_name) + "_" + result.ex_id);
        if(oldDir.exists()) upgradeDir(result, oldDir);



        File dir = new File(Main.repositoryPath + "/" + removeIllegal(result.album_name) + "_" + result.ex_id + "_" + result.ex_hash);
        if(Main.skipDir && dir.exists()) return null;

        if(!dir.exists()) dir.mkdir();
        Printer.printToLog(dir.listFiles().length + " files in directory, expected " + result.length, Printer.LOGTYPE.INFO);
        if(Main.skipCount && (dir.listFiles().length >= Integer.parseInt(result.length))) {
            Printer.printToLog("Skipping", Printer.LOGTYPE.DEBUG);
            return null;
        } else {
            Printer.printToLog("Entering", Printer.LOGTYPE.DEBUG);
        }

        if(!Main.checkAlbums) result.images = extractImages(page, url, result);

        return result;
    }

    private void upgradeDir(ExhentaiAlbum ea, File dir){
        String pattern = ".+_[0-9]+$";
        if(dir.getName().matches(pattern)){
            String newPath = Main.repositoryPath + "/" + removeIllegal(ea.album_name) + "_" + ea.ex_id + "_" + ea.ex_hash;
            File npFile = new File(newPath);
            if(npFile.exists()) npFile.delete();
            boolean success = dir.renameTo(new File(newPath));
            if(!success) Printer.printError("Failed to rename \n" + dir.getAbsolutePath() + "\n to \n" + newPath);
        }
    }

    private ArrayList<ExhentaiImage> extractImages(String page, String url, ExhentaiAlbum group) throws Exception {
        ArrayList<ExhentaiImage> result = new ArrayList<>();

        String no = findNoOfPages(page);

        Printer.printToLog("Found " + no + " pages for this album", Printer.LOGTYPE.INFO);
        AtomicInteger c = new AtomicInteger(0);
        for(int i = 0; i < Integer.parseInt(no); i++){
            result.addAll(parseImages(Main.ec.getPage(url + "/?p=" + i), c, group));
        }

        return result;
    }

    private ArrayList<ExhentaiImage> parseImages(String page, AtomicInteger count, ExhentaiAlbum group) throws Exception {
        ArrayList<ExhentaiImage> result = new ArrayList<>();

        String tmp = getPassage(page, "<div id=\"gdt\">", "<div class=\"c\"></div>");

        String[] tmp1 = tmp.split("<div class=\"gdtl\" style=\"height:\\d+px\"><a href=\"");
        for(int i = 1; i < tmp1.length; i++){
            count.getAndIncrement();
            Printer.printToLog("Processing image " + count + "/" + group.length + "...", Printer.LOGTYPE.INFO);
            String url = tmp1[i].split("\">")[0];
            result.add(parseImagePage(url, count, group));
        }

        return result;
    }

    private ExhentaiImage parseImagePage(String url, AtomicInteger count, ExhentaiAlbum group) throws Exception {
        ExhentaiImage result = new ExhentaiImage();

        result.ex_id = url.split("exhentai\\.org/s/")[1].split("/")[0];
        result.order_pos = String.format("%4s", count.toString()).replace(' ','0');
        result.img_page = url;

        String page = Main.ec.getPage(url);
        String tmp = getPassage(page, "<div id=\"i3\">", "</a>");
        String imgUrl = getPassage(tmp, "<img id=\"img\" src=\"", "\" style=\"");

        String tmp2 = getPassage(page, "<div id=\"i4\"><div>", "</div>");
        String widthHeight = tmp2.split(" :: ")[1];
        result.img_width = widthHeight.split(" x ")[0];
        result.img_height = widthHeight.split(" x ")[1];
        result.filesize = tmp2.split(" :: ")[2];
        String[] tmp3 = tmp2.split(" :: ")[0].split("\\.");
        result.file_type = tmp3[tmp3.length - 1];


        String filePath = Main.repositoryPath + "/" + removeIllegal(group.album_name) + "_" + group.ex_id + "_" + group.ex_hash + "/" + result.order_pos + "_" + result.ex_id + "." + result.file_type;
        File file = new File(filePath);
        if(!file.exists()){
            Printer.printToLog("Downloading image...", Printer.LOGTYPE.DEBUG);
            try{
                ReadableByteChannel rbc = Channels.newChannel(Main.ec.establishConnection(imgUrl).getInputStream());
                FileOutputStream fos = new FileOutputStream(filePath);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                if(Utility.isQuotaExceeded(file)){
                    Printer.printToLog("\033[1;32m Last downloaded image was  quota_exceeded img, please try downloading more at a later point in time. Use \u001B[0m \u001B[31m -startFrom \u001B[0m" + group.ex_id, Printer.LOGTYPE.INFO);
                    file.delete();
                    System.exit(-1);
                }
            } catch (Exception e){
                Printer.printException(e);
            }
        } else {
            Printer.printToLog("Image already exists, skipping...", Printer.LOGTYPE.DEBUG);
        }

        return result;
    }

    private HashMap<String, String> extractTags(String taglist){
        HashMap<String, String> result = new HashMap<>();
        String[] tmp = taglist.split("<tr><td class=\"tc\">");
        for(int i = 1; i < tmp.length; i++){
            String val = tmp[i].split(":</td>")[0];

            String[] tmp2 = tmp[i].split("</a>");
            for(int j = 0; j < tmp2.length - 1; j++){
                String[] tmp3 = tmp2[j].split(">");
                result.put(tmp3[tmp3.length-1].replace(" ", "_"), val);
            }
        }
        return result;
    }

    //patternStart has to be unique in the string
    private String getPassage(String strToFilter, String patternStart, String patternEnd){
        String[] tmp = strToFilter.split(patternStart);
        if(tmp.length != 2) {
            Printer.printError("Failed to parse correctly:\nStrToFilter: " + strToFilter + "\nPatternStart: " +
                    patternStart + "\nPatternEnd: " + patternEnd);
            System.exit(3);
        }
        String[] tmp1 = tmp[1].split(patternEnd);
        String result = "";
        if(tmp1.length > 0) result = tmp1[0];
        return result;
    }
}
