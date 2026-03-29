package com.hoya.learning.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoya.learning.repository.entity.ProblemStatisticJpaEntity;
import com.hoya.learning.repository.jpa.ProblemStatisticJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/submission-controller-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
class SubmissionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProblemStatisticJpaRepository statisticJpaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 객관식_정답_제출_시_201과_CORRECT가_반환된다() throws Exception {
        Map<String, Object> request = Map.of("choiceNumbers", List.of(3));

        mockMvc.perform(post("/api/problems/1/submissions")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.problemId").value(1))
                .andExpect(jsonPath("$.data.answerStatus").value("CORRECT"))
                .andExpect(jsonPath("$.data.explanation").value("default는 접근 제어자가 아닙니다."))
                .andExpect(jsonPath("$.data.answer.correctChoiceNumbers[0]").value(3));
    }

    @Test
    void 객관식_오답_제출_시_WRONG이_반환된다() throws Exception {
        Map<String, Object> request = Map.of("choiceNumbers", List.of(1));

        mockMvc.perform(post("/api/problems/1/submissions")
                        .header("X-User-Id", 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.answerStatus").value("WRONG"));
    }

    @Test
    void 객관식_부분정답_제출_시_PARTIAL이_반환된다() throws Exception {
        Map<String, Object> request = Map.of("choiceNumbers", List.of(1, 2));

        mockMvc.perform(post("/api/problems/3/submissions")
                        .header("X-User-Id", 3)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.answerStatus").value("PARTIAL"));
    }

    @Test
    void 주관식_정답_제출_시_CORRECT와_정답_텍스트가_반환된다() throws Exception {
        Map<String, Object> request = Map.of("subjectiveAnswer", "서울");

        mockMvc.perform(post("/api/problems/2/submissions")
                        .header("X-User-Id", 4)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.answerStatus").value("CORRECT"))
                .andExpect(jsonPath("$.data.answer.correctSubjectiveAnswers[0]").value("서울"));
    }

    @Test
    void 이미_제출한_문제_재제출_시_409와_ALREADY_SUBMITTED_PROBLEM이_반환된다() throws Exception {
        // user 99는 문제 1을 이미 제출한 상태
        Map<String, Object> request = Map.of("choiceNumbers", List.of(3));

        mockMvc.perform(post("/api/problems/1/submissions")
                        .header("X-User-Id", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_SUBMITTED_PROBLEM"));
    }

    @Test
    void 답안_없이_제출_시_400이_반환된다() throws Exception {
        mockMvc.perform(post("/api/problems/1/submissions")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 빈_선택지_리스트로_제출_시_400이_반환된다() throws Exception {
        Map<String, Object> request = Map.of("choiceNumbers", List.of());

        mockMvc.perform(post("/api/problems/1/submissions")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void 존재하지_않는_문제_제출_시_404와_PROBLEM_NOT_FOUND가_반환된다() throws Exception {
        Map<String, Object> request = Map.of("choiceNumbers", List.of(1));

        mockMvc.perform(post("/api/problems/999/submissions")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROBLEM_NOT_FOUND"));
    }

    /**
     * 정답 제출 후 problem_statistics의 카운트가 증가하는지 검증한다.
     * 초기 통계: total=30, correct=15
     * CORRECT 제출 후: total=31, correct=16
     */
    @Test
    void 정답_제출_후_통계가_갱신된다() throws Exception {
        Map<String, Object> request = Map.of("choiceNumbers", List.of(3));

        mockMvc.perform(post("/api/problems/1/submissions")
                        .header("X-User-Id", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ProblemStatisticJpaEntity stat = statisticJpaRepository.findByProblemId(1L).orElseThrow();
        assertThat(stat.getTotalSolvedUserCount()).isEqualTo(31);
        assertThat(stat.getCorrectSolvedUserCount()).isEqualTo(16);
    }

    /**
     * 오답 제출 후 total만 증가하고, correct는 변하지 않는지 검증한다.
     * 초기 통계: total=30, correct=15
     * WRONG 제출 후: total=31, correct=15
     */
    @Test
    void 오답_제출_후_정답_카운트는_변하지_않는다() throws Exception {
        Map<String, Object> request = Map.of("choiceNumbers", List.of(1));

        mockMvc.perform(post("/api/problems/1/submissions")
                        .header("X-User-Id", 6)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        ProblemStatisticJpaEntity stat = statisticJpaRepository.findByProblemId(1L).orElseThrow();
        assertThat(stat.getTotalSolvedUserCount()).isEqualTo(31);
        assertThat(stat.getCorrectSolvedUserCount()).isEqualTo(15);
    }

    /**
     * user 88은 문제 1을 CORRECT로 제출한 상태 (선택지 3번)
     * 통계: 30명 중 15명 정답 → 50%
     */
    @Test
    void 풀이_상세_조회_시_제출_정보와_정답_및_정답률이_반환된다() throws Exception {
        mockMvc.perform(get("/api/problems/1/submission")
                        .header("X-User-Id", 88))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.problemId").value(1))
                .andExpect(jsonPath("$.data.answerStatus").value("CORRECT"))
                .andExpect(jsonPath("$.data.explanation").value("default는 접근 제어자가 아닙니다."))
                .andExpect(jsonPath("$.data.submittedChoiceNumbers[0]").value(3))
                .andExpect(jsonPath("$.data.answer.correctChoiceNumbers[0]").value(3))
                .andExpect(jsonPath("$.data.answerCorrectRate").value(50.0));
    }

    @Test
    void 풀이_이력이_없는_문제_조회_시_404와_SUBMISSION_NOT_FOUND가_반환된다() throws Exception {
        // user 1은 문제 1을 제출한 적 없음
        mockMvc.perform(get("/api/problems/1/submission")
                        .header("X-User-Id", 1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SUBMISSION_NOT_FOUND"));
    }

    @Test
    void 풀이_상세_조회_시_통계가_30명_미만이면_answerCorrectRate가_null이다() throws Exception {
        // user 88이 문제 2(주관식)를 제출 후 상세 조회 (문제 2는 통계 없음)
        Map<String, Object> submitRequest = Map.of("subjectiveAnswer", "서울");
        mockMvc.perform(post("/api/problems/2/submissions")
                        .header("X-User-Id", 88)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/problems/2/submission")
                        .header("X-User-Id", 88))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answerCorrectRate", nullValue()));
    }

    /**
     * user 77은 문제 2(주관식)를 '서울'로 제출한 상태
     * submittedSubjectiveAnswer와 correctSubjectiveAnswers가 응답에 포함되는지 검증
     */
    @Test
    void 주관식_풀이_상세_조회_시_제출한_답안과_정답이_반환된다() throws Exception {
        mockMvc.perform(get("/api/problems/2/submission")
                        .header("X-User-Id", 77))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.problemId").value(2))
                .andExpect(jsonPath("$.data.answerStatus").value("CORRECT"))
                .andExpect(jsonPath("$.data.submittedSubjectiveAnswer").value("서울"))
                .andExpect(jsonPath("$.data.answer.correctSubjectiveAnswers[0]").value("서울"))
                .andExpect(jsonPath("$.data.submittedChoiceNumbers").isArray())
                .andExpect(jsonPath("$.data.submittedChoiceNumbers").isEmpty())
                .andExpect(jsonPath("$.data.answer.correctChoiceNumbers", nullValue()));
    }
}
