package com.hoya.learning.mock;

import com.hoya.learning.domain.Problem;
import com.hoya.learning.repository.ProblemRepository;

import java.util.*;

public class FakeProblemRepository implements ProblemRepository {

    private final Map<Long, Problem> store = new HashMap<>();
    private final Map<String, List<Long>> candidates = new HashMap<>();

    public void save(Problem problem) {
        store.put(problem.getId(), problem);
    }

    public void addCandidate(Long chapterId, Long userId, Problem problem) {
        store.put(problem.getId(), problem);
        candidates.computeIfAbsent(chapterId + ":" + userId, k -> new ArrayList<>()).add(problem.getId());
    }

    @Override
    public Optional<Problem> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Long> findCandidateProblemIds(Long chapterId, Long userId) {
        return candidates.getOrDefault(chapterId + ":" + userId, List.of());
    }
}
