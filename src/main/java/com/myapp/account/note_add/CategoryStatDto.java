package com.myapp.account.note_add;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryStatDto {
	public String category;
	public int totalAmount;
	public int anomalyCount;
	public int overspendingCount;
	public List<NoteItemDto> flaggedItems; //항목 리스트
}
