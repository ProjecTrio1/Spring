package com.myapp.account.note_add;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.myapp.account.user.User;
import com.myapp.account.user.UserRepository;

@Service
public class NoteAddService {
	private final NoteAddRepository noteAddRepository;
	private final UserRepository userRepository;
	
	public NoteAddService(NoteAddRepository noteAddRepository, UserRepository userRepository) {
		this.noteAddRepository = noteAddRepository;
		this.userRepository = userRepository;
	}
	
	public NoteAdd saveNote(NoteAdd noteadd) {
		noteadd.setCreatedAt(LocalDateTime.now());
		return noteAddRepository.save(noteadd);
	}
	//삭제
	public void deleteExpense(Long id) {
		noteAddRepository.deleteById(id);
	}
	//정기지출
	public void replicateNextMonth() {
		YearMonth lastMonth = YearMonth.now().minusMonths(1);
		
		List<NoteAdd> regularExpenses = noteAddRepository.findByIsRegularExpenseTrueAndCreatedAtBetween(lastMonth.atDay(1).atStartOfDay(), lastMonth.atEndOfMonth().atTime(23,59,59));
		
		for (NoteAdd noteadd : regularExpenses) {
			NoteAdd newExpense = new NoteAdd();
			newExpense.setUser(noteadd.getUser());
			newExpense.setCategory(noteadd.getCategory());
			newExpense.setAmount(noteadd.getAmount());
			newExpense.setCreatedAt(noteadd.getCreatedAt().plusMonths(1));
			newExpense.setIsRegularExpense(true);
			
			noteAddRepository.save(newExpense);
			System.out.println("✅ 복사 완료: " + noteadd.getId());
		}
	}
	//월간 리포트
	public MonthlyReportDto generateMonthlyReport(Long userID) {
		LocalDateTime start = YearMonth.now().minusMonths(1).atDay(1).atStartOfDay();
		LocalDateTime end = YearMonth.now().minusMonths(1).atEndOfMonth().atTime(23,59);
		
		User user = userRepository.findById(userID).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		List<NoteAdd> notes = noteAddRepository.findByUserAndCreatedAtBetween(user, start, end);
		
		int totalAmount = 0;
		int anomalyCount = 0;
		int overspendingCount = 0;
		int lateNightTotal = 0;
		Map<String, CategoryStatDto> categoryMap = new HashMap<>();
		
		for(NoteAdd note : notes) {
			if(!note.getIsIncome()) {
				totalAmount += note.getAmount();
				String cat = note.getCategory();
				categoryMap.putIfAbsent(cat, new CategoryStatDto(cat, 0, 0, 0));
				CategoryStatDto stat = categoryMap.get(cat);
				
				stat.setTotalAmount(stat.getTotalAmount()+note.getAmount());
				
				if(Boolean.TRUE.equals(note.getIsAnomaly())) {
					stat.setAnomalyCount(stat.getAnomalyCount()+1);
					anomalyCount++;
				}
				if(Boolean.TRUE.equals(note.getIsOverspending())) {
					stat.setOverspendingCount(stat.getOverspendingCount()+1);
					overspendingCount++;
				}
				if(note.getCreatedAt().getHour() >=22) {
					lateNightTotal += note.getAmount();
				}
			}
		}
		//전월 데이터
		Map<String, Integer> lastMonthMap = getLastMonthCategoryTotals(userID);
		
		List<String> suggestions = new ArrayList<>();
		
		for(CategoryStatDto stat: categoryMap.values()) {
			if(stat.getAnomalyCount() >= 2) {
				suggestions.add(stat.getCategory()+ "항목에서 이상소비가 반복되었습니다.") ;
			}
			if(stat.getOverspendingCount() >= 5) {
				suggestions.add(stat.getCategory()+"에서 과소비가 5번이상 반복되었습니다.");
			}
			int last = lastMonthMap.getOrDefault(stat.getCategory(), 0);
			if(last>0 && stat.getTotalAmount()>= last*2) {
				suggestions.add(stat.getCategory()+"가 지난날보다 2배이상 증가했습니다. (지난달 : "+last+"원 → 이번달 : "+stat.getTotalAmount()+"원)");
			}
		}
		//시간대 집중 소비
		if(totalAmount > 0 && (double) lateNightTotal / totalAmount >= 0.3) {
			suggestions.add("오후 10시 이후 소비가 많습니다. 늦은 시간 소비를 줄여보세요.");
		}
		suggestions.add("이번 달 총 소비는 " + String.format("%,d원", totalAmount)+"입니다.");
		
		String finalSuggestion = String.join(" / ", suggestions);
		
		return new MonthlyReportDto(
				YearMonth.now().minusMonths(1).toString(),
				totalAmount,
				anomalyCount,
				overspendingCount,
				new ArrayList<>(categoryMap.values()),
				finalSuggestion
		);
	}
	//전월 카테고리별 소비 총액
	private Map<String, Integer> getLastMonthCategoryTotals(Long userId){
		YearMonth lastMonth = YearMonth.now().minusMonths(2);
		LocalDateTime start = lastMonth.atDay(1).atStartOfDay();
		LocalDateTime end = lastMonth.atEndOfMonth().atTime(23,59);
		
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		List<NoteAdd> lastMonthNotes = noteAddRepository.findByUserAndCreatedAtBetween(user, start, end);
		Map<String, Integer> map = new HashMap<>();
		
		for(NoteAdd note : lastMonthNotes) {
			String category = note.getCategory();
			map.put(category, map.getOrDefault(category, 0)+note.getAmount());
		}
		return map;
	}
}