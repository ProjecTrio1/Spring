package com.myapp.account.question;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myapp.account.answer.Answer;
import com.myapp.account.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Question {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 200)
	private String subject;
	
	@Column(columnDefinition = "TEXT")
	private String content;
	
	private LocalDateTime createDate;
	
	@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
	private List<Answer> answerList;
	
	@ManyToOne
	private User author;
	
	private LocalDateTime modifyDate;
	
	@ManyToMany
	Set<User> voter;
	
	@ManyToMany
	@JoinTable(
		    name = "question_scrap",
		    joinColumns = @JoinColumn(name = "question_id"),
		    inverseJoinColumns = @JoinColumn(name = "scrap_id")
		)
	@JsonIgnore
	private Set<User> scrap = new HashSet<>();
}
