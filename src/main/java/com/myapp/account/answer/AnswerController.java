package com.myapp.account.answer;

import java.security.Principal;

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

import com.myapp.account.question.Question;
import com.myapp.account.question.QuestionService;
import com.myapp.account.user.User;
import com.myapp.account.user.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/answer")
@RequiredArgsConstructor
@RestController
public class AnswerController {
	private final QuestionService questionService;
	private final AnswerService answerService;
	private final UserService userService;
	
	//@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{id}")
	public ResponseEntity<?> createAnswer(@RequestBody @Valid AnswerForm answerForm, @PathVariable("id") Integer id, Principal principal,  BindingResult bindingResult){
		if(bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body("답변 내용이 비어있습니다.");
		}
		try {
			Question question = questionService.getQuestion(id);
			User user = userService.getUser(principal.getName());
			answerService.create(question, answerForm.getContent(),user);
			return ResponseEntity.ok("답글이 등록되었습니다.");
		}catch(Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("답변 등록 실패 : "+e.getMessage());
		}
	}
	
	//@PreAuthorize("isAuthenticated()")
	@PutMapping("/modify/{id}")
	public ResponseEntity<?> modifyAnswer(@RequestBody @Valid AnswerForm answerForm,@PathVariable("id") Integer id, Principal principal, BindingResult bindingResult){
		if(bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body("입력값이 유효하지 않습니다.");
		}
		Answer answer = this.answerService.getAnswer(id);
		if(!answer.getAuthor().getUsername().equals(principal.getName())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}
		this.answerService.modify(answer, answerForm.getContent());
		return ResponseEntity.ok("수정이 완료되었습니다.");
	}
	
	//@PreAuthorize("isAuthenticated()")
	@GetMapping("/delete/{id}")
	public ResponseEntity<?> deleteAnswer(@PathVariable("id") Integer id,Principal principal){
		Answer answer = this.answerService.getAnswer(id);
		if(!answer.getAuthor().getUsername().equals(principal.getName())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}
		this.answerService.delete(answer);
		return ResponseEntity.ok("삭제되었습니다.");
	}
	
	//@PreAuthorize("isAuthenticated()")
	@GetMapping("/vote/{id}")
	public ResponseEntity<?> voteAnswer(@PathVariable("id") Integer id, Principal principal){
		Answer answer = this.answerService.getAnswer(id);
		User user = this.userService.getUser(principal.getName());
		this.answerService.vote(answer, user);
		return ResponseEntity.ok("추천되었습니다.");
	}
	
}
