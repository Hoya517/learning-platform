package com.hoya.learning.service;

import com.hoya.learning.service.command.GetSolveDetailCommand;
import com.hoya.learning.service.command.SubmitProblemCommand;
import com.hoya.learning.service.result.ProblemSolveDetail;
import com.hoya.learning.service.result.SubmissionResult;

public interface SubmissionService {

    SubmissionResult submit(SubmitProblemCommand command);

    ProblemSolveDetail getSolveDetail(GetSolveDetailCommand command);
}
