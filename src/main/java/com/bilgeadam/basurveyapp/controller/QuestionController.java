package com.bilgeadam.basurveyapp.controller;

import com.bilgeadam.basurveyapp.dto.request.CreateQuestionDto;
import com.bilgeadam.basurveyapp.dto.request.QuestionFindByIdRequestDto;
import com.bilgeadam.basurveyapp.dto.request.UpdateQuestionDto;
import com.bilgeadam.basurveyapp.dto.response.AllQuestionResponseDto;
import com.bilgeadam.basurveyapp.dto.response.QuestionFindByIdResponseDto;
import com.bilgeadam.basurveyapp.entity.Question;
import com.bilgeadam.basurveyapp.services.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/test")
    public String test() {
        return "question";
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createQuestion(@RequestBody @Valid CreateQuestionDto createQuestionDto, @Valid Long userOid) {
        questionService.createQuestion(createQuestionDto, userOid);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Boolean> updateQuestion(@RequestBody @Valid UpdateQuestionDto updateQuestionDto, @Valid Long userOid) {
        questionService.updateQuestion(updateQuestionDto, userOid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/findbyid")
    public ResponseEntity<QuestionFindByIdResponseDto> findById(@RequestBody @Valid QuestionFindByIdRequestDto questionFindByIdRequestDto) {
        return ResponseEntity.ok(questionService.findById(questionFindByIdRequestDto.getQuestionId()));
    }

    @GetMapping("/findall")
    public ResponseEntity<List<AllQuestionResponseDto>> findAll() {
        List<AllQuestionResponseDto> responseDtoList = questionService.findAll();
        return ResponseEntity.ok(responseDtoList);
    }

    @PostMapping("/delete")
    public ResponseEntity<Boolean> delete(@RequestBody @Valid Long questionId, @Valid Long userOid) {
        return ResponseEntity.ok(questionService.delete(questionId, userOid));
    }

}
