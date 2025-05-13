package com.myapp.account.ai;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ai")
public class AiController {
	@Autowired
	private AiService aiService;
	
    @PostMapping("/flask")
    public Map<String, Object> sendToFlask(@RequestBody RequestSendToFlaskDto dto) throws JsonProcessingException{
    	return aiService.sendToFlask(dto);
    }
}
