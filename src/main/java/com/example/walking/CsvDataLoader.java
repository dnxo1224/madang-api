package com.example.walking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
//
///**
// * 1회용 CSV 적재기. application.properties 에 csv.loader.enabled=true 를 추가하고
// * 앱을 한 번 실행하면 자동으로 적재된다.
// * 적재 완료 후 csv.loader.enabled=false (또는 줄 삭제) 로 되돌린다.
// */
//@Component
//@ConditionalOnProperty(name = "csv.loader.enabled", havingValue = "true")
//public class CsvDataLoader implements ApplicationRunner {
//
//    private final JdbcTemplate jdbc;
//
//    @Value("${csv.loader.path}")
//    private String csvPath;
//
//    private static final Map<String, Integer> LEVEL_MAP = Map.of(
//            "매우쉬움", 1, "쉬움", 2, "보통", 3, "어려움", 4, "매우어려움", 5);
//
//    private static final Pattern P1 = Pattern.compile("([0-9]+)시간\\s*([0-9]+)분");
//    private static final Pattern P2 = Pattern.compile("([0-9]+)시간");
//    private static final Pattern P3 = Pattern.compile("([0-9]+)분");
//
//    public CsvDataLoader(JdbcTemplate jdbc) {
//        this.jdbc = jdbc;
//    }
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM course", Integer.class);
//        if (count != null && count > 0) {
//            System.out.println("[CsvDataLoader] course 테이블에 이미 데이터가 있습니다. 스킵.");
//            return;
//        }
//
//        System.out.println("[CsvDataLoader] 시작: " + csvPath);
//        List<String[]> rows = readCsv(csvPath);
//        System.out.println("[CsvDataLoader] CSV 읽기 완료 — " + rows.size() + "행");
//
//        insertRegions(rows);
//
//        Map<String, Integer> regionIdMap = new HashMap<>();
//        jdbc.query("SELECT region_id, sido, sigungu FROM region",
//                (org.springframework.jdbc.core.RowCallbackHandler) rs ->
//                        regionIdMap.put(rs.getString("sido") + "|" + rs.getString("sigungu"),
//                                rs.getInt("region_id")));
//
//        int[] result = insertCourses(rows, regionIdMap);
//        System.out.printf("[CsvDataLoader] 완료 — 적재: %d행, 스킵: %d행%n", result[0], result[1]);
//        System.out.println("[CsvDataLoader] application.properties 의 csv.loader.enabled 를 false로 되돌리세요.");
//    }
//
//    // ── region INSERT ────────────────────────────────────────────────────────
//    private void insertRegions(List<String[]> rows) {
//        Set<String> seen = new LinkedHashSet<>();
//        for (String[] row : rows) {
//            String signgu = row[4].trim();
//            if (signgu.contains(" ") && seen.add(signgu)) {
//                int sp = signgu.indexOf(' ');
//                String sido = signgu.substring(0, sp);
//                String sigungu = signgu.substring(sp + 1).trim();
//                jdbc.update("INSERT IGNORE INTO region (sido, sigungu) VALUES (?, ?)", sido, sigungu);
//            }
//        }
//        System.out.println("[CsvDataLoader] region 적재 완료");
//    }
//
//    // ── course batch INSERT ───────────────────────────────────────────────────
//    private int[] insertCourses(List<String[]> rows, Map<String, Integer> regionIdMap) {
//        int inserted = 0, skipped = 0;
//
//        List<Object[]> batch = new ArrayList<>();
//        for (String[] row : rows) {
//            Object[] params = buildCourseParams(row, regionIdMap);
//            if (params == null) { skipped++; continue; }
//            batch.add(params);
//            inserted++;
//        }
//
//        jdbc.batchUpdate("""
//                INSERT INTO course
//                  (course_id, course_name, flag_name, region_id, level_id,
//                   length_km, time_min, toilet_info, store_info, lat, lon, description)
//                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
//                """, batch);
//
//        return new int[]{inserted, skipped};
//    }
//
//    private Object[] buildCourseParams(String[] row, Map<String, Integer> regionIdMap) {
//        try {
//            String courseId  = row[0].trim();
//            String flagName  = row[1].trim();
//            String courseName = row[2].trim();
//            String courseDc  = row[3].trim();
//            String signguNm  = row[4].trim();
//            String levelNm   = row[5].trim();
//            String detailLt  = row[7].trim();
//            String timeCn    = row[9].trim();
//            String toiletDc  = row[11].trim();
//            String cvntlNm   = row[12].trim();
//            String latStr    = row[14].trim();
//            String lonStr    = row[15].trim();
//
//            BigDecimal lat = new BigDecimal(latStr);
//            BigDecimal lon = new BigDecimal(lonStr);
//
//            if (!signguNm.contains(" ")) return null;
//            int sp = signguNm.indexOf(' ');
//            String sido    = signguNm.substring(0, sp);
//            String sigungu = signguNm.substring(sp + 1).trim();
//            Integer regionId = regionIdMap.get(sido + "|" + sigungu);
//            if (regionId == null) return null;
//
//            int levelId = LEVEL_MAP.getOrDefault(levelNm, 3);
//
//            BigDecimal lengthKm = null;
//            if (detailLt.matches("[0-9]+(\\.[0-9]+)?")) {
//                lengthKm = new BigDecimal(detailLt).setScale(1, RoundingMode.HALF_UP);
//            }
//
//            return new Object[]{
//                    courseId,
//                    courseName,
//                    flagName.isEmpty()  ? null : flagName,
//                    regionId,
//                    levelId,
//                    lengthKm,
//                    parseTimeMin(timeCn),
//                    toiletDc.isEmpty()  ? null : toiletDc,
//                    cvntlNm.isEmpty()   ? null : cvntlNm,
//                    lat, lon,
//                    courseDc.isEmpty()  ? null : courseDc
//            };
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    // ── CSV 파싱 ─────────────────────────────────────────────────────────────
//    private List<String[]> readCsv(String path) throws IOException {
//        List<String[]> rows = new ArrayList<>();
//        try (BufferedReader br = new BufferedReader(
//                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
//            br.readLine(); // 헤더(+BOM) 스킵
//            String line;
//            while ((line = br.readLine()) != null) {
//                if (!line.isBlank()) rows.add(parseLine(line));
//            }
//        }
//        return rows;
//    }
//
//    /** 쌍따옴표 enclosed, 따옴표 안 쉼표 처리 */
//    private String[] parseLine(String line) {
//        List<String> fields = new ArrayList<>();
//        boolean inQuotes = false;
//        StringBuilder cur = new StringBuilder();
//        for (int i = 0; i < line.length(); i++) {
//            char c = line.charAt(i);
//            if (c == '"') {
//                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
//                    cur.append('"'); i++;
//                } else {
//                    inQuotes = !inQuotes;
//                }
//            } else if (c == ',' && !inQuotes) {
//                fields.add(cur.toString());
//                cur = new StringBuilder();
//            } else {
//                cur.append(c);
//            }
//        }
//        fields.add(cur.toString());
//        return fields.toArray(new String[0]);
//    }
//
//    // ── 소요시간 파싱 ────────────────────────────────────────────────────────
//    private Integer parseTimeMin(String s) {
//        if (s == null || s.isBlank()) return null;
//        // 1. N시간 M분 (앞에 '약' 있어도 처리)
//        Matcher m1 = P1.matcher(s);
//        if (m1.find()) return Integer.parseInt(m1.group(1)) * 60 + Integer.parseInt(m1.group(2));
//        // 2. N시간 (분/왕복/~/박 없는 경우)
//        if (!s.contains("분") && !s.contains("~") && !s.contains("왕복") && !s.contains("박")) {
//            Matcher m2 = P2.matcher(s);
//            if (m2.find()) return Integer.parseInt(m2.group(1)) * 60;
//        }
//        // 3. N분 (시간 없는 경우)
//        if (!s.contains("시간")) {
//            Matcher m3 = P3.matcher(s);
//            if (m3.find()) return Integer.parseInt(m3.group(1));
//        }
//        // 4. 순수 숫자
//        if (s.trim().matches("[0-9]+")) return Integer.parseInt(s.trim());
//        return null;
//    }
//}
