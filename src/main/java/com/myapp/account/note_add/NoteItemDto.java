package com.myapp.account.note_add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteItemDto {
	private Long id;
	private String content;
	private int amount;
	private String date;
	private boolean isAnomaly;
	private boolean isOverspending;
	private Boolean userFeedback; //boolean과 다르게 true, false, null을 가짐
}
//개별 소비 항목에 대한 정보 제공