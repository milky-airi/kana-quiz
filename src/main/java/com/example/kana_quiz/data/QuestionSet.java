// src/main/java/com/example/kana_quiz/data/QuestionSet.java
package com.example.kana_quiz.data;

import java.util.List;

public record QuestionSet(
        String type,      // 例: "audio2hira"
        String level,     // 例: "TANGO"
        List<Item> questions
) {
    public static record Item(
            String prompt,     // 例: "えき"
            String answer,     // 例: "えき"
            String[] choices,  // TANGOは固定4択、他レベルはnull/省略可
            String audio,       // 例: "/audio/audio2hira/TANGO/えき.m4a"（省略可）
            String meaning
    ) {}
}
