package com.myapp.account.ai;

import java.util.Collections;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    public String sendToFlask(RequestSendToFlaskDto dto) throws JsonProcessingException{
    	String flaskURL = "http://localhost:5000/predict";
    	
    	ObjectMapper objectMapper = new ObjectMapper();
    	String json = objectMapper.writeValueAsString(Collections.singleton(dto));
    	HttpHeaders headers = new HttpHeaders();
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	
    	HttpEntity<String> request = new HttpEntity<>(json,headers);
    	ResponseEntity<String> response = restTemplate.postForEntity(flaskURL,request,String.class);
    	
    	return response.getBody();
    }
}
