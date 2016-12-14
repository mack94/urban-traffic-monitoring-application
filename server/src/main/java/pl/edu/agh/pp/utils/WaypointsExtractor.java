package pl.edu.agh.pp.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Created by Jakub Janusz on 14.11.2016.
 * 21:27
 * server
 */
public class WaypointsExtractor {

    public static String[] extractWaypoints(String waypoints) {
        String[] splitted = waypoints.split(";");
        for (int i = 0; i < splitted.length; i++) {
            splitted[i] = StringUtils.trim(splitted[i]);
        }
        int ctrIdx = splitted.length / 2;
        String[] head = new String[ctrIdx];
        String[] tail = new String[splitted.length - ctrIdx + 1];
        System.arraycopy(splitted, 0, head, 0, ctrIdx);
        System.arraycopy(splitted, ctrIdx - 1, tail, 0, splitted.length - ctrIdx + 1);
        String first = Arrays.stream(head).collect(Collectors.joining("; "));
        String second = Arrays.stream(tail).collect(Collectors.joining("; "));
        String begin = head[0];
        String central = head[head.length - 1];
        String end = tail[tail.length - 1];
        return new String[]{begin, central, end, first, second};
    }

}
