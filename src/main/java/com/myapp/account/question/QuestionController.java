package com.myapp.account.question;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.account.user.User;
import com.myapp.account.user.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/question")
public class QuestionController {
	
	private final QuestionService questionService;
	private final UserRepository userRepository;
	
	@GetMapping("/list")
	public ResponseEntity<List<Question>> list() {
		List<Question> questionList = questionService.getList();
		return ResponseEntity.ok(questionList);
	}
	
	@GetMapping("/detail/{id}")
	public ResponseEntity<?> detail(@PathVariable("id") Integer id) {
		try {
			Question question = questionService.getQuestion(id);
			return ResponseEntity.ok(question);
		}catch(DataNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("글을 찾을 수 없습니다.");
		}
	}
	
//	@PreAuthorize("isAuthenticated()")
//	@GetMapping("/create")
//	public String questionCreate(QuestionForm questionForm) {
//		return ResponseEntity.ok();
//	}
	
	//@PreAuthorize("isAuthenticated()")
	@PostMapping("/create")
	public ResponseEntity<?> createQuestion(@RequestBody QuestionForm questionForm, @RequestParam("userID") Long userID){
		if(questionForm.getSubject() == null || questionForm.getSubject().isEmpty()
				|| questionForm.getContent() == null || questionForm.getContent().isEmpty()) {
			return ResponseEntity.badRequest().body("제목과 내용을 입력하세요.");
		}
		User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		questionService.create(questionForm.getSubject(), questionForm.getContent(),user);
		return ResponseEntity.ok("글이 등록되었습니다.");
	}
	
	//@PreAuthorize("isAuthenticated()")
	@PostMapping("/modify/{id}")
	public ResponseEntity<?> modifyQuestion(@RequestBody @Valid QuestionForm questionForm, @PathVariable("id") Integer id, @RequestParam("userID") Long userID,BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body("입력값이 유효하지 않습니다.");
		}
		
		User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		Question question = this.questionService.getQuestion(id);
		if(!question.getAuthor().getId().equals(user.getId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}
		this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
		return ResponseEntity.ok("수정이 완료되었습니다.");
	}
	
	//@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public ResponseEntity<?> deleteQuestion(@PathVariable("id") Integer id, @RequestParam("userID") Long userID){
		Question question = this.questionService.getQuestion(id);
		User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		if(!question.getAuthor().getUsername().equals(user.getUsername())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}
		this.questionService.delete(question);
		return ResponseEntity.ok("삭제되었습니다.");
	}
	
	//@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public ResponseEntity<?> voteQuestion(@PathVariable("id") Integer id, @RequestParam("userID") Long userID){
		Question question = this.questionService.getQuestion(id);
		User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		if(question.getVoter().contains(user)) {
			return ResponseEntity.badRequest().body("이미 추천되었습니다.");
		}
		this.questionService.vote(question, user);
		return ResponseEntity.ok("추천되었습니다.");
	}
	//추천 수 기준으로 내림차순
	@GetMapping("/vote/list")
	public ResponseEntity<?> getSortedQuestionList(){
		List<Question> questionList = questionService.getList();
		questionList.sort((q1,q2)-> Integer.compare(q2.getVoter().size(), q1.getVoter().size()));
		return ResponseEntity.ok(questionList);
	}
	//스크랩 적용, 취소 동시에 가능
	@PostMapping("/scrap/{id}")
	public ResponseEntity<?> toggleScrap(@PathVariable("id") Integer id, @RequestParam("userID") Long userID){
		Question question = this.questionService.getQuestion(id);
		User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		boolean isScrapped = this.questionService.toggleScrap(question,user);
		return ResponseEntity.ok(isScrapped? "스크랩되었습니다.":"스크랩이 취소되었습니다.");
	}
}
