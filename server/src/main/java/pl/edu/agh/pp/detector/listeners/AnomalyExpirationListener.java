package pl.edu.agh.pp.detector.listeners;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import pl.edu.agh.pp.detector.adapters.Server;
import pl.edu.agh.pp.detector.operations.AnomalyOperationProtos;
import pl.edu.agh.pp.settings.Options;
import pl.edu.agh.pp.settings.exceptions.IllegalPreferenceObjectExpected;

public class AnomalyExpirationListener extends Thread
{
    private ConcurrentHashMap<Integer, Long> anomalyID;
    private ConcurrentHashMap<Integer, DateTime> anomalyTime;
    private Set<Long> expiredAnomalies;
    private Server server;

    public AnomalyExpirationListener(ConcurrentHashMap<Integer, Long> anomalyID, ConcurrentHashMap<Integer, DateTime> anomalyTime)
    {
        this.anomalyID = anomalyID;
        this.anomalyTime = anomalyTime;
        this.expiredAnomalies = new HashSet<>();
    }

    @Override
    public void run()
    {
        int anomalyLiveTime;
        int expirationBroadcastTime;
        try
        {
            anomalyLiveTime = (int) Options.getInstance().getPreference("AnomalyLiveTime", Integer.class);
            expirationBroadcastTime = (int) Options.getInstance().getPreference("AnomalyExpirationBroadcastTime", Integer.class);
        }
        catch (IllegalPreferenceObjectExpected e)
        {
            e.printStackTrace();
            return;
        }
        while (true)
        {
            anomalyID.entrySet()
                    .stream()
                    .filter(entry -> !expiredAnomalies.contains(entry.getValue()))
                    .forEach(entry -> {
                        DateTime anomaly = anomalyTime.get(entry.getKey());
                        DateTime now = DateTime.now();
                        int lastUpdateInSeconds = Seconds.secondsBetween(anomaly, now).getSeconds();
                        if (lastUpdateInSeconds > anomalyLiveTime)
                        {
                            sendMessage(entry.getKey(), entry.getValue());
                            if (lastUpdateInSeconds > expirationBroadcastTime)
                            {
                                expiredAnomalies.add(entry.getValue());
                            }
                        }
                    });
            try
            {
                sleep(1000 * 90);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(int routeId, long anomalyId)
    {
        AnomalyOperationProtos.AnomalyMessage message = AnomalyOperationProtos.AnomalyMessage.newBuilder()
                .setAnomalyID(anomalyId)
                .setRouteIdx(routeId)
                .setDate(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"))
                .setIsActive(false)
                .build();
        server.send(message.toByteArray());
    }

    public void setServer(Server server)
    {
        this.server = server;
    }
}
