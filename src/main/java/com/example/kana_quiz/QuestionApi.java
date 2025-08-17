// src/main/java/com/example/kana_quiz/QuestionApi.java
package com.example.kana_quiz;

import com.example.kana_quiz.service.QuestionService;
import com.example.kana_quiz.QuestionDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuestionApi {

    private final QuestionService svc;

    public QuestionApi(QuestionService svc) {
        this.svc = svc;
    }

    @GetMapping("/levels/{level}/questions")
    public List<QuestionDto> get(
            @PathVariable String level,
            @RequestParam String type,
            @RequestParam(defaultValue = "10") int count
    ) {
        return svc.get(type, level, count);
    }
}
