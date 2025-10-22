package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySqliteOpenHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_USER_TABLE =
            "CREATE TABLE IF NOT EXISTS user (name TEXT PRIMARY KEY, password TEXT)";

    public MySqliteOpenHelper(Context context) {
        super(context, "user", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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