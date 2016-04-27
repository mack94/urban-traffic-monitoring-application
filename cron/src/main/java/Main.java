import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;

public class Main {

    public static void main(String args[]){
        GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyCzSkC5CdUMz62J0Cg1n6r6BgXJpbwpQi0");
        try {
            String[] destinations = new String[1];
            String[] origins = new String[1];
            destinations[0] = "Kremerowska 11, Kraków";
            origins[0] = "Kawiory 21, Kraków";

//            DistanceMatrixApiRequest distanceMatrixApiRequest = new DistanceMatrixApiRequest(context);
            DistanceMatrix result = DistanceMatrixApi.getDistanceMatrix(context, destinations, origins ).await();

            for (DistanceMatrixRow row: result.rows) {
                for (DistanceMatrixElement element: row.elements) {
                    String elementResult = String.format("{Distance = %s, Duration = %s, Fare = %s, Status = %s",
                            element.distance, element.duration, element.fare, element.status);
                    System.out.println(elementResult);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}