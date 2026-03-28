package com.hoya.learning.mock;

import com.hoya.learning.common.RandomHolder;
import com.hoya.learning.presentation.ProblemController;
import com.hoya.learning.presentation.SubmissionController;
import com.hoya.learning.service.ProblemServiceImpl;
import com.hoya.learning.service.SubmissionServiceImpl;

public class TestContainer {

    public final FakeProblemRepository problemRepository;
    public final FakeProblemStatisticRepository statisticRepository;
    public final FakeProblemSubmissionRepository submissionRepository;
    public final ProblemController problemController;
    public final SubmissionController submissionController;

    public TestContainer(RandomHolder randomHolder) {
        this.problemRepository = new FakeProblemRepository();
        this.statisticRepository = new FakeProblemStatisticRepository();
        this.submissionRepository = new FakeProblemSubmissionRepository();

        ProblemServiceImpl problemService = new ProblemServiceImpl(problemRepository, statisticRepository, randomHolder);
        SubmissionServiceImpl submissionService = new SubmissionServiceImpl(problemRepository, submissionRepository, statisticRepository);

        this.problemController = new ProblemController(problemService);
        this.submissionController = new SubmissionController(submissionService);
    }
}
