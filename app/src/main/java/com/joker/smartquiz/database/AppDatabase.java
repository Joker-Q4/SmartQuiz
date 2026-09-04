package com.joker.smartquiz.database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.joker.smartquiz.database.dao.InputDataDao;
import com.joker.smartquiz.database.dao.InputTitleDao;
import com.joker.smartquiz.database.entity.InputData;
import com.joker.smartquiz.database.entity.InputTitle;
import com.joker.smartquiz.utils.Utils;

/**
 * @author Joker
 * @since 2020/08/07
 */
@Database(entities =
        {InputTitle.class, InputData.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "joker";
    private volatile static AppDatabase instance;
    public abstract InputTitleDao inputTitleDao();
    public abstract InputDataDao inputDataDao();

    AppDatabase() {
    }

    public static AppDatabase getInstance() {
        //1.第一次检测
        if (instance == null) {
            //同步
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    //1.分配对象内存空间
                    //2.初始化对象
                    //3.设置instance指向刚分配的内存地址，此时instance!=null
                    instance = Room
                            .databaseBuilder(Utils.getApp().getApplicationContext(), AppDatabase.class, DB_NAME)
                            .fallbackToDestructiveMigration(true)
                            .allowMainThreadQueries()
//                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return instance;
    }

    //升级语句：SQLite 不支持 MODIFY COLUMN，需要重建表
    @SuppressWarnings("unused")
    private final static Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // SQLite 修改列类型的标准做法：重建表
            database.execSQL("CREATE TABLE IF NOT EXISTS video_history_new (id INTEGER PRIMARY KEY, percent INTEGER)");
            database.execSQL("INSERT INTO video_history_new (id, percent) SELECT id, percent FROM video_history");
            database.execSQL("DROP TABLE video_history");
            database.execSQL("ALTER TABLE video_history_new RENAME TO video_history");
        }
    };
}
