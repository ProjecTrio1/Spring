package com.myapp.account.note_add;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.myapp.account.user.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "add")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoteAdd {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private int amount;
	private String category;
	private String content;
	private String memo;
	
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;
	private Boolean isRegularExpense;
	private Boolean notifyOverspend;
	private Boolean isIncome;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonBackReference //순환 직렬화 방지
	private User user;
	
	private Boolean isAnomaly;
	private Boolean isOverspending;
	private Boolean userFeedback;
}