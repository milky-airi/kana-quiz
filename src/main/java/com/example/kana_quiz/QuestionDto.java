// src/main/java/com/example/kana_quiz/QuestionDto.java
package com.example.kana_quiz;

public record QuestionDto(
        String type,      // "audio2hira" など
        String prompt,    // 表示用の文字（audioの場合は正解語）
        String[] choices, // TANGOは固定4択、他レベルはnullでもOK
        String answer,    // 正解
        String level,     // 例: "TANGO"
        String audio,     // 例: "/audio/audio2hira/TANGO/えき.m4a"（null可）
        String meaning    // ★ 追加：意味（単語レベルで表示、null 可）
) {}
