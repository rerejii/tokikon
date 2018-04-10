package jp.ac.kochi_tech.info.krlab;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "tokikon_db";   // データベース名
    private final static int DB_VERSION = 1;    // データベースのバージョン

    public DataBaseHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String DB_ITEM = "CREATE TABLE day_time ("
                + "id INTEGER PRIMARY KEY"
                + ",year INTEGER NOT NULL"
                + ",mouth INTEGER NOT NULL"
                + ",day INTEGER NOT NULL"
                + ",time INTEGER NOT NULL);"
                ;

//Create文を実行
        db.execSQL(DB_ITEM);
    }

    public void onReset(SQLiteDatabase db) {
        String DB_ITEM = "CREATE TABLE day_time ("
                + "id INTEGER PRIMARY KEY"
                + ",year INTEGER NOT NULL"
                + ",mouth INTEGER NOT NULL"
                + ",day INTEGER NOT NULL"
                + ",time INTEGER NOT NULL);"
                ;

//Create文を実行
        db.execSQL(DB_ITEM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
