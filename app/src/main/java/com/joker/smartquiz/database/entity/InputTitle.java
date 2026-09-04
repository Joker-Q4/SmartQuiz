package com.joker.smartquiz.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

/**
 * @author Joker
 * @since 2020/08/07
 */
@Entity(tableName = "input_title")
public class InputTitle {

    public InputTitle(@NonNull String id, @NonNull String fileName) {
        this.id = id;
        this.fileName = fileName;
        this.currentTime = System.currentTimeMillis();
    }

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String fileName;
    private long currentTime;

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(@NonNull String fileName) {
        this.fileName = fileName;
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
        InputTitle that = (InputTitle) o;
        return fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }
}
