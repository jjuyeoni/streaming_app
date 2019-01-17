package org.teckown.hello.sqliteDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChannelDBHepler extends SQLiteOpenHelper {
    private final static String DB_NAME = "channel_db";
    public final static String TABLE_NAME = "channel_table";

    public ChannelDBHepler(Context context) {
        super(context, DB_NAME, null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( _id integer primary key autoincrement,"
                + "name TEXT, img TEXT, link TEXT, platform_type TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
