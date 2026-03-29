package com.hoya.learning.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/problem-controller-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
})
class ProblemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * user 11: 문제 2, 3 풀었음 → 후보 = [1] (유일 후보이므로 결정적)
     * 문제 1 통계: 30명 중 20명 정답 → 66.7% → 반올림 67%
     */
    @Test
    void 안_푼_문제를_조회하면_문제_정보와_정답률이_반환된다() throws Exception {
        mockMvc.perform(get("/api/chapters/1/problems/random")
                        .header("X-User-Id", 11))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.problemId").value(1))
                .andExpect(jsonPath("$.data.content").value("Java의 접근 제어자가 아닌 것은?"))
                .andExpect(jsonPath("$.data.type").value("MULTIPLE_CHOICE"))
                .andExpect(jsonPath("$.data.multipleAnswer").value(false))
                .andExpect(jsonPath("$.data.choices").isArray())
                .andExpect(jsonPath("$.data.choices.length()").value(5))
                .andExpect(jsonPath("$.data.answerCorrectRate").value(67.0));
    }

    /**
     * user 10: 문제 1, 2 풀었음 → 후보 = [3]
     * 문제 3 통계: 10명 → 30명 미만이므로 answerCorrectRate = null
     */
    @Test
    void 정답률_30명_미만_문제는_answerCorrectRate가_null이다() throws Exception {
        mockMvc.perform(get("/api/chapters/1/problems/random")
                        .header("X-User-Id", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.problemId").value(3))
                .andExpect(jsonPath("$.data.answerCorrectRate", nullValue()));
    }

    /**
     * user 20: 모든 문제 풀었음 → 후보 없음 → 404
     */
    @Test
    void 모든_문제를_풀었으면_404와_NO_AVAILABLE_PROBLEM이_반환된다() throws Exception {
        mockMvc.perform(get("/api/chapters/1/problems/random")
                        .header("X-User-Id", 20))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NO_AVAILABLE_PROBLEM"));
    }

    /**
     * user 30: 문제 2만 풀었음 → 후보 = [1, 3]
     * excludeProblemId=1 지정 시 후보 = [3] (유일 후보이므로 결정적)
     */
    @Test
    void excludeProblemId_지정_시_해당_문제를_제외한_문제가_반환된다() throws Exception {
        mockMvc.perform(get("/api/chapters/1/problems/random")
                        .header("X-User-Id", 30)
                        .param("excludeProblemId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.problemId").value(3));
    }

    /**
     * user 40: 문제 1, 2 풀었음 → 후보 = [3]
     * excludeProblemId=3 지정 시 후보 없음 → 404
     */
    @Test
    void excludeProblemId_제외_후_후보가_없으면_404와_NO_AVAILABLE_PROBLEM이_반환된다() throws Exception {
        mockMvc.perform(get("/api/chapters/1/problems/random")
                        .header("X-User-Id", 40)
                        .param("excludeProblemId", "3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NO_AVAILABLE_PROBLEM"));
    }
}
