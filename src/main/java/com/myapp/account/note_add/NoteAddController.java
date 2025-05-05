package com.myapp.account.note_add;

import java.time.LocalDateTime;
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

import com.myapp.account.user.User;
import com.myapp.account.user.UserRepository;

@RestController
@RequestMapping("/note")
public class NoteAddController {
	private final NoteAddRepository noteAddRepository;
	private final UserRepository userRepository;
	
	public NoteAddController(NoteAddRepository noteAddRepository, UserRepository userRepository) {
		this.noteAddRepository = noteAddRepository;
		this.userRepository = userRepository;
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
			noteAdd.setCreatedAt(LocalDateTime.parse((String) consume.get("createdAt")));
			noteAdd.setIsRegularExpense((Boolean) consume.get("isRegularExpense"));
			noteAdd.setNotifyOverspend((Boolean) consume.get("notifyOverspend"));
			noteAdd.setIsIncome((Boolean) consume.get("isIncome"));
			
			noteAdd.setUser(user);
			
			NoteAdd save = noteAddRepository.save(noteAdd);
			return ResponseEntity.ok(save);
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