package ru.practicum;

import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate restTemplate;

    public BaseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected <T> ResponseEntity<Object> get(String url, @Nullable Map<String, Object> uriVariables) {
        return makeAndSendRequest(HttpMethod.GET, url, uriVariables, null);
    }

    protected <T> ResponseEntity<Object> post(T body) {
        return makeAndSendRequest(HttpMethod.POST, "/hit", null, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(HttpMethod method, String url, @Nullable Map<String, Object> uriVariables, @Nullable T body) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        try {
            assert uriVariables != null;
            return restTemplate.exchange(url, method, requestEntity, Object.class, uriVariables);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
