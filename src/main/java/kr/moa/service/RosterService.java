package kr.moa.service;

import kr.moa.domain.RosterEntry;
import kr.moa.repository.RosterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로스터(가입 허용 명단) 업로드/집계 로직.
 */
@Service
public class RosterService {

    private final RosterRepository rosterRepo;

    public RosterService(RosterRepository rosterRepo) {
        this.rosterRepo = rosterRepo;
    }

    /**
     * "학번,이름" 형식의 여러 줄 텍스트를 파싱하여 upsert.
     *   구분자는 콤마(,) 또는 탭(\t) 허용. 빈 줄/공백 줄은 무시.
     *   학번이 숫자가 아니면 건너뛴다.
     *   학년 = 학번 첫 글자, 반 = 학번 2~3번째 글자.
     *
     * @return 저장(신규+갱신)된 항목 수
     */
    @Transactional
    public int bulkUpsert(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        int count = 0;
        for (String rawLine : text.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            // 콤마 또는 탭으로 분리
            String[] parts = line.split("[,\\t]");
            if (parts.length == 0) continue;

            String studentNo = parts[0].trim();
            if (studentNo.isEmpty() || !studentNo.matches("\\d+")) {
                continue; // 학번이 숫자가 아니면 스킵
            }
            String name = (parts.length >= 2) ? parts[1].trim() : "";

            String grade = studentNo.length() >= 1 ? studentNo.substring(0, 1) : null;
            String klass = studentNo.length() >= 3 ? studentNo.substring(1, 3) : null;

            // upsert: 있으면 갱신, 없으면 신규
            RosterEntry entry = rosterRepo.findById(studentNo).orElse(null);
            if (entry == null) {
                entry = new RosterEntry(studentNo, name, grade, klass);
            } else {
                entry.setName(name);
                entry.setGrade(grade);
                entry.setKlass(klass);
            }
            rosterRepo.save(entry);
            count++;
        }
        return count;
    }

    @Transactional(readOnly = true)
    public long count() {
        return rosterRepo.count();
    }
}
