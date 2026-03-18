package com.quinzex.service;

import com.quinzex.dto.*;
import com.quinzex.entity.Questions;
import com.quinzex.repository.QuestionsRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExamService implements IExamService {

    private final QuestionsRepo questionsRepo;

    public ExamService(QuestionsRepo questionsRepo) {
        this.questionsRepo = questionsRepo;
    }

    @Override
    @Transactional
    public String createQuestion(List<CreateQuestion> createQuestions) {
        List<Questions> questionsList = createQuestions.stream().map(question -> {
            Questions questions = new Questions();
            questions.setQuestion(question.getQuestion());
            questions.setOpt1(question.getOption1());
            questions.setOpt2(question.getOption2());
            questions.setOpt3(question.getOption3());
            questions.setOpt4(question.getOption4());
            questions.setCorrectOption(question.getCorrectAnswer().toUpperCase());
            questions.setCategory(question.getCategory());
            questions.setChapterId(question.getChapterId());
            return questions;
        }).toList();

        questionsRepo.saveAll(questionsList);
        return questionsList.size() + " questions added successfully";
    }

    @Override
    public ScoreWithAnswers getScore(List<AnswerRequest> answers) {
        if (answers == null || answers.isEmpty()) {
            return new ScoreWithAnswers(0, List.of());
        }

        List<Long> questionIds = answers.stream()
                .map(AnswerRequest::getQuestionId)
                .distinct()
                .toList();

        Map<Long, String> answerMap = answers.stream()
                .collect(Collectors.toMap(
                        AnswerRequest::getQuestionId,
                        a -> a.getSelectedOpt().toUpperCase()));

        List<Questions> questions = questionsRepo.findAllByIdIn(questionIds);

        long score = questions.stream()
                .filter(q -> answerMap.containsKey(q.getId()) &&
                        q.getCorrectOption().equalsIgnoreCase(answerMap.get(q.getId())))
                .count();

        List<CorrectOptionResponse> correctOptionResponses = questions.stream()
                .map(q -> new CorrectOptionResponse(q.getId(), q.getCorrectOption()))
                .toList();

        return new ScoreWithAnswers(score, correctOptionResponses);
    }

    @Transactional
    @Override
    public String editQuestion(Long id, CreateQuestion createQuestion) {
        Questions questions = questionsRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Question Not Found"));
        questions.setQuestion(createQuestion.getQuestion());
        questions.setOpt1(createQuestion.getOption1());
        questions.setOpt2(createQuestion.getOption2());
        questions.setOpt3(createQuestion.getOption3());
        questions.setOpt4(createQuestion.getOption4());
        questions.setCorrectOption(createQuestion.getCorrectAnswer().toUpperCase());
        questionsRepo.save(questions);
        return "Question Edited Successfully";
    }

    @Transactional
    @Override
    public String deleteQuestion(List<Long> ids) {
        List<Questions> questions = questionsRepo.findAllById(ids);
        if (questions.isEmpty()) {
            throw new RuntimeException("No Questions Found");
        }
        questionsRepo.deleteAll(questions);
        return questions.size() + " questions deleted successfully";
    }

    @Override
    public List<QuestionsResponse> getRandomQuestionsByCategory(String category, int limit) {
        return questionsRepo.findRandomByCategory(category, limit)
                .stream()
                .map(this::mapToQuestionResponse)
                .toList();
    }

    @Override
    public List<QuestionsResponse> getRandomQuestionsByChapterID(Long chapterId, int limit) {
        return questionsRepo.findRandomByChapterId(chapterId, limit)
                .stream()
                .map(this::mapToQuestionResponse)
                .toList();
    }

    @Override
    public List<String> getAllExamCategories() {
        return questionsRepo.findDistinctCategories();
    }

    private QuestionsResponse mapToQuestionResponse(Questions questions) {
        return new QuestionsResponse(
                questions.getId(),
                questions.getQuestion(),
                questions.getOpt1(),
                questions.getOpt2(),
                questions.getOpt3(),
                questions.getOpt4(),
                questions.getCategory(),
                questions.getChapterId()
        );
    }
}