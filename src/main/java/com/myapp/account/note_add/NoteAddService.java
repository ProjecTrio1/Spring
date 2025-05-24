package com.myapp.account.note_add;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.myapp.account.ai.AiService;
import com.myapp.account.ai.RequestSendToFlaskDto;
import com.myapp.account.user.User;
import com.myapp.account.user.UserRepository;

@Service
public class NoteAddService {
	private final NoteAddRepository noteAddRepository;
	private final UserRepository userRepository;
	private final AiService aiService;
	
	public NoteAddService(NoteAddRepository noteAddRepository, UserRepository userRepository, AiService aiService) {
		this.noteAddRepository = noteAddRepository;
		this.userRepository = userRepository;
		this.aiService = aiService;
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
			newExpense.setIsIncome(false);
			newExpense.setNotifyOverspend(false);
			
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
				categoryMap.putIfAbsent(cat, new CategoryStatDto(cat, 0, 0, 0,new ArrayList<>()));
				CategoryStatDto stat = categoryMap.get(cat);
				
				stat.setTotalAmount(stat.getTotalAmount()+note.getAmount());
//				System.out.println("카테고리 체크: " + note.getCategory());

				boolean added = false; //이상소비 또는 과소비일 경우 리스트에 추가
				if(Boolean.TRUE.equals(note.getIsAnomaly())) {
					stat.setAnomalyCount(stat.getAnomalyCount()+1);
					anomalyCount++;
					added = true;
				}
				if(Boolean.TRUE.equals(note.getIsOverspending())) {
					stat.setOverspendingCount(stat.getOverspendingCount()+1);
					overspendingCount++;
					added = true;
				}
				
				if(added) {
					stat.getFlaggedItems().add(toDto(note));
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
		LocalDateTime end = lastMonth.atEndOfMonth().atTime(LocalTime.MAX);
		
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		List<NoteAdd> lastMonthNotes = noteAddRepository.findByUserAndCreatedAtBetween(user, start, end);
		Map<String, Integer> map = new HashMap<>();
		
		for(NoteAdd note : lastMonthNotes) {
			String category = note.getCategory();
			map.put(category, map.getOrDefault(category, 0)+note.getAmount());
		}
		return map;
	}
	
	private NoteItemDto toDto(NoteAdd note) {
		return new NoteItemDto(
				note.getId(),
				note.getContent(),
				note.getAmount(),
				note.getCreatedAt().toLocalDate().toString(),
				Boolean.TRUE.equals(note.getIsAnomaly()),
				Boolean.TRUE.equals(note.getIsOverspending()),
				note.getUserFeedback()
		);
	}
	//사용자 맞춤ai로 전환하기 위한 조건 판별
	public boolean shouldTrainPersonalModel(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		List<NoteAdd> records = noteAddRepository.findByUser(user);
		int spendingCount =0; //300건이상
		LocalDateTime spendingDate = null; //3개월이상
		
		for(NoteAdd note: records) {
			if(!note.getIsIncome()) {
				spendingCount++;
				LocalDateTime noteDate = note.getCreatedAt();
				if(spendingDate == null || noteDate.isBefore(spendingDate)) {
					spendingDate = noteDate; //제일 처음 소비 기록한 시간
				}
			}
		}
		boolean hasEnoughRecords = spendingCount >= 300;
		boolean hasEnoughMonths = false;
		
		if(spendingDate != null) {
			long monthsBetween = ChronoUnit.MONTHS.between(spendingDate, LocalDateTime.now());
			hasEnoughMonths = monthsBetween >= 3;
		}
		return hasEnoughRecords && hasEnoughMonths;
	}
	//조건 판별 후 학습 요청
	public void checkAndTrain(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));		
		boolean shouldTrainUnsupervised = shouldTrainPersonalModel(userId);
		boolean shouldTrainSupervised = shouldTrainFeedback(userId);
		System.out.println("모델 전환 조건 충족 여부(비지도): " + shouldTrainUnsupervised);
		System.out.println("모델 전환 조건 충족 여부(지도): " + shouldTrainSupervised);
		int genderCode = "M".equalsIgnoreCase(user.getGender()) ? 1:0;
		
		if(shouldTrainUnsupervised || shouldTrainSupervised) {
			System.out.println("사용자 맞춤 모델 학습 요청 시작");
			List<NoteAdd> records = noteAddRepository.findByUser(user);
			RequestSendToFlaskDto dto = new RequestSendToFlaskDto();
			dto.setUserId(user.getId().toString());
			dto.setGender(genderCode);
			dto.setAgeGroup(user.getAge());
			
			try {
				if(shouldTrainUnsupervised) {
					aiService.requestTrainToFlask(dto, records);
				}else if(shouldTrainSupervised){
					aiService.requestTrainFeedback(dto, records);
				}
				System.out.println("Flask 요청 완료");
			}catch(JsonProcessingException e) {
				System.out.println("Flask 요청 실패 : "+ e.getMessage());
				e.printStackTrace();
			}
		}
	}
	//피드백 반영 후 지도 학습 전환 조건 판별
	public boolean shouldTrainFeedback(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
		List<NoteAdd> records = noteAddRepository.findByUser(user);
		
		long feedbackCount = records.stream()
				.filter(note -> Boolean.TRUE.equals(note.getUserFeedback()))
				.count();
		
		return feedbackCount >= 100;
	}
	//수정
	public void updateNote(Long noteId, NoteUpdateDto dto) {
		NoteAdd note = noteAddRepository.findById(noteId)
	            .orElseThrow(() -> new RuntimeException("수정할 항목이 존재하지 않습니다."));

	        note.setAmount(dto.getAmount());
	        note.setContent(dto.getContent());
	        note.setCategory(dto.getCategory());
	        note.setMemo(dto.getMemo());
	        note.setCreatedAt(LocalDateTime.parse(dto.getCreatedAt()));
	        note.setIsRegularExpense(dto.getIsRegularExpense());
	        note.setNotifyOverspend(dto.getNotifyOverspend());
	        note.setIsIncome(dto.getIsIncome());

	        noteAddRepository.save(note);
	}
	//카테고리 합계
	public int getCategoryTotal(Long userId, String category, int year, int month) {
		User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        
        return noteAddRepository.findByUserAndCreatedAtBetween(user, start, end).stream()
                .filter(n -> category.equals(n.getCategory()) && !n.getIsIncome())
                .mapToInt(NoteAdd::getAmount)
                .sum();
	}
}