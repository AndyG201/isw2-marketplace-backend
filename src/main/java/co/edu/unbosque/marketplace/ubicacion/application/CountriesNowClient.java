package co.edu.unbosque.marketplace.ubicacion.application;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

@Component
class CountriesNowClient {
    private final RestClient client;

    CountriesNowClient(@Value("${app.ubicacion.api-base-url:https://countriesnow.space/api/v0.1}") String baseUrl) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient))
                .build();
    }

    List<CountryData> countries() {
        JsonNode response = client.get().uri("/countries/iso").retrieve().body(JsonNode.class);
        JsonNode data = validarRespuesta(response);
        List<CountryData> result = new ArrayList<>();
        for (JsonNode item : data) {
            String name = item.path("name").asText("").trim();
            String iso2 = item.path("Iso2").asText("").trim().toUpperCase();
            if (!name.isBlank() && iso2.matches("[A-Z]{2}")) result.add(new CountryData(name, iso2));
        }
        return result;
    }

    List<String> cities(String countryName) {
        JsonNode response = client.post().uri("/countries/cities")
                .body(Map.of("country", countryName)).retrieve().body(JsonNode.class);
        JsonNode data = validarRespuesta(response);
        List<String> result = new ArrayList<>();
        for (JsonNode item : data) {
            String name = item.asText("").trim();
            if (!name.isBlank()) result.add(name);
        }
        return result;
    }

    private JsonNode validarRespuesta(JsonNode response) {
        if (response == null || response.path("error").asBoolean(true) || !response.path("data").isArray()) {
            throw new IllegalStateException("La API externa de países y ciudades devolvió una respuesta inválida.");
        }
        return response.path("data");
    }

    record CountryData(String name, String iso2) { }
}
