package pl.edu.agh.pp.charts.data.server;

import java.util.Map;

/**
 * Created by Dawid on 2016-11-12.
 */
public class ServerDatesInfo {
    private static Map<String, Integer> map = null;
    public static Map<String, Integer> getDates(){
        return map;
    }
    public static void setMap( Map<String, Integer> arg){
        map = arg;
    }
}
