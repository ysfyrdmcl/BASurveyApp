package com.bilgeadam.basurveyapp.services;

import com.bilgeadam.basurveyapp.configuration.jwt.JwtService;
import com.bilgeadam.basurveyapp.dto.request.ResponseRequestDto;
import com.bilgeadam.basurveyapp.dto.request.ResponseRequestSaveDto;
import com.bilgeadam.basurveyapp.dto.response.AnswerResponseDto;
import com.bilgeadam.basurveyapp.entity.Response;
import com.bilgeadam.basurveyapp.entity.User;
import com.bilgeadam.basurveyapp.exceptions.custom.QuestionNotFoundException;
import com.bilgeadam.basurveyapp.repositories.QuestionRepository;
import com.bilgeadam.basurveyapp.exceptions.custom.ResponseNotFoundException;
import com.bilgeadam.basurveyapp.repositories.ResponseRepository;
import com.bilgeadam.basurveyapp.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResponseService {
    private final ResponseRepository responseRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;



    public void createResponse(ResponseRequestDto responseRequestDto){
        // The same answer can be recreated over and over again. There not will be exist checking
        Response response = Response.builder()
                .responseString(responseRequestDto.getResponseString())
                .build();
        responseRepository.save(response);
    }

    public void updateResponse(ResponseRequestDto responseRequestDto){
        Optional<Response>updatedResponse = responseRepository.findActiveById(responseRequestDto.getResponseOid());
        if(updatedResponse.isEmpty()){
           throw new ResponseNotFoundException("There's a error while finding response");
        }
        else {
            updatedResponse.get().setResponseString(responseRequestDto.getResponseString());
            responseRepository.save(updatedResponse.get());
        }
    }

    public AnswerResponseDto findByIdResponse(Long responseOid){
        Optional <Response> response = responseRepository.findById(responseOid);
        if(response.isEmpty()){
            throw new ResponseNotFoundException("There's a error while finding response");
        }
        return AnswerResponseDto.builder()
                .responseString(response.get().getResponseString())
                .userOid(response.get().getUser().getOid())
                .questionOid(response.get().getQuestion().getOid())
                .build();
    }

    public List<AnswerResponseDto> findAll() {
        List<Response> findAllList = responseRepository.findAllActive();
        List<AnswerResponseDto> responseDtoList = new ArrayList<>();
        findAllList.forEach(response ->
                responseDtoList.add(AnswerResponseDto.builder()
                .responseString(response.getResponseString())
                .userOid(response.getUser().getOid())
                .questionOid(response.getQuestion().getOid())
                .build()));
        return responseDtoList;
    }

    public Boolean deleteResponseById(Long responseOid){
      Optional<Response> response= responseRepository.findActiveById(responseOid);
        if (response.isEmpty()) {
            throw new ResponseNotFoundException("There's a error while finding response");
        } else {
            responseRepository.softDeleteById(response.get().getOid());
            return true;
        }
    }

    public Boolean saveAll( String token, List<ResponseRequestSaveDto> responseRequestSaveDtoList){
        Optional<User> user = userRepository.findByEmail(jwtService.extractEmail(token));// tokendan gelen id var gibi kabul edildi.

        if(user.isPresent()) {
            responseRequestSaveDtoList.forEach(response -> { //gelenleri listeye kaydetmek için for each kullanıldı.
                responseRepository.save(Response.builder()
                        .user(user.get()) //tokendan gelen userı, teker teker büğtün cevaplara kaydetmiş oluyoruz(bu user bunu cevapladı.).
                        .responseString(response.getResponseString())
                        .question(questionRepository.findActiveById(response.getQuestionOid()).orElseThrow(()-> new QuestionNotFoundException("Question not found"))) //orElseThrow() get yapıyor.boşsa exception atıyor. içine kendi exeption atar. a
                        .build());
            });
            return true;
        }
        else{
             return false;
        }
    }
}
