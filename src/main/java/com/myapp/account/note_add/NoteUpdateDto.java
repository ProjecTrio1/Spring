package com.myapp.account.note_add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteUpdateDto {
	private int amount;
	private String category;
	private String content;
	private String memo;
	private String createdAt;
	private Boolean isRegularExpense;
	private Boolean notifyOverspend;
	private Boolean isIncome;
}