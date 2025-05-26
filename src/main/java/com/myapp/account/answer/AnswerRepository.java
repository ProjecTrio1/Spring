package com.myapp.account.answer;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import com.myapp.account.question.Question;

public interface AnswerRepository extends JpaRepository<Answer, Integer>{
	List<Answer> findByQuestion(Question question,Sort sort);
}
