package com.myapp.account.ai;

import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {
    private final RestTemplate restTemplate = new RestTemplate();
    
    public Map<String, Object> sendToFlask(RequestSendToFlaskDto dto) throws JsonProcessingException{
    	String flaskURL = "http://localhost:5000/overspending";
    	
    	ObjectMapper objectMapper = new ObjectMapper();
//    	String json = objectMapper.writeValueAsString(Collections.singleton(dto));
    	String json = objectMapper.writeValueAsString(dto);
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	
    	HttpEntity<String> request = new HttpEntity<>(json,headers);
    	ResponseEntity<String> response = restTemplate.postForEntity(flaskURL,request,String.class);
    	
    	return objectMapper.readValue(response.getBody(), new TypeReference<Map<String,Object>>() {});
    }
}
