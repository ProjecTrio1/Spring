package com.myapp.account.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.account.note_add.NoteAdd;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {
    private final RestTemplate restTemplate = new RestTemplate();
    
    public Map<String, Object> sendToFlask(RequestSendToFlaskDto dto) throws JsonProcessingException{
    	if(Boolean.TRUE.equals(dto.getIsIncome())) {
    		return Map.of("recommendation","","overspending",false);
    	}
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
    //사용자 맞춤 모델 학습 요청
    public boolean requestTrainToFlask(RequestSendToFlaskDto dto, List<NoteAdd> records) throws JsonProcessingException{
    	String flaskURL = "http://localhost:5000/train_user_model";
    	
    	Map<String, Object> requestBody = new HashMap<>();
    	requestBody.put("userId", dto.getUserId());
    	
    	List<Map<String, Object>> recordList = new ArrayList<>();
    	for(NoteAdd note : records) {
    		if(!note.getIsIncome()) {
    			Map<String, Object> record = new HashMap<>();
    			record.put("hour", note.getCreatedAt().getHour());
    			record.put("day", note.getCreatedAt().getDayOfWeek().getValue());
    			record.put("amt", note.getAmount());
    			record.put("category_group", note.getCategory());
    			record.put("isIncome", false);
    			recordList.add(record);
    		}
    	}
    	requestBody.put("records", recordList);
    	
    	return sendPostToFlask(flaskURL, requestBody);
    }
    //지도학습 모델 학습 요청
    public boolean requestTrainFeedback(RequestSendToFlaskDto dto, List<NoteAdd> records) throws JsonProcessingException{
    	String flaskURL = "http://localhost:5000/train_feedback_model";
    	
    	Map<String, Object> requestBody = new HashMap<>();
    	requestBody.put("userId", dto.getUserId());
    	
    	List<Map<String, Object>> recordList = new ArrayList<>();
    	for(NoteAdd note : records) {
    		if(!note.getIsIncome()) {
    			Map<String, Object> record = new HashMap<>();
    			record.put("hour", note.getCreatedAt().getHour());
    			record.put("day", note.getCreatedAt().getDayOfWeek().getValue());
    			record.put("amt", note.getAmount());
    			record.put("category_group", note.getCategory());
    			record.put("isIncome", false);
    			record.put("user_feedback", note.getUserFeedback() == null ? null : note.getUserFeedback());
    			record.put("overspending", note.getIsOverspending());
    			record.put("anomaly", note.getIsAnomaly());
    			recordList.add(record);
    		}
    	}
    	requestBody.put("records", recordList);
    	
    	return sendPostToFlask(flaskURL, requestBody);
    }
    
    private boolean sendPostToFlask(String url, Map<String, Object> body) {
    	try {
    		HttpHeaders headers = new HttpHeaders();
        	headers.setContentType(MediaType.APPLICATION_JSON);
        	HttpEntity<Map<String, Object>> request = new HttpEntity<>(body,headers);
        	RestTemplate restTemplate = new RestTemplate();
        	ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    		return response.getStatusCode() == HttpStatus.OK;
    	}catch(Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    }
}
