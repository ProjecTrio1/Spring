package com.myapp.account.answer;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.myapp.account.question.DataNotFoundException;
import com.myapp.account.question.Question;
import com.myapp.account.user.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AnswerService {
	private final AnswerRepository answerRepository;
	
	public Answer getAnswer(Integer id) {
		return answerRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("answer not found"));
	}
	
	public void create(Question question, String content, User author) {
		Answer answer = new Answer();
		answer.setContent(content);
		answer.setCreateDate(LocalDateTime.now());
		answer.setQuestion(question);
		answer.setAuthor(author);
		this.answerRepository.save(answer);
	}
	
	public void modify(Answer answer, String content) {
		answer.setContent(content);
		answer.setCreateDate(LocalDateTime.now());
		this.answerRepository.save(answer);
	}
	
	public void delete(Answer answer) {
		this.answerRepository.delete(answer);
	}
	
	public void vote(Answer answer, User user) {
		answer.getVoter().add(user);
		this.answerRepository.save(answer);
	}
}
