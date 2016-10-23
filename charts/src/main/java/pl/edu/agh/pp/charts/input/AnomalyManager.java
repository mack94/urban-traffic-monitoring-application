package pl.edu.agh.pp.charts.input;

import pl.edu.agh.pp.charts.controller.MainWindowController;
import pl.edu.agh.pp.charts.operations.AnomalyOperationProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dawid on 2016-10-23.
 */
public class AnomalyManager {
    private List<Anomaly> anomalyList;
    private static AnomalyManager instance;
    private MainWindowController mainWindowController;

    private AnomalyManager(){
        anomalyList = new ArrayList<>();
    }

    public static AnomalyManager getInstance(){
        if(instance == null){
            instance = new AnomalyManager();
        }
        return instance;

    }

    public void setController(MainWindowController mainWindowController){
        this.mainWindowController = mainWindowController;
    }

    public void addAnomaly(AnomalyOperationProtos.AnomalyMessage anomalyMessage){
        String id = String.valueOf(anomalyMessage.getAnomalyID());
        Anomaly anomaly;
        if(anomalyExists(id)){
            anomaly = getAnomalyById(id);
            anomaly.addMessage(anomalyMessage);
            if(mainWindowController != null) mainWindowController.updateAnomalyInfo(anomaly.getScreenId());
        }
        else{
            anomaly = new Anomaly(anomalyMessage);
            anomalyList.add(anomaly);
            if(mainWindowController != null) mainWindowController.addAnomalyToList(anomaly.getScreenId());
        }
    }

    private boolean anomalyExists(String anomalyId){
        for(Anomaly a: anomalyList){
            if(a.getAnomalyId().equals(anomalyId)) return true;
        }
        return false;
    }

    public Anomaly getAnomalyById(String id){
        for(Anomaly a: anomalyList){
            if(a.getAnomalyId().equals(id)) return a;
        }
        return null;
    }

    public Anomaly getAnomalyByScreenId(String screenId){
        for(Anomaly a: anomalyList){
            if(a.getScreenId().equals(screenId)) return a;
        }
        return null;
    }
}
