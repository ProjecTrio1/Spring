package com.myapp.account.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myapp.account.note_add.NoteAdd;
import com.myapp.account.question.Question;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique= true)
	private String username;
	
	private String gender;
	private Integer age;
	private String password;
	
	@Column(unique = true)
	@Email(message= "이메일 형식이 아닙니다.")
	private String email;
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@JsonIgnore//순환 직렬화 방지
	private List<NoteAdd> notes = new ArrayList<>();
	
	@ManyToMany(mappedBy = "scarp")
	private Set<Question> scrappedQuestions;
}

