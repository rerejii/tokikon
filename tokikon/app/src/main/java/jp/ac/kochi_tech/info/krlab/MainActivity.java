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

public class MainActivity extends AppCompatActivity  {

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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (InitialOperation == true){
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        setContentView(R.layout.activity_main);
                        mTextMessage = (TextView) findViewById(R.id.message);
                        mTextMessage.setText(R.string.title_home);
                        navigation = (BottomNavigationView) findViewById(R.id.navigation);
                        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                        InitialOperation = false;
                        navigation.setSelectedItemId(R.id.navigation_home);
                        InitialOperation = true;
                        return true;
                    case R.id.navigation_dashboard:
                        setContentView(R.layout.activity_day);
                        mTextMessage = (TextView) findViewById(R.id.message2);
                        mTextMessage.setText(R.string.title_dashboard);
                        navigation = (BottomNavigationView) findViewById(R.id.navigation2);
                        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                        InitialOperation = false;
                        navigation.setSelectedItemId(R.id.navigation_dashboard);
                        InitialOperation = true;
                        return true;
                    case R.id.navigation_notifications:
                        mTextMessage.setText(R.string.title_notifications);
                        return true;
                }
                return false;
            }
            return true;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // "DataStore"という名前でインスタンスを生成
        dataStore = getSharedPreferences("DataStore", MODE_PRIVATE);

        InitialOperation = true;
        buttonInput = true;
        mTextMessage = (TextView) findViewById(R.id.message);
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //初期値
        timerText = (TextView) findViewById(R.id.now_time);
        totalText = (TextView) findViewById(R.id.total_time);
        timerText.setText(dataFormat.format(0));
        sendButton = findViewById(R.id.fight_button);
        //作ったクラスのインスタンスを作成して
        dbHelper = new DataBaseHelper(this);
        db =  dbHelper.getWritableDatabase();
        //書き込み可能でデータベースを開く
        //(ここでDBファイルがなかったら作成する(onCreate)
        dbHelper.getWritableDatabase();

        updateTime = new Runnable() {
            public void run() {
                //TextView text = (TextView) findViewById(R.id.now_time);
                timeTextCountUp(startTime);
                mHandler.removeCallbacks(updateTime);
                //mHandler.post(updateTime);
                mHandler.postDelayed(updateTime, 1000);
            }
        };

        checkDay = new Runnable() {
            public void run() {
                Calendar cal = Calendar.getInstance();
                int year = dataStore.getInt("year", 0);
                int mouth = dataStore.getInt("mouth", 0);
                int day = dataStore.getInt("day", 0);
                if ( day == cal.get(Calendar.DAY_OF_MONTH)
                        && mouth == cal.get(Calendar.MONTH)
                        && year == cal.get(Calendar.YEAR)){
                    //日が変わった時の動作をここに
                    long ToDayTime = dataStore.getLong("ToDayTime", 0);
                    if (!buttonInput) {
                        long endTime = System.currentTimeMillis();
                        // カウント時間 = 経過時間 - 開始時間
                        ToDayTime += (endTime - startTime);
                        startTime = endTime;
                    }
                    //入力するデータ生成
                    ContentValues values = new ContentValues();
                    values.put("day", day);
                    values.put("mouth", mouth);
                    values.put("year", year);
                    values.put("time", ToDayTime);
                    //Insert発行
                    db.insert("DAY_DATA", null, values);
                    SharedPreferences.Editor editor = dataStore.edit();
                    editor.putLong("ToDayTime", ToDayTime);
                    editor.putLong("year", year);
                    editor.putLong("mouth", mouth);
                    editor.putLong("day", day);
                    editor.apply();


                    mHandler.removeCallbacks(checkDay);
                    //mHandler.post(updateTime);
                    mHandler.postDelayed(checkDay, 1000);

                }
            }
        };

        startTime = dataStore.getLong("startTime", 0);
        if(startTime != 0){
            mHandler.post(updateTime);
            //mHandler.postDelayed(updateTime, 1000);
            sendButton.setBackground(getResources().getDrawable(R.drawable.try_now_button));
            buttonInput = false;
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonInput) {
                    startTime = System.currentTimeMillis();
                    // 入力文字列を"input"に書き込む
                    SharedPreferences.Editor editor = dataStore.edit();
                    editor.putLong("startTime", startTime);
                    editor.apply();
                    mHandler.postDelayed(updateTime, 1000);
                    sendButton.setBackground(getResources().getDrawable(R.drawable.try_now_button));
                    buttonInput = false;
                }else{
                    mHandler.removeCallbacks(updateTime);
                    timerText.setText(dataFormat.format(0));
                    SharedPreferences.Editor editor = dataStore.edit();
                    long endTime = System.currentTimeMillis();
                    // カウント時間 = 経過時間 - 開始時間
                    long diffTime = (endTime - startTime);
                    long ToDayTime = dataStore.getLong("ToDayTime", 0);
                    editor.putLong("ToDayTime", ToDayTime + diffTime);
                    editor.remove("startTime");
                    editor.apply();
                    sendButton.setBackground(getResources().getDrawable(R.drawable.try_button));
                    buttonInput = true;
                }
            }
        });
    }

    public void timeTextCountUp(long sTime){
        long endTime = System.currentTimeMillis();
        // カウント時間 = 経過時間 - 開始時間
        long diffTime = (endTime - sTime);
        long ToDayTime = dataStore.getLong("ToDayTime", 0);
        timerText.setText(dataFormat.format(diffTime));
        totalText.setText(dataFormat.format(diffTime+ToDayTime));
    }

}
