package com.myapp.account.question;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.myapp.account.user.User;
import com.myapp.account.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/question")
public class QuestionController {
	
	private final QuestionService questionService;
	private final UserService userService;
	
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
	public ResponseEntity<?> createQuestion(@RequestBody QuestionForm questionForm, Principal principal){
		if(questionForm.getSubject() == null || questionForm.getSubject().isEmpty()
				|| questionForm.getContent() == null || questionForm.getContent().isEmpty()) {
			return ResponseEntity.badRequest().body("제목과 내용을 입력하세요.");
		}
		User user = this.userService.getUser(principal.getName());
		questionService.create(questionForm.getSubject(), questionForm.getContent(),user);
		return ResponseEntity.ok("글이 등록되었습니다.");
	}
	
	//@PreAuthorize("isAuthenticated()")
	@PutMapping("/modify/{id}")
	public ResponseEntity<?> modifyQuestion(@RequestBody @Valid QuestionForm questionForm, @PathVariable("id") Integer id, Principal principal,BindingResult bindingResult) {
		if(bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body("입력값이 유효하지 않습니다.");
		}
		Question question = this.questionService.getQuestion(id);
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}
		this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
		return ResponseEntity.ok("수정이 완료되었습니다.");
	}
	
	//@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public ResponseEntity<?> deleteQuestion(@PathVariable("id") Integer id, Principal principal){
		Question question = this.questionService.getQuestion(id);
		if(!question.getAuthor().getUsername().equals(principal.getName())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}
		this.questionService.delete(question);
		return ResponseEntity.ok("삭제되었습니다.");
	}
	
	//@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public ResponseEntity<?> voteQuestion(@PathVariable("id") Integer id, Principal principal){
		Question question = this.questionService.getQuestion(id);
		User user = this.userService.getUser(principal.getName());
		this.questionService.vote(question, user);
		return ResponseEntity.ok("추천되었습니다.");
	}
	//스크랩 적용, 취소 동시에 가능
	@PostMapping("/scrap/{id}")
	public ResponseEntity<?> toggleScrap(@PathVariable("id") Integer id, Principal principal){
		Question question = this.questionService.getQuestion(id);
		User user = this.userService.getUser(principal.getName());
		boolean isScrapped = this.questionService.toggleScrap(question,user);
		return ResponseEntity.ok(isScrapped? "스크랩되었습니다.":"스크랩이 취소되었습니다.");
	}
}
