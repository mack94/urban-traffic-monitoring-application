package pl.edu.agh.pp.charts.data.server;

import java.util.List;
import java.util.Map;

/**
 * Created by Dawid on 2016-11-12.
 */
public class ServerDatesInfo {
    private static Map<String, List<Integer>> map = null;
    public static Map<String, List<Integer>> getDates(){
        return map;
    }
    public static void setMap( Map<String, List<Integer>> arg){
        map = arg;
    }
}
