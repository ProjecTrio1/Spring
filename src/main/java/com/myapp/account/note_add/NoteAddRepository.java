package com.myapp.account.note_add;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.myapp.account.user.User;


public interface NoteAddRepository extends JpaRepository<NoteAdd, Long>{
	List<NoteAdd> findByUser(User user);
	//정기지출
	List<NoteAdd> findByIsRegularExpenseTrueAndCreatedAtBetween(LocalDateTime start, LocalDateTime end);
	//삭제
	void deleteById(Long id);
	//월간리포트
	List<NoteAdd> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end );
}