package com.nitu.utils;
import java.util.ArrayList;
import com.nitu.reflect.list.ListItem;

/**
 * @author NITU
 */
public class CodeUtils {

    //java转义
    public static String EscapeJava(String input) {
        
        return input
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\"", "\\\"");
    }

    //itemlist2xml
    public static String Items2Xml(ArrayList<ListItem> itemlist) {
        String outXml="";
        return outXml;
    }
    //xml2itemlist
    public static ArrayList<ListItem> Xml2Items(String inputXml) {
        ArrayList<ListItem> itemlist=new ArrayList<>();
        return itemlist;
    }
}
