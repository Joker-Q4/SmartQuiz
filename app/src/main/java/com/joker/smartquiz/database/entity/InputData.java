package com.joker.smartquiz.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * @author Joker
 * @since 2020/08/07
 */
@SuppressWarnings("unused")
@Entity(tableName = "input_data")
public class InputData {

    public InputData(@NonNull String id, @NonNull String titleId, String question, String answer, String key, String col_a,
                     String col_b, String col_c, String col_d, String col_e, String col_f) {
        this.id = id;
        this.titleId = titleId;
        currentTime = System.currentTimeMillis();
        this.question = question;
        this.answer = answer;
        this.key = key;
        this.col_a = col_a;
        this.col_b = col_b;
        this.col_c = col_c;
        this.col_d = col_d;
        this.col_e = col_e;
        this.col_f = col_f;
    }

    @PrimaryKey
    @NonNull
    private String id;
    @NonNull
    private String titleId;
    private long currentTime;
    private String question;
    private String answer;
    private String key;
    private String col_a;
    private String col_b;
    private String col_c;
    private String col_d;
    private String col_e;
    private String col_f;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getTitleId() {
        return titleId;
    }

    public void setTitleId(@NonNull String titleId) {
        this.titleId = titleId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCol_a() {
        return col_a;
    }

    public void setCol_a(String col_a) {
        this.col_a = col_a;
    }

    public String getCol_b() {
        return col_b;
    }

    public void setCol_b(String col_b) {
        this.col_b = col_b;
    }

    public String getCol_c() {
        return col_c;
    }

    public void setCol_c(String col_c) {
        this.col_c = col_c;
    }

    public String getCol_d() {
        return col_d;
    }

    public void setCol_d(String col_d) {
        this.col_d = col_d;
    }

    public String getCol_e() {
        return col_e;
    }

    public void setCol_e(String col_e) {
        this.col_e = col_e;
    }

    public String getCol_f() {
        return col_f;
    }

    public void setCol_f(String col_f) {
        this.col_f = col_f;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputData that = (InputData) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
