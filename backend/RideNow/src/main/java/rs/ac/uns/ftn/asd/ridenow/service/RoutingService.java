package rs.ac.uns.ftn.asd.ridenow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RideEstimateResponseDTO;
import rs.ac.uns.ftn.asd.ridenow.dto.ride.RoutePointDTO;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@Service
public class RoutingService {
    private final RestTemplate restTemplate = new RestTemplate();

    public double[] getGeocode(String address) throws Exception {
        String url = "https://nominatim.openstreetmap.org/search" +
                        "?format=json" + "&limit=1" +
                        "&street=" + address + "&county=South Bačka District" +
                        "&state=Vojvodina" + "&country=Serbia" +
                        "&countrycodes=rs" + "&accept-language=sr";

        HttpHeaders headers = new HttpHeaders();
        headers.set(
                "User-Agent",
                "RideNow-App/1.0 (ridenow.application@gmail.com)"
        );
        headers.set("Accept-Language", "sr,en");
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode[]> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode[].class);

        System.out.println(response.getStatusCode());
        System.out.println(Arrays.toString(response.getBody()));

        JsonNode[] body = response.getBody();

        if (body == null || body.length == 0) {
            throw new Exception("Address not found");
        }

        JsonNode node = body[0];
        double lat = node.get("lat").asDouble();
        double lon = node.get("lon").asDouble();

        return new double[]{lat, lon};
    }

    public RideEstimateResponseDTO getRoute(double latStart, double lonStart, double latEnd, double lonEnd) throws Exception {
        String url = "http://router.project-osrm.org/route/v1/driving/"
                + lonStart + "," + latStart + ";" + lonEnd + "," + latEnd + "?overview=full&geometries=geojson";

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);

        if (response == null || response.get("routes") == null) {
            throw new Exception("Route not found");
        }

        JsonNode route = response.get("routes").get(0);

        double distanceMeters = route.get("distance").asDouble();
        double durationSeconds = route.get("duration").asDouble();

        List<RoutePointDTO> points = new ArrayList<>();
        for (JsonNode coordinate : route.get("geometry").get("coordinates")) {
            RoutePointDTO point = new RoutePointDTO();
            point.setLng(coordinate.get(0).asDouble());
            point.setLat(coordinate.get(1).asDouble());
            points.add(point);
        }

        RideEstimateResponseDTO responseDTO = new RideEstimateResponseDTO();
        responseDTO.setDistanceKm(distanceMeters / 1000);
        responseDTO.setEstimatedDurationMin((int) (durationSeconds / 60));
        responseDTO.setRoute(points);
        return responseDTO;
    }
}