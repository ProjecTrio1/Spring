package com.exapp.comsumption.note_add;

import java.time.LocalDateTime;
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
}
