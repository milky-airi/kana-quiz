// src/main/java/com/example/kana_quiz/service/QuestionService.java
package com.example.kana_quiz.service;

import com.example.kana_quiz.QuestionDto;
import com.example.kana_quiz.data.QuestionSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;

@Service
public class QuestionService {
    private final ObjectMapper om = new ObjectMapper();
    private final Map<String, List<BaseQ>> baseCache = new HashMap<>();
    private final Random rng = new SecureRandom();

    // ▼ランダム選択肢用プール（キーは "TYPE-LEVEL" をすべて大文字で）
    private static final Map<String, List<String>> ANSWER_POOL = Map.ofEntries(
            // HIRA2ROME
            Map.entry("HIRA2ROME-SEION", List.of(
                    "a","i","u","e","o",
                    "ka","ki","ku","ke","ko",
                    "sa","shi","su","se","so",
                    "ta","chi","tsu","te","to",
                    "na","ni","nu","ne","no",
                    "ha","hi","fu","he","ho",
                    "ma","mi","mu","me","mo",
                    "ya","yu","yo",
                    "ra","ri","ru","re","ro",
                    "wa","wo","n"
            )),
            Map.entry("HIRA2ROME-DAKUON", List.of(
                    "ga","gi","gu","ge","go",
                    "za","ji","zu","ze","zo",
                    // di/du は混乱防止で採用しない
                    "da","de","do",
                    "ba","bi","bu","be","bo",
                    "pa","pi","pu","pe","po"
            )),
            Map.entry("HIRA2ROME-YOUON", List.of(
                    "kya","kyu","kyo",
                    "sha","shu","sho",
                    "cha","chu","cho",
                    "nya","nyu","nyo",
                    "hya","hyu","hyo",
                    "mya","myu","myo",
                    "rya","ryu","ryo",
                    "gya","gyu","gyo",
                    "ja","ju","jo",
                    "bya","byu","byo",
                    "pya","pyu","pyo"
            )),

            // ROME2HIRA / AUDIO2HIRA はひらがなプール（ぢ・づは除外）
            Map.entry("ROME2HIRA-SEION", List.of(
                    "あ","い","う","え","お",
                    "か","き","く","け","こ",
                    "さ","し","す","せ","そ",
                    "た","ち","つ","て","と",
                    "な","に","ぬ","ね","の",
                    "は","ひ","ふ","へ","ほ",
                    "ま","み","む","め","も",
                    "や","ゆ","よ",
                    "ら","り","る","れ","ろ",
                    "わ","を","ん"
            )),
            Map.entry("ROME2HIRA-DAKUON", List.of(
                    "が","ぎ","ぐ","げ","ご",
                    "ざ","じ","ず","ぜ","ぞ",
                    // ぢ・づを入れない
                    "だ","で","ど",
                    "ば","び","ぶ","べ","ぼ",
                    "ぱ","ぴ","ぷ","ぺ","ぽ"
            )),
            Map.entry("ROME2HIRA-YOUON", List.of(
                    "きゃ","きゅ","きょ",
                    "しゃ","しゅ","しょ",
                    "ちゃ","ちゅ","ちょ",
                    "にゃ","にゅ","にょ",
                    "ひゃ","ひゅ","ひょ",
                    "みゃ","みゅ","みょ",
                    "りゃ","りゅ","りょ",
                    "ぎゃ","ぎゅ","ぎょ",
                    "じゃ","じゅ","じょ",
                    "びゃ","びゅ","びょ",
                    "ぴゃ","ぴゅ","ぴょ"
            )),

            // AUDIO2HIRA は同じプールを流用
            Map.entry("AUDIO2HIRA-SEION", List.of(
                    "あ","い","う","え","お",
                    "か","き","く","け","こ",
                    "さ","し","す","せ","そ",
                    "た","ち","つ","て","と",
                    "な","に","ぬ","ね","の",
                    "は","ひ","ふ","へ","ほ",
                    "ま","み","む","め","も",
                    "や","ゆ","よ",
                    "ら","り","る","れ","ろ",
                    "わ","を","ん"
            )),
            Map.entry("AUDIO2HIRA-DAKUON", List.of(
                    "が","ぎ","ぐ","げ","ご",
                    "ざ","じ","ず","ぜ","ぞ",
                    "だ","で","ど",
                    "ば","び","ぶ","べ","ぼ",
                    "ぱ","ぴ","ぷ","ぺ","ぽ"
            )),
            Map.entry("AUDIO2HIRA-YOUON", List.of(
                    "きゃ","きゅ","きょ",
                    "しゃ","しゅ","しょ",
                    "ちゃ","ちゅ","ちょ",
                    "にゃ","にゅ","にょ",
                    "ひゃ","ひゅ","ひょ",
                    "みゃ","みゅ","みょ",
                    "りゃ","りゅ","りょ",
                    "ぎゃ","ぎゅ","ぎょ",
                    "じゃ","じゅ","じょ",
                    "びゃ","びゅ","びょ",
                    "ぴゃ","ぴゅ","ぴょ"
            ))
            // TANGO は固定4択（JSON）使用
    );

    public List<QuestionDto> get(String type, String level, int count){
        final String key = (type + "-" + level).toUpperCase(Locale.ROOT);

        // 実ファイル名は大小区別せず（questions/{type}-{level}.json）
        var base = baseCache.computeIfAbsent(key, k -> loadBase(type, level));
        if (base.isEmpty()) return List.of();

        var shuffled = new ArrayList<>(base);
        Collections.shuffle(shuffled, rng);
        if (count > 0 && shuffled.size() > count) {
            shuffled = new ArrayList<>(shuffled.subList(0, count));
        }

        // プール（無ければベースのanswer一覧）
        List<String> baseAnswers = base.stream().map(b -> b.answer).distinct().toList();
        var pool = ANSWER_POOL.getOrDefault(key, baseAnswers);

        List<QuestionDto> out = new ArrayList<>(shuffled.size());
        for (BaseQ b : shuffled) {
            String audioPath = b.audio;
            if (audioPath == null && "audio2hira".equalsIgnoreCase(type)) {
                audioPath = "/audio/audio2hira/" + level + "/" + b.prompt + ".m4a";
            }

            if (b.fixedChoices != null && b.fixedChoices.length > 0) {
                // TANGO の固定4択にも同値フィルタ
                String[] filtered = filterEquivalentChoices(type, b.answer, b.fixedChoices);
                shuffleArray(filtered);
                out.add(new QuestionDto(type, b.prompt, filtered, b.answer, level, audioPath, b.meaning));
            } else {
                String[] arr = buildRandomChoices(type, b.answer, pool, 4);
                out.add(new QuestionDto(type, b.prompt, arr, b.answer, level, audioPath, b.meaning));
            }
        }
        return out;
    }

    private List<BaseQ> loadBase(String type, String level){
        String path = "questions/" + type + "-" + level + ".json";
        try (InputStream in = new ClassPathResource(path).getInputStream()){
            var set = om.readValue(in, QuestionSet.class);
            List<BaseQ> out = new ArrayList<>(set.questions().size());
            for (var it : set.questions()){
                out.add(new BaseQ(it.prompt(), it.answer(), it.choices(), it.audio(), it.meaning()));
            }
            return out;
        } catch(Exception e){
            e.printStackTrace();
            return List.of();
        }
    }

    // === ランダム4択（同値表記を除外） ============================
    private String[] buildRandomChoices(String type, String answer, List<String> pool, int choiceCount){
        Set<String> choices = new LinkedHashSet<>();
        choices.add(answer);

        String canonAns = canonical(type, answer);

        List<String> candidates = new ArrayList<>(pool);
        candidates.removeIf(c -> c.equals(answer) || canonical(type, c).equals(canonAns));

        Collections.shuffle(candidates, rng);
        for (String c : candidates) {
            choices.add(c);
            if (choices.size() >= choiceCount) break;
        }

        // 万一不足時の保険
        int guard = 1000;
        while (choices.size() < choiceCount && guard-- > 0 && !pool.isEmpty()) {
            String c = pool.get(rng.nextInt(pool.size()));
            if (!canonical(type, c).equals(canonAns)) choices.add(c);
        }

        String[] arr = choices.toArray(String[]::new);
        shuffleArray(arr);
        return arr;
    }

    // === 固定4択（TANGO）にも同値フィルタ ==========================
    private String[] filterEquivalentChoices(String type, String answer, String[] fixed){
        String canonAns = canonical(type, answer);
        List<String> out = new ArrayList<>(fixed.length);
        for (String c : fixed) {
            if (c.equals(answer)) { out.add(c); continue; }
            if (canonical(type, c).equals(canonAns)) continue; // 同値は除外
            out.add(c);
        }
        return out.toArray(String[]::new);
    }

    // === 正規化：type に応じた“同じ音”の丸め =======================
    private String canonical(String type, String s){
        if (type == null) return s;
        String t = type.toLowerCase(Locale.ROOT);
        if (t.startsWith("hira2rome")) return canonRomaji(s);
        if (t.startsWith("rome2hira") || t.startsWith("audio2hira")) return canonKana(s);
        return s;
    }

    // ローマ字の正規化（ヘボン式に寄せる）
    private String canonRomaji(String s){
        String x = s.toLowerCase(Locale.ROOT);
        x = x.replace("si","shi").replace("ti","chi").replace("tu","tsu");
        x = x.replace("zi","ji").replace("di","ji").replace("du","zu");
        x = x.replaceAll("[^a-z]", ""); // 保険
        return x;
    }

    // かなの正規化（ぢ→じ、づ→ず）
    private String canonKana(String s){
        return s.replace("ぢ","じ").replace("づ","ず");
    }

    private void shuffleArray(String[] a){
        for (int i=a.length-1; i>0; i--){
            int j = rng.nextInt(i+1);
            String tmp = a[i]; a[i]=a[j]; a[j]=tmp;
        }
    }

    // ベース問題
    private record BaseQ(String prompt, String answer, String[] fixedChoices, String audio, String meaning){}
}
