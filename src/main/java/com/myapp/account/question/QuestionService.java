package com.myapp.account.question;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {
	private final QuestionRepository questionRepository;
	
	public List<Question> getList(){
		return this.questionRepository.findAll();
	}
	
//	public Question getQuestion(Integer id) {
//		Optional<Question> question = this.questionRepository.findById(id);
//		if(question.isPresent()) {
//			return question.get();
//		}
//	}
}
