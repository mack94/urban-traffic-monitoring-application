package pl.edu.agh.pp.cron;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Instant;
import org.json.JSONObject;
import pl.edu.agh.pp.cron.utils.ContextLoader;
import pl.edu.agh.pp.cron.utils.Route;
import pl.edu.agh.pp.cron.utils.WaypointsExtractor;
import pl.edu.agh.pp.detector.loaders.InputParser;
import pl.edu.agh.pp.detector.records.Record;

/**
 * Created by Jakub Janusz on 14.11.2016.
 * 21:23
 * server
 */
public class HalfRouteManager
{
    private final Pattern DISTANCE_PATTERN = Pattern.compile("(\\d+,\\d+) km");

    private final Record record;
    private final String waypoints;
    private final GeoApiContext context;

    public HalfRouteManager(Record record, String waypoints) throws IOException
    {
        this.record = record;
        this.waypoints = waypoints;
        this.context = new ContextLoader().geoApiContextLoader();
    }

    public String splitRoute() throws Exception
    {
        String[] extractedWaypoints = WaypointsExtractor.extractWaypoints(waypoints);
        String first = executeRequest(extractedWaypoints[0], extractedWaypoints[1], extractedWaypoints[3]);
        String second = executeRequest(extractedWaypoints[1], extractedWaypoints[2], extractedWaypoints[4]);
        return concatResults(first, second);
    }

    // TODO: I really don't like this solution, but can't see any better.
    private String executeRequest(String origin, String destination, String waypoints) throws Exception
    {
        DistanceMatrix distanceMatrix = DistanceMatrixApi
                .getDistanceMatrix(context, new String[] { origin }, new String[] { destination })
                .mode(TravelMode.DRIVING)
                .language("pl")
                .departureTime(Instant.now())
                .await();

        DirectionsResult directionsApi = DirectionsApi
                .getDirections(context, origin, destination)
                .alternatives(false)
                .language("pl")
                .mode(TravelMode.DRIVING)
                .departureTime(Instant.now())
                .await();

        Route route = new Route(String.valueOf(record.getRouteID()), distanceMatrix, directionsApi, waypoints);
        return route.toString();
    }

    private String concatResults(String a, String b)
    {
        InputParser inputParser = new InputParser();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS").format(new Date());
        Record first = inputParser.parse(a);
        Record second = inputParser.parse(b);
        JSONObject result = new JSONObject()
                .put("timeStamp", timeStamp)
                .put("id", String.valueOf(record.getRouteID()))
                .put("distance", countDistance(first.getDistance(), second.getDistance()))
                .put("duration", String.valueOf(first.getDuration() + second.getDuration()))
                .put("durationInTraffic", String.valueOf(first.getDurationInTraffic() + second.getDurationInTraffic()))
                .put("waypoints", "default")
                .put("anomalyId", "");
        return result.toString();
    }

    private String countDistance(String first, String second)
    {
        double a = getDistance(first);
        double b = getDistance(second);

        return String.valueOf(a + b).concat(" km");
    }

    private double getDistance(String distance)
    {
        Matcher matcher = DISTANCE_PATTERN.matcher(distance);
        if (matcher.find())
        {
            return Double.valueOf(matcher.group(1).replaceAll(",", "."));
        }
        throw new IllegalArgumentException("Improper distance format.");
    }

    private String getWaypoints(String first, String second)
    {
        String[] a = first.split(";");
        String[] b = second.split(";");
        String[] result = new String[a.length + b.length - 1];
        for (int i = 0; i < a.length; i++)
        {
            result[i] = StringUtils.trim(a[i]);
        }
        for (int i = 1; i < b.length; i++)
        {
            result[i + a.length] = StringUtils.trim(b[i]);
        }
        return Arrays.stream(result).collect(Collectors.joining("; "));
    }
}
