package com.example.kana_quiz;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() { return "index"; }

    // 種類選択後のレベル選択画面
    @GetMapping("/levels")
    public String levels(@RequestParam String type) {
        // type の存在チェックは将来追加でもOK（rome2hira/hira2rome/audio2hira）
        return "levels";
    }

    // クイズ開始（type=出題タイプ, level=カテゴリ）
    @GetMapping("/quiz")
    public String quiz(@RequestParam String type, @RequestParam String level) {
        return "quiz";
    }

    @GetMapping("/result")
    public String result() { return "result"; }
}
