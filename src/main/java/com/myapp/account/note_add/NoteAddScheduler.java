package com.myapp.account.note_add;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NoteAddScheduler {
	private final NoteAddService noteAddService;
	
	public NoteAddScheduler(NoteAddService noteAddService) {
		this.noteAddService = noteAddService;
	}
	
	@Scheduled(cron = "0 0 0 1 * *")
//	@Scheduled(cron = "*/60 * * * * *") //테스트용
	public void copyRegularExpenses() {
		noteAddService.replicateNextMonth();
	}
}
