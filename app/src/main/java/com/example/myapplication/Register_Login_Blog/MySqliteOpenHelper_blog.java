package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySqliteOpenHelper_blog extends SQLiteOpenHelper {

    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS blog2 (name TEXT, text TEXT,image Int,id Int primary key)";

    public MySqliteOpenHelper_blog(Context context) {
        super(context, "blog2", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 执行建表 SQL
        db.execSQL(SQL_CREATE_USER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: 处理升级逻辑（比如删除旧表、创建新表、迁移数据等）
        // 示例：删除旧表（谨慎使用，会丢失数据）
        // db.execSQL("DROP TABLE IF EXISTS user");
        // 重新创建表
        // onCreate(db);
    }
}