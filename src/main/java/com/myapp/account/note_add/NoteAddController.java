package com.myapp.account.note_add;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.account.ai.AiService;
import com.myapp.account.ai.FlaskRequestMapper;
import com.myapp.account.ai.RequestSendToFlaskDto;
import com.myapp.account.user.User;
import com.myapp.account.user.UserRepository;

@RestController
@RequestMapping("/note")
public class NoteAddController {
	private final NoteAddRepository noteAddRepository;
	private final UserRepository userRepository;
	private final AiService aiService;
	
	public NoteAddController(NoteAddRepository noteAddRepository, UserRepository userRepository,AiService aiService) {
		this.noteAddRepository = noteAddRepository;
		this.userRepository = userRepository;
		this.aiService = aiService;
	}
	
	@PostMapping("/add")
	public ResponseEntity<?> save(@RequestBody Map<String, Object> consume){
		try {
			if(consume.get("userID")==null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("userID가 요청에 포함되지 않았습니다.");
			}
			Long UserID = Long.valueOf(consume.get("userID").toString());
			
			User user = userRepository.findById(UserID)
					.orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
			
			NoteAdd noteAdd = new NoteAdd();
			noteAdd.setAmount((Integer) consume.get("amount"));
			noteAdd.setContent((String) consume.get("content"));
			noteAdd.setCategory((String) consume.get("category"));
			noteAdd.setMemo((String) consume.get("memo"));
			DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
			noteAdd.setCreatedAt(LocalDateTime.parse((String) consume.get("createdAt"),formatter));
			noteAdd.setIsRegularExpense((Boolean) consume.get("isRegularExpense"));
			noteAdd.setNotifyOverspend((Boolean) consume.get("notifyOverspend"));
			noteAdd.setIsIncome((Boolean) consume.get("isIncome"));
			
			noteAdd.setUser(user);
			NoteAdd save = noteAddRepository.save(noteAdd);
			
			//Flask 분석 요청 20250513
			RequestSendToFlaskDto dto = FlaskRequestMapper.from(save, user);
			//시간 정보 파싱
			LocalDateTime createdAt = save.getCreatedAt();
			dto.setHour(createdAt.getHour());
			dto.setDay(createdAt.getDayOfWeek().getValue());
			
			Map<String, Object> aiResponse = aiService.sendToFlask(dto);
			
			Map<String,Object> result = new HashMap<>();
			result.put("save", save);
			if(aiResponse.containsKey("recommendation")) {
				result.put("recommendation", aiResponse.get("recommendation"));
			}
			
			result.put("overspending", aiResponse.get("overspending"));
			return ResponseEntity.ok(result);
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("저장 실패 : "+e.getMessage());
		}
	}
	
	@GetMapping("/list")
	public ResponseEntity<List<NoteAdd>> getNotesByEntity(@RequestParam("userID") Long userID){
		try {
			User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
			List<NoteAdd> notes = noteAddRepository.findByUser(user);
			return ResponseEntity.ok(notes);
		}catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
}