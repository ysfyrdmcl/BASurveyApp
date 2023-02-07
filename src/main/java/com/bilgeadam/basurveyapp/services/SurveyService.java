package com.bilgeadam.basurveyapp.services;

import com.bilgeadam.basurveyapp.configuration.EmailService;
import com.bilgeadam.basurveyapp.configuration.jwt.JwtService;
import com.bilgeadam.basurveyapp.dto.request.SurveyCreateRequestDto;
import com.bilgeadam.basurveyapp.dto.request.SurveyResponseQuestionRequestDto;
import com.bilgeadam.basurveyapp.dto.request.SurveyUpdateRequestDto;
import com.bilgeadam.basurveyapp.dto.request.SurveyUpdateResponseRequestDto;
import com.bilgeadam.basurveyapp.entity.Classroom;
import com.bilgeadam.basurveyapp.entity.Question;
import com.bilgeadam.basurveyapp.entity.Response;
import com.bilgeadam.basurveyapp.entity.Survey;
import com.bilgeadam.basurveyapp.entity.User;
import com.bilgeadam.basurveyapp.exceptions.custom.AlreadyAnsweredSurveyException;
import com.bilgeadam.basurveyapp.exceptions.custom.QuestionsAndResponsesDoesNotMatchException;
import com.bilgeadam.basurveyapp.entity.base.BaseEntity;
import com.bilgeadam.basurveyapp.entity.enums.Role;
import com.bilgeadam.basurveyapp.exceptions.custom.ResourceNotFoundException;
import com.bilgeadam.basurveyapp.exceptions.custom.UserInsufficientAnswerException;
import com.bilgeadam.basurveyapp.repositories.ClassroomRepository;
import com.bilgeadam.basurveyapp.repositories.ResponseRepository;
import com.bilgeadam.basurveyapp.repositories.SurveyRepository;
import com.bilgeadam.basurveyapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final ClassroomRepository classroomRepository;
    private final ResponseRepository responseRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final JwtService jwtService;

    public List<Survey> getSurveyList() {
        return new ArrayList<>(surveyRepository.findAllActive());
    }

    public Page<Survey> getSurveyPage(Pageable pageable) {
        return surveyRepository.findAllActive(pageable);
    }

    public Survey create(SurveyCreateRequestDto dto) {

        Survey survey = Survey.builder()
                .surveyTitle(dto.getSurveyTitle())
                .questions(dto.getQuestions())
                .courseTopic(dto.getCourseTopic())
                .build();
        return surveyRepository.save(survey);
    }

    public Survey update(Long surveyId, SurveyUpdateRequestDto dto) {

        Optional<Survey> surveyToBeUpdated = surveyRepository.findActiveById(surveyId);
        if (surveyToBeUpdated.isEmpty()) {
            throw new ResourceNotFoundException("Survey is not found");
        }
        surveyToBeUpdated.get().setSurveyTitle(dto.getSurveyTitle());
        return surveyRepository.save(surveyToBeUpdated.get());
    }

    public void delete(Long surveyId) {

        Optional<Survey> surveyToBeDeleted = surveyRepository.findActiveById(surveyId);
        if (surveyToBeDeleted.isEmpty()) {
            throw new ResourceNotFoundException("Survey is not found");
        }
        surveyRepository.softDelete(surveyToBeDeleted.get());
    }

    public Survey findByOid(Long surveyId) {

        Optional<Survey> surveyById = surveyRepository.findActiveById(surveyId);
        if (surveyById.isEmpty()) {
            throw new ResourceNotFoundException("Survey is not found");
        }
        return surveyById.get();
    }

    public Survey responseSurveyQuestions(Long surveyId, SurveyResponseQuestionRequestDto dto) {

        Survey survey = surveyRepository.findActiveById(surveyId)
            .orElseThrow(() -> new ResourceNotFoundException("Survey is not Found."));

        if(Boolean.FALSE.equals(crossCheckSurveyQuestionsAndCreateResponses(survey,dto.getCreateResponses()))){
            throw new UserInsufficientAnswerException("User must response all the questions.");
        }

        Optional<Long> currentUserIdOptional= Optional.of((Long) SecurityContextHolder.getContext().getAuthentication().getCredentials());
        Long currentUserId = currentUserIdOptional.orElseThrow(() -> new ResourceNotFoundException("Token does not contain User Info"));
        User currentUser = userRepository.findActiveById(currentUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User is not found"));

        List<Long> participantIdList = survey.getUsers()
            .parallelStream()
            .map(User::getOid)
            .toList();
        if(participantIdList.contains(currentUserId)){
            throw new AlreadyAnsweredSurveyException("User cannot answer a survey more than once.");
        }

        List<Question> surveyQuestions = survey.getQuestions();
        List<Response> responses = dto.getCreateResponses().keySet()
            .parallelStream()
            .map((id -> Response.builder()
                .responseString(dto.getCreateResponses().get(id).trim())
                .question(surveyQuestions
                    .stream()
                    .filter(question -> question.getOid().equals(id))
                    .findAny()
                    .orElse(null))
                .user(currentUser)
                .build()))
            .toList();
        for(Response response : responses){
            Optional<Question> questionOptional = surveyQuestions
                .parallelStream()
                .filter(question -> question.getOid().equals(response.getQuestion().getOid()))
                .findAny();
            questionOptional.ifPresent(question -> question.getResponses().add(response));
        }
        survey.getUsers().add(currentUser);
        currentUser.getSurveys().add(survey);

        return surveyRepository.save(survey);
    }
//    public Survey updateSurveyAnswers(Long surveyId, SurveyUpdateResponseRequestDto dto) {
//        Survey survey = surveyRepository.findActiveById(surveyId)
//                .orElseThrow(() -> new ResourceNotFoundException("Survey is not Found"));
//        if(survey.getEndDate().before(new Date())){
//            throw new ResourceNotFoundException("Survey is Expired.");
//        }
//        if(Boolean.FALSE.equals(crossCheckSurveyQuestionsAndUpdateResponses(survey,dto.getUpdateResponseMap()))){
//            throw new QuestionsAndResponsesDoesNotMatchException("Questions does not match with responses.");
//        }
//        Optional<Long> currentUserIdOptional= Optional.of((Long) SecurityContextHolder.getContext().getAuthentication().getCredentials());
//        Long currentUserId = currentUserIdOptional.orElseThrow(() -> new ResourceNotFoundException("Token does not contain User Info"));
//        List<Response> currentUserResponses = survey.getQuestions()
//            .stream()
//            .flatMap(question -> question.getResponses().stream())
//            .filter(response -> response.getUser().getOid().equals(currentUserId))
//            .collect(Collectors.toList());
//
//        currentUserResponses
//            .parallelStream()
//            .filter(response -> dto.getUpdateResponseMap().containsKey(response.getOid()))
//            .forEach(response -> response.setResponseString(dto.getUpdateResponseMap().get(response.getOid())));
//        responseRepository.saveAll(currentUserResponses);
//        return survey;
//    }
//    public Survey assignSurveyToClassroom(Long surveyId, Long classroomId) {
//
//        Survey survey = surveyRepository.findActiveById(surveyId)
//            .orElseThrow(() -> new ResourceNotFoundException("Survey is not Found"));
//        if(survey.getEndDate().before(new Date())){
//            throw new ResourceNotFoundException("Survey is Expired.");
//        }
//        Classroom classroom = classroomRepository.findActiveById(classroomId)
//            .orElseThrow(() -> new ResourceNotFoundException("Classroom is not Found"));
//
//        survey.getClassrooms().add(classroom);
//
//        /**
//         * Email service will be scheduled with accordance startDate of Survey.
//         */
////        Map<String,String> emailTokenMap = classroom.getUsers()
////            .parallelStream()
////            .collect(Collectors.toMap(User::getEmail, user -> jwtService.generateMailToken(user.getEmail(),survey.getOid())));
////        emailService.sendSurveyMail(emailTokenMap);
//
//        return surveyRepository.save(survey);
//    }

    public List<Survey> findByClassroomOid(Long classroomOid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("authentication failure.");
        }
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("authentication failure.");
        }
        Long userOid = (Long) authentication.getCredentials();
        User user = userRepository.findActiveById(userOid).orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        if (user.getRole() == Role.ASSISTANT_TRAINER || user.getRole() == Role.MASTER_TRAINER) {
            Classroom classroom = classroomRepository.findActiveById(classroomOid).orElseThrow(() -> new ResourceNotFoundException("Classroom does not exist"));
            if (!classroom.getUsers().contains(user)) {
                throw new AccessDeniedException("authentication failure.");
            }
        }
        Optional<Classroom> classroomOptional = classroomRepository.findActiveById(classroomOid);
        if (classroomOptional.isEmpty()) {
            throw new ResourceNotFoundException("Classroom is not found.");
        }
        List<Survey> surveyList = surveyRepository.findAllActive();
        List<Survey> surveysWithTheOidsOfTheClasses = surveyList
                .stream()
                .filter(survey -> survey.getClassrooms()
                        .stream()
                        .map(BaseEntity::getOid)
                        .toList().contains(classroomOptional.get().getOid()))
                .toList();
        return surveysWithTheOidsOfTheClasses;
    }
    private Boolean crossCheckSurveyQuestionsAndCreateResponses(Survey survey, Map<Long, String> getCreateResponses) {

        Set<Long> surveyQuestionIdSet = survey.getQuestions()
            .parallelStream()
            .map(Question::getOid)
            .collect(Collectors.toSet());
        Set<Long> createResponseQuestionIdSet = getCreateResponses.keySet();

        return surveyQuestionIdSet.equals(createResponseQuestionIdSet);
    }
    private Boolean crossCheckSurveyQuestionsAndUpdateResponses(Survey survey, Map<Long, String> updateResponseMap) {
        Set<Long> surveyQuestionIdSet = survey.getQuestions()
            .parallelStream()
            .map(Question::getOid)
            .collect(Collectors.toSet());
        Set<Long> updateResponseQuestionIdSet = updateResponseMap.keySet();

        return surveyQuestionIdSet.containsAll(updateResponseQuestionIdSet);
    }
}