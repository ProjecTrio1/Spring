package com.myapp.account.note_add;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class NoteAddService {
	private final NoteAddRepository noteAddRepository;
	
	public NoteAddService(NoteAddRepository noteAddRepository) {
		this.noteAddRepository = noteAddRepository;
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
}