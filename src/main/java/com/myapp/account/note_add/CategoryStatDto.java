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
	public List<NoteItemDto> flaggedItems; //과소비/이상소비 개별 항목들
}
//MonthlyReportDto 안에서 카테고리별 소비 통계 표현