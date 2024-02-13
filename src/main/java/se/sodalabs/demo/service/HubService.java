package se.sodalabs.demo.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import se.sodalabs.demo.domain.FeedbackDTO;
import se.sodalabs.demo.domain.ParticipantDTO;

@Service
public class HubService {

  @Value("${hub.address}")
  String hubAddress;

  @Value("${service.tag}")
  String id;

  @Value("${service.name}")
  String name;

  RestTemplate restTemplate = new RestTemplate();
  HttpHeaders headers = new HttpHeaders();
  ObjectMapper objectMapper = new ObjectMapper();

  Logger logger = LoggerFactory.getLogger(HubService.class);
  private String lastRegisteredAt = "n/a";

  public HubService() {
    headers.setContentType(MediaType.APPLICATION_JSON);

    restTemplate.setErrorHandler(new ErrorHandler());

    objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    // The standard JDK HTTP library does not support HTTP PATCH
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory();
    restTemplate.setRequestFactory(requestFactory);
  }

  public String getHubAddress() {
    return hubAddress;
  }

  public ResponseEntity<String> registerNewParticipant() {
    ParticipantDTO participantDTO = new ParticipantDTO(id, name);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    HttpEntity<ParticipantDTO> request = new HttpEntity<>(participantDTO, headers);

    this.lastRegisteredAt =
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("y-MM-dd HH:mm:ss"));
    return restTemplate.exchange(
        hubAddress + "/api/v1/participant/", HttpMethod.POST, request, String.class);
  }

  public ResponseEntity<String> unregisterParticipant() {
    HttpHeaders headers = new HttpHeaders();
    HttpEntity<ParticipantDTO> request = new HttpEntity<>(headers);

    return restTemplate.exchange(
        hubAddress + "/api/v1/participant/" + id, HttpMethod.DELETE, request, String.class);
  }

  public ResponseEntity<String> sendFeedback(int happinessScore) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    HttpEntity<Integer> request = new HttpEntity<>(happinessScore, headers);

    return restTemplate.exchange(
        hubAddress + "/api/v1/participant/" + id + "/feedback",
        HttpMethod.POST,
        request,
        String.class);
  }

  public ResponseEntity<String> sendFeedback(FeedbackDTO feedback) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
    HttpEntity<FeedbackDTO> request = new HttpEntity<>(feedback, headers);

    return restTemplate.exchange(
        hubAddress + "/api/v2/participant/" + id + "/feedback",
        HttpMethod.POST,
        request,
        String.class);
  }

  private static ResponseEntity<String> returnParsedHubResponse(
      ResponseEntity<String> responseFromHub) {
    if (responseFromHub.getStatusCode().is2xxSuccessful()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else if (responseFromHub.getStatusCode().is4xxClientError()) {
      return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    } else {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public String getLastRegisteredAt() {
    return lastRegisteredAt;
  }
}
