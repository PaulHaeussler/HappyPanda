package exh;

import java.util.ArrayList;
import java.util.HashMap;

public class ExhentaiAlbum {

    public String album_id;
    public String ex_id;
    public String album_name;
    public String album_name_jp;
    public String posted;
    public String parent;
    public String language;
    public String file_size;
    public String length;
    public String favourited;
    public String rating_total;
    public String rating_avg;
    public String ration_local;
    public String category;
    public String uploader;
    public String fav_id;
    public String added;

    public HashMap<String, String> tags = new HashMap<>(); //tag_name - category
    public ArrayList<ExhentaiImage> images = new ArrayList<>();
}
