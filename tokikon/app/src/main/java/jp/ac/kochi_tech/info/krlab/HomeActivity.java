package jp.ac.kochi_tech.info.krlab;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.SimpleDateFormat;

import com.github.mikephil.charting.charts.LineChart;

import java.util.Calendar;
import java.util.Locale;

@SuppressLint("Registered")
public class HomeActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView timerText;
    private TextView totalText;
    private BottomNavigationView navigation;
    private boolean InitialOperation;
    private LineChart mChart;
    private long startTime;
    private boolean buttonInput;
    private Button sendButton;
    private SharedPreferences dataStore;
    private DataBaseHelper dbHelper;
    private SQLiteDatabase db;
    /** スレッドUI操作用ハンドラ */
    private Handler mHandler = new Handler();
    /** テキストオブジェクト */
    private Runnable updateTime;
    private Runnable checkDay;
    private SimpleDateFormat dataFormat =
            new SimpleDateFormat("mm:ss", Locale.JAPAN);

    public void setView() {
        setContentView(R.layout.activity_main);
    }


}
