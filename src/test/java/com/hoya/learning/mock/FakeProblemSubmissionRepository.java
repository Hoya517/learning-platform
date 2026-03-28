package com.hoya.learning.mock;

import com.hoya.learning.domain.ProblemSubmission;
import com.hoya.learning.repository.ProblemSubmissionRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeProblemSubmissionRepository implements ProblemSubmissionRepository {

    private final Map<String, ProblemSubmission> store = new HashMap<>();

    @Override
    public boolean existsByUserIdAndProblemId(Long userId, Long problemId) {
        return store.containsKey(userId + ":" + problemId);
    }

    @Override
    public Optional<ProblemSubmission> findByUserIdAndProblemId(Long userId, Long problemId) {
        return Optional.ofNullable(store.get(userId + ":" + problemId));
    }

    @Override
    public ProblemSubmission save(ProblemSubmission submission) {
        store.put(submission.getUserId() + ":" + submission.getProblemId(), submission);
        return submission;
    }
}
