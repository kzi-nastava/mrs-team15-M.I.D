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

    public String getReverseGeocode(double lat, double lon) throws Exception {
        String url = "https://nominatim.openstreetmap.org/reverse" +
                "?format=json" + "&lat=" + lat +
                "&lon=" + lon + "&accept-language=sr";

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "RideNow-App/1.0 (ridenow.application@gmail.com)");
        headers.set("Accept-Language", "sr,en");
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);

        JsonNode body = response.getBody();
        if (body == null || body.get("address") == null) {
            throw new Exception("Address not found for coordinates");
        }

        JsonNode address = body.get("address");

        String road = address.has("road") ? address.get("road").asText() : "";
        String houseNumber = address.has("house_number") ? address.get("house_number").asText() : "";

        String city = address.has("city") ? address.get("city").asText() :
                (address.has("town") ? address.get("town").asText() :
                        (address.has("village") ? address.get("village").asText() : ""));

        StringBuilder formattedAddress = new StringBuilder();

        if (!road.isEmpty()) {
            formattedAddress.append(road);
            if (!houseNumber.isEmpty()) {
                formattedAddress.append(" ").append(houseNumber);
            }
        }

        if (!city.isEmpty()) {
            if (formattedAddress.length() > 0) {
                formattedAddress.append(", ");
            }
            formattedAddress.append(city);
        }

        if (formattedAddress.length() == 0) {
            throw new Exception("Could not format address");
        }

        return transliterate(formattedAddress.toString());
    }

    private String transliterate(String message) {
        char[] abcCyr = {
                ' ', 'а', 'б', 'в', 'г', 'д', 'ђ', 'е', 'ж', 'з', 'и', 'ј', 'к', 'л', 'љ', 'м', 'н', 'њ', 'о', 'п', 'р', 'с', 'т', 'ћ', 'у', 'ф', 'х', 'ц', 'ч', 'џ', 'ш',
                'А', 'Б', 'В', 'Г', 'Д', 'Ђ', 'Е', 'Ж', 'З', 'И', 'Ј', 'К', 'Л', 'Љ', 'М', 'Н', 'Њ', 'О', 'П', 'Р', 'С', 'Т', 'Ћ', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Џ', 'Ш',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '.'
        };

        String[] abcLat = {
                " ", "a", "b", "v", "g", "d", "đ", "e", "ž", "z", "i", "j", "k", "l", "lj", "m", "n", "nj", "o", "p", "r", "s", "t", "ć", "u", "f", "h", "c", "č", "dž", "š",
                "A", "B", "V", "G", "D", "Đ", "E", "Ž", "Z", "I", "J", "K", "L", "Lj", "M", "N", "Nj", "O", "P", "R", "S", "T", "Ć", "U", "F", "H", "C", "Č", "Dž", "Š",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ",", "."
        };

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            boolean found = false;
            for (int x = 0; x < abcCyr.length; x++) {
                if (message.charAt(i) == abcCyr[x]) {
                    builder.append(abcLat[x]);
                    found = true;
                    break;
                }
            }
            if (!found) {
                builder.append(message.charAt(i));
            }
        }
        return builder.toString();
    }
}