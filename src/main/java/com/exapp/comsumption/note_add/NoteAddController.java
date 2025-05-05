package com.exapp.comsumption.note_add;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/note")
public class NoteAddController {
	private final NoteAddRepository noteAddRepository;
	
	public NoteAddController(NoteAddRepository noteAddRepository) {
		this.noteAddRepository = noteAddRepository;
	}
	
	@PostMapping("/add")
	public NoteAdd save(@RequestBody NoteAdd noteadd) {
		return noteAddRepository.save(noteadd);
	}
}
