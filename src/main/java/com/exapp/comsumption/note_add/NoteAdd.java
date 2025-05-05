package com.exapp.comsumption.note_add;

import java.time.LocalDateTime;

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
	private LocalDateTime createdAt = LocalDateTime.now();
	private Boolean isRegularExpense;
	private Boolean notifyOverspend;
	private Boolean isIncome;
}
