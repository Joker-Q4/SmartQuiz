package com.joker.smartquiz.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.joker.smartquiz.database.entity.InputTitle;

import java.util.List;

/**
 * @author Joker
 * @since 2020/08/07
 */
@SuppressWarnings("unused")
@Dao
public interface InputTitleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(InputTitle inputTitle);

    @Delete
    void remove(InputTitle inputTitle);

    @Query("DELETE FROM input_title")
    void removeAll();

    @Update
    void modify(InputTitle inputTitle);

    @Query("SELECT * FROM input_title")
    List<InputTitle> getAll();

    @Query("SELECT * FROM input_title ORDER BY currenttime DESC LIMIT 0, 10")
    List<InputTitle> getFront();

    @Query("SELECT * FROM input_title WHERE fileName = :name")
    InputTitle get(String name);
}
