package pl.edu.agh.pp.detector.listeners;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
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
        try
        {
            anomalyLiveTime = (int) Options.getInstance().getPreference("AnomalyLiveTime", Integer.class);
        }
        catch (IllegalPreferenceObjectExpected e)
        {
            e.printStackTrace();
            return;
        }
        while (true)
        {
            for (Map.Entry<Integer, Long> entry : anomalyID.entrySet())
            {
                if (!expiredAnomalies.contains(entry.getValue()))
                {
                    DateTime anomaly = anomalyTime.get(entry.getKey());
                    DateTime now = DateTime.now();
                    int lastUpdateInSeconds = Seconds.secondsBetween(anomaly, now).getSeconds();
                    if (lastUpdateInSeconds > anomalyLiveTime)
                    {
                        sendMessage(lastUpdateInSeconds, entry);
                        expiredAnomalies.add(entry.getValue());
                    }
                }
            }
            try
            {
                // TODO: for demo it's 15 seconds, on production should be at least 1 minute. ALSO, REMEMBER TO CHANGE THE VALUE OF ANOMALYLIVETIME IN DEFAULT PREFERENCES - FOR DEMO IT'S 10 SECONDS.
                sleep(1000 * 15);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(int lastUpdateInSeconds, Map.Entry<Integer, Long> anomalyIDEntry)
    {
        AnomalyOperationProtos.ExpirationMessage expirationMessage = AnomalyOperationProtos.ExpirationMessage.newBuilder()
                .setAnomalyID(anomalyIDEntry.getValue())
                .setRouteIdx(anomalyIDEntry.getKey())
                .setDate(DateTime.now().toString("yyyy-MM-dd HH:mm:ss"))
                .build();
        server.sendExpirationMessage(ByteBuffer.wrap(expirationMessage.toByteArray()));
    }

    public void setServer(Server server)
    {
        this.server = server;
    }
}
