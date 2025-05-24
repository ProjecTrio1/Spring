package com.myapp.account.note_add;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

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
	private final NoteAddService noteAddService;
	
	public NoteAddController(NoteAddRepository noteAddRepository, UserRepository userRepository,AiService aiService,NoteAddService noteAddService) {
		this.noteAddRepository = noteAddRepository;
		this.userRepository = userRepository;
		this.aiService = aiService;
		this.noteAddService = noteAddService;
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
			int hourGroup = convertHourToGroup(save.getCreatedAt().getHour());
			dto.setHour(hourGroup);
			dto.setDay(save.getCreatedAt().getDayOfWeek().getValue());
			
			Map<String, Object> aiResponse = aiService.sendToFlask(dto);
			//결과 저장
			save.setIsAnomaly((Boolean) aiResponse.get("anomaly"));
			save.setIsOverspending((Boolean) aiResponse.get("overspending"));
			noteAddRepository.save(save);
			//결과 반환
			Map<String,Object> result = new HashMap<>();
			result.put("save", save);
			result.put("anomaly", aiResponse.get("anomaly"));
			result.put("overspending", aiResponse.get("overspending"));
			//사용자 맞춤 ai 전환 확인
			noteAddService.checkAndTrain(UserID);
			
			if(aiResponse.containsKey("recommendation")) {
				result.put("recommendation", aiResponse.get("recommendation"));
			}
			return ResponseEntity.ok(result);
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("저장 실패 : "+e.getMessage());
		}
	}
	public int convertHourToGroup(int hour){
	    if(hour < 7) return 1;
	    if(hour < 9) return 2;
	    if(hour < 11) return 3;
	    if(hour < 13) return 4;
	    if(hour < 15) return 5;
	    if(hour < 17) return 6;
	    if(hour < 19) return 7;
	    if(hour < 21) return 8;
	    if(hour < 23) return 9;
	    return 10;
	  }
	//내역보여주기
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
	//삭제
	@CrossOrigin(origins="*")
	@GetMapping("/delete/{id}")
	public ResponseEntity<Void> delete(@PathVariable("id") Long id){
		System.out.println("delete 요청 들어옴: " + id);
		noteAddService.deleteExpense(id);
		return ResponseEntity.noContent().build();
	}
	//월간리포트
	@GetMapping("/report/monthly/{userId}")
	public ResponseEntity<?> getMonthlyReport(@PathVariable("userId") Long userID){
		try {
			MonthlyReportDto report = noteAddService.generateMonthlyReport(userID);
			return ResponseEntity.ok(report);
		}catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("리포트 생성 중 오류: "+e.getMessage());
		}
	}
	//사용자 피드백
	@PostMapping("/report/feedback")
	public ResponseEntity<?> feedback(@RequestBody FeedbackDto dto){
		NoteAdd note = noteAddRepository.findById(dto.getNoteId()).orElseThrow(() -> new RuntimeException("Not found"));
		note.setUserFeedback(dto.isAgree());
		noteAddRepository.save(note);
		return ResponseEntity.ok("피드백 반영 완료");
	}
	
	//수정
	@PutMapping("/update/{id}")
	public ResponseEntity<?> updateNote(@PathVariable Long id, @RequestBody Map<String, Object> updatedData) {
	    try {
	        NoteAdd note = noteAddRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("수정할 항목이 존재하지 않습니다."));

	        note.setAmount((Integer) updatedData.get("amount"));
	        note.setContent((String) updatedData.get("content"));
	        note.setCategory((String) updatedData.get("category"));
	        note.setMemo((String) updatedData.get("memo"));
	        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
	        note.setCreatedAt(LocalDateTime.parse((String) updatedData.get("createdAt"), formatter));
	        note.setIsRegularExpense((Boolean) updatedData.get("isRegularExpense"));
	        note.setNotifyOverspend((Boolean) updatedData.get("notifyOverspend"));
	        note.setIsIncome((Boolean) updatedData.get("isIncome"));

	        noteAddRepository.save(note);
	        return ResponseEntity.ok("수정 완료");
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("수정 실패: " + e.getMessage());
	    }
	}
	
	// 금액 합계 조회
	@GetMapping("/total")
	public ResponseEntity<?> getCategoryTotal(
	        @RequestParam("userId") Long userId,
	        @RequestParam("category") String category,
	        @RequestParam("year") int year,
	        @RequestParam("month") int month
	) {
	    try {
	        User user = userRepository.findById(userId)
	                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

	        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
	        LocalDateTime end = start.plusMonths(1);
	        
	        int total = noteAddRepository.findByUserAndCreatedAtBetween(user, start, end).stream()
	                .filter(n -> category.equals(n.getCategory()) && !n.getIsIncome())
	                .mapToInt(NoteAdd::getAmount)
	                .sum();

	        Map<String, Integer> result = new HashMap<>();
	        result.put("total", total);
	        return ResponseEntity.ok(result);
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("합계 조회 실패: " + e.getMessage());
	    }
	}


}