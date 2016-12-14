package pl.edu.agh.pp.utils;

import com.google.maps.model.TravelMode;
import org.joda.time.Instant;

/**
 * Created by Jakub Janusz on 14.11.2016.
 * 21:04
 * server
 */
public class RequestParams {
    private String id;
    private String[] origins;
    private String[] destinations;
    private TravelMode travelMode;
    private Instant departure;
    private String defaultWaypoints;
    private boolean missingHistoricalData;

    public RequestParams withId(String id) {
        this.id = id;
        return this;
    }

    public RequestParams withOrigins(String[] origins) {
        this.origins = origins;
        return this;
    }

    public RequestParams withDestinations(String[] destinations) {
        this.destinations = destinations;
        return this;
    }

    public RequestParams withTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
        return this;
    }

    public RequestParams withDeparture(Instant departure) {
        this.departure = departure;
        return this;
    }

    public RequestParams withDefaultWaypoints(String defaultWaypoints) {
        this.defaultWaypoints = defaultWaypoints;
        return this;
    }

    public RequestParams withMissingHistoricalData(boolean isHistoricalDataMissing) {
        this.missingHistoricalData = isHistoricalDataMissing;
        return this;
    }

    public String getId() {
        return id;
    }

    public String[] getOrigins() {
        return origins;
    }

    public String[] getDestinations() {
        return destinations;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public Instant getDeparture() {
        return departure;
    }

    public String getDefaultWaypoints() {
        return defaultWaypoints;
    }

    public boolean isMissingHistoricalData() {
        return missingHistoricalData;
    }
}
