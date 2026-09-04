package com.joker.smartquiz.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import com.joker.smartquiz.database.entity.InputData;

import java.util.List;

/**
 * @author Joker
 * @since 2020/08/07
 */
@SuppressWarnings("unused")
@Dao
public interface InputDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(InputData inputData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addAll(List<InputData> inputDataMutableList);

    @Delete
    void remove(InputData inputData);

    @Query("DELETE FROM input_data")
    void removeAll();

    @Update
    void modify(InputData inputData);

    @Query("SELECT * FROM input_data")
    List<InputData> getAll();

    @SuppressWarnings(RoomWarnings.QUERY_MISMATCH)
    @Query("SELECT id, titleId, currentTime, `key` FROM input_data WHERE titleId IN (:titleIds)")
    List<InputData> getSentence(List<String> titleIds);

    @Query("SELECT * FROM input_data WHERE titleId = :titleId")
    List<InputData> getByTitle(String titleId);

    @Query("SELECT COUNT(*) FROM input_data WHERE titleId = :titleId")
    int countByTitle(String titleId);

    @Query("DELETE FROM input_data WHERE titleId = :titleId")
    void removeByTitle(String titleId);

    @Query("SELECT * FROM input_data where id = :id limit 1")
    InputData getOne(String id);

    @Query("SELECT * FROM input_data ORDER BY currenttime DESC LIMIT 0, 10")
    List<InputData> getFront();

    @Query("SELECT * FROM input_data where `key` is null and titleId = :titleId")
    List<InputData> getNewImport(String titleId);

}
