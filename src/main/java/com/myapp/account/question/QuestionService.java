package com.myapp.account.question;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.myapp.account.user.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class QuestionService {
	private final QuestionRepository questionRepository;
	
	public List<Question> getList(){
		Sort sort = Sort.by(Sort.Order.desc("createDate"));
		return this.questionRepository.findAll(sort);
	}
	
	public Question getQuestion(Integer id) {
		return questionRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException("question not found"));
	}
	public void create(String subject, String content, User user) {
		Question question = new Question();
		question.setSubject(subject);
		question.setContent(content);
		question.setCreateDate(LocalDateTime.now());
		question.setAuthor(user);
		this.questionRepository.save(question);
	}
	public void modify(Question question, String subject, String content) {
		question.setSubject(subject);
		question.setContent(content);
		question.setModifyDate(LocalDateTime.now());
		this.questionRepository.save(question);
	}
	
	public void delete(Question question) {
		this.questionRepository.delete(question);
	}
	
	public void vote(Question question, User user) {
		question.getVoter().add(user);
		this.questionRepository.save(question);
	}
	
	public boolean toggleScrap(Question question, User user) {
		if(question.getScrap().contains(user)) {
			question.getScrap().remove(user);
			this.questionRepository.save(question);
			return false;
		}else {
			question.getScrap().add(user);
			this.questionRepository.save(question);
			return true;
		}
	}
}
