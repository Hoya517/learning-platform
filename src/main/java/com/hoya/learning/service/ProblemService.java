package com.hoya.learning.service;

import com.hoya.learning.service.command.GetRandomProblemCommand;
import com.hoya.learning.service.result.RandomProblem;

public interface ProblemService {

    RandomProblem getRandomProblem(GetRandomProblemCommand command);
}
