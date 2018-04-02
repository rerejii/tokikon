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
//データベース定義
        String DB_ITEM = "Create table DAY_DATA ("
                + "id integer primary key"
                + ",year integer not null"
                + ",mouth integer not null"
                + ",day integer not null"
                + ",time integer not null)";

//Create文を実行
        db.execSQL(DB_ITEM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
