package com.myapp.account.answer;

import java.time.LocalDateTime;
import java.util.Set;

import com.myapp.account.question.Question;
import com.myapp.account.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne; //하나의 질문에 여러개의 답변
import lombok.Data;

@Data
@Entity
public class Answer {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	private LocalDateTime createDate;
	
	@ManyToOne
	private Question question;
	
	@ManyToOne
	private User author;
	
	private LocalDateTime modifyDate;
	
	@ManyToMany
	Set<User> voter;
}
