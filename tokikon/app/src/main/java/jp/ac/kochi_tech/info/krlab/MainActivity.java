package jp.ac.kochi_tech.info.krlab;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.SimpleDateFormat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity implements OnChartGestureListener, OnChartValueSelectedListener {

    /////定数/////
    private final int DEFALT_LEFT_BREAK_TIME = 15;
    private final int DEFALT_RIGHT_BREAK_TIME = 90;
    private final long TIME_TO_ZERO = -32400000;
    
    /////共通ID/////
    private SharedPreferences dataStore;
    private DataBaseHelper dbHelper;
    private SQLiteDatabase db;
    private LineChart mChart;

    /////テキストオブジェクト/////
    private Handler mHandler = new Handler();
    private Runnable updateTime;
    private Runnable checkDay;
    private Runnable updateBreakTime;

    /////判定用フラグ/////
    private boolean InitialOperation;
    private boolean buttonInput;

    //////フォーマット/////
    private SimpleDateFormat dataFormat =
            new SimpleDateFormat("HH:mm:ss", Locale.JAPAN);
    private SimpleDateFormat dayFormat =
            new SimpleDateFormat("yyyy/MM", Locale.JAPAN);

    /////共有変数/////
    private int viewMouth;
    private int viewYear;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (InitialOperation == true){
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        setContentView(R.layout.activity_main);
                        BottomNavigationView navigationOfMain = (BottomNavigationView) findViewById(R.id.navigation);
                        navigationOfMain.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                        onCreateHome();
                        InitialOperation = false;
                        navigationOfMain.setSelectedItemId(R.id.navigation_home);
                        InitialOperation = true;
                        return true;
                    case R.id.navigation_graph:
                        setContentView(R.layout.activity_graph);
                        BottomNavigationView navigationOfGraph = (BottomNavigationView) findViewById(R.id.navigation2);
                        navigationOfGraph.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                        onCreateGraph();
                        InitialOperation = false;
                        navigationOfGraph.setSelectedItemId(R.id.navigation_graph);
                        InitialOperation = true;
                        return true;
                    case R.id.navigation_notifications:
                        InitialOperation = false;
                        //navigation.setSelectedItemId(R.id.navigation_notifications);
                        InitialOperation = true;
                }
                return false;
            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigationOfMain = (BottomNavigationView) findViewById(R.id.navigation);
        navigationOfMain.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        dataStore = getSharedPreferences("DataStore", MODE_PRIVATE);// "DataStore"という名前でインスタンスを生成
        dbHelper = new DataBaseHelper(this);
        db = dbHelper.getWritableDatabase();
        dbHelper.getWritableDatabase();
        if (dataStore.getInt("InitializationFlag", 0) == 0) {
            SharedPreferences.Editor editor = dataStore.edit();
            Calendar cal = Calendar.getInstance();
            editor.putInt("leftBreakTime", DEFALT_LEFT_BREAK_TIME);
            editor.putInt("rightBreakTime", DEFALT_RIGHT_BREAK_TIME);
            editor.putInt("year", cal.get(Calendar.YEAR));
            editor.putInt("mouth", cal.get(Calendar.MONTH));
            editor.putInt("day", cal.get(Calendar.DAY_OF_MONTH));
            editor.putLong("ToDayTime", TIME_TO_ZERO);
            editor.putInt("InitializationFlag", 1);
            editor.apply();
        }

        long breakEndTime = dataStore.getLong("breakEndTime", TIME_TO_ZERO);
        long nowTime = System.currentTimeMillis();

        if(breakEndTime > nowTime){
            //休憩時間であれば
            onCreateBreak();
        }else{
            //休憩時間外であれば
            onCreateHome();
        }
    }

    private void onCreateHome() {

        InitialOperation = true;
        buttonInput = true;

        Button leftBreakButton = findViewById(R.id.left_break_button);
        leftBreakButton.setText("  "+String.valueOf(dataStore.getInt("leftBreakTime", 0))+"分休憩");
        Button rightBreakButton = findViewById(R.id.right_break_button);
        rightBreakButton.setText(String.valueOf(dataStore.getInt("rightBreakTime", 0))+"分休憩  ");

        updateTime = new Runnable() {
            public void run() {
                long endTime = System.currentTimeMillis();
                // カウント時間 = 経過時間 - 開始時間
                long startTime = dataStore.getLong("startTime", TIME_TO_ZERO);
                long diffTime = (endTime - startTime);
                long ToDayTime = dataStore.getLong("ToDayTime", TIME_TO_ZERO);
                TextView timerText = (TextView) findViewById(R.id.now_time);
                //初期時間のズレを修正するためにTIME_TO_ZEROを足す
                timerText.setText(dataFormat.format(diffTime + TIME_TO_ZERO));
                TextView totalText = (TextView) findViewById(R.id.total_time);
                totalText.setText(dataFormat.format(diffTime+ToDayTime));
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
                    long ToDayTime = dataStore.getLong("ToDayTime", TIME_TO_ZERO);
                    if (!buttonInput) {
                        long startTime = dataStore.getLong("startTime", 0);
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
                    db.insert("day_time", null, values);
                    SharedPreferences.Editor editor = dataStore.edit();
                    editor.putLong("ToDayTime", ToDayTime);
                    editor.putLong("year", year);
                    editor.putLong("mouth", mouth);
                    editor.putLong("day", day);
                    editor.putLong("ToDayTime", TIME_TO_ZERO);
                    editor.apply();
                    mHandler.removeCallbacks(checkDay);
                    mHandler.postDelayed(checkDay, 1000);

                }
            }
        };

        long startTime = dataStore.getLong("startTime", TIME_TO_ZERO);
        if(startTime != TIME_TO_ZERO){
            mHandler.post(updateTime);
            Button fightButton = findViewById(R.id.fight_button);
            fightButton.setBackground(getResources().getDrawable(R.drawable.try_now_button));
            buttonInput = false;
        }else{
            TextView timerText = (TextView) findViewById(R.id.now_time);
            timerText.setText(dataFormat.format(TIME_TO_ZERO));
            long ToDayTime = dataStore.getLong("ToDayTime", TIME_TO_ZERO);
            TextView totalText = (TextView) findViewById(R.id.total_time);
            totalText.setText(dataFormat.format(ToDayTime));
        }

        Button fightButton = findViewById(R.id.fight_button);
        fightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonInput) {
                    onFightButton();
                }else{
                    offFightButton();
                }
            }
        });

        Button left_break_button = findViewById(R.id.left_break_button);
        left_break_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!buttonInput) {
                    offFightButton();
                }
                int breakTime = dataStore.getInt("leftBreakTime", 0);
                goToOnCreateBreak(breakTime);
                onCreateBreak();
            }
        });

        Button right_break_button = findViewById(R.id.right_break_button);
        right_break_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!buttonInput) {
                    offFightButton();
                }
                int breakTime = dataStore.getInt("rightBreakTime", 0);
                goToOnCreateBreak(breakTime);
                onCreateBreak();
            }
        });

    }

    private void goToOnCreateBreak(int breakTime) {
        long nowTime = System.currentTimeMillis();
        long breakEndTime = 60 * 1000 * breakTime + nowTime;
        SharedPreferences.Editor editor = dataStore.edit();
        editor.putLong("breakEndTime", breakEndTime);
        editor.apply();
        setContentView(R.layout.activity_break);
    }

    private void onFightButton() {
        long startTime = System.currentTimeMillis();
        // 入力文字列を"input"に書き込む
        SharedPreferences.Editor editor = dataStore.edit();
        editor.putLong("startTime", startTime);
        editor.apply();
        mHandler.postDelayed(updateTime, 1000);
        Button fightButton = findViewById(R.id.fight_button);
        fightButton.setBackground(getResources().getDrawable(R.drawable.try_now_button));
        ImageView statusView = findViewById(R.id.status_view);
        statusView.setImageResource(R.drawable.fight);
        buttonInput = false;
    }

    private void offFightButton() {
        mHandler.removeCallbacks(updateTime);
        TextView timerText = (TextView) findViewById(R.id.now_time);
        timerText.setText(dataFormat.format(TIME_TO_ZERO));
        SharedPreferences.Editor editor = dataStore.edit();
        long startTime = dataStore.getLong("startTime", TIME_TO_ZERO);
        long endTime = System.currentTimeMillis();
        // カウント時間 = 経過時間 - 開始時間
        long diffTime = (endTime - startTime);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(diffTime);
        int msec = cal.get(Calendar.MILLISECOND);
        diffTime = diffTime - msec;
        long ToDayTime = dataStore.getLong("ToDayTime", TIME_TO_ZERO);
        editor.putLong("ToDayTime", ToDayTime + diffTime);
        editor.remove("startTime");
        editor.apply();
        Button fightButton = findViewById(R.id.fight_button);
        fightButton.setBackground(getResources().getDrawable(R.drawable.try_button));
        ImageView statusView = findViewById(R.id.status_view);
        statusView.setImageResource(R.drawable.stand_by);
        buttonInput = true;
    }

    private void onCreateGraph() {
        mChart = (LineChart) findViewById(R.id.chart);
        TextView setTimeView = findViewById(R.id.setTimeView);
        long time = System.currentTimeMillis();
        setTimeView.setText(dayFormat.format(time));

        Button nextDayButton = findViewById(R.id.nextDayButton);
        Button backDayButton = findViewById(R.id.backDayButton);

        Calendar cal = Calendar.getInstance();
        viewMouth = cal.get(Calendar.MONTH);
        viewYear = cal.get(Calendar.YEAR);

        nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewMouth == 11){
                    viewMouth = 0;
                    viewYear += 1;
                }else{
                    viewMouth += 1;
                }
                Calendar cal = Calendar.getInstance();
                cal.set(viewYear, viewMouth, cal.get(Calendar.DAY_OF_MONTH));
                long time = cal.getTimeInMillis();
                TextView setTimeView = findViewById(R.id.setTimeView);
                setTimeView.setText(dayFormat.format(time));
                iniitChart();
            }
        });

        backDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewMouth == 0){
                    viewMouth = 11;
                    viewYear -= 1;
                }else{
                    viewMouth -= 1;
                }
                Calendar cal = Calendar.getInstance();
                cal.set(viewYear, viewMouth, cal.get(Calendar.DAY_OF_MONTH));
                long time = cal.getTimeInMillis();
                TextView setTimeView = findViewById(R.id.setTimeView);
                setTimeView.setText(dayFormat.format(time));
                iniitChart();
            }
        });
        iniitChart();
    }

    private void onCreateBreak() {
        setContentView(R.layout.activity_break);

        //広告
        MobileAds.initialize(getApplicationContext(),"ca-app-pub-3940256099942544~3347511713");
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("9C08F511374C27EC7F49B26DE7B4B741")
                .build();
        mAdView.loadAd(adRequest);

        updateBreakTime = new Runnable() {
            public void run() {
                long breakEndTime = dataStore.getLong("breakEndTime", TIME_TO_ZERO);
                long nowTime = System.currentTimeMillis();
                if(breakEndTime > nowTime){
                    TextView remainingTime  = findViewById(R.id.remaining_time);
                    remainingTime.setText(dataFormat.format(breakEndTime - nowTime + TIME_TO_ZERO));
                    mHandler.removeCallbacks(updateBreakTime);
                    mHandler.postDelayed(updateBreakTime, 1000);
                }
            }
        };
        mHandler.post(updateBreakTime);

        Button endBreakButton = findViewById(R.id.end_break_button);
        endBreakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = dataStore.edit();
                editor.putLong("breakEndTime", TIME_TO_ZERO);
                editor.apply();
                setContentView(R.layout.activity_main);
                BottomNavigationView navigationOfMain = (BottomNavigationView) findViewById(R.id.navigation);
                navigationOfMain.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
                onCreateHome();
            }
        });

    }

    private void iniitChart() {
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mChart.fitScreen();
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);
        mChart.setTouchEnabled(true);
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setPinchZoom(true);
        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);
        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line
        //Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.setAxisMaximum(24f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);//グリッド線を有効にする
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(true);
        mChart.getAxisRight().setEnabled(false);
        //mChart.setVisibleXRange(20f,20f);
        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);
        setData();// add data
        mChart.centerViewTo(0, 0, YAxis.AxisDependency.LEFT);
        mChart.animateX(2500);
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();
        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        // // dont forget to refresh the drawing
        mChart.zoom(2f, 2f, 0, 0);
        mChart.invalidate();
    }

    private void setData() {

        ArrayList<Entry> values = new ArrayList<Entry>();

        Calendar cal = Calendar.getInstance();
        cal.set(viewYear, viewMouth, 1);
        int maxDate = cal.getActualMaximum(Calendar.DATE);
        for (int i = 1; i <= maxDate; i++){
            Cursor c = db.query("day_time",
                    new String[] { "time" },
                    "year == ? AND mouth == ? AND day == ?",
                    new String[] { String.valueOf(viewYear), String.valueOf(viewMouth), String.valueOf(i) },
                    null,
                    null,
                    null);
            boolean mov = c.moveToFirst();
            if (mov) {
                float val = c.getFloat(0);
                values.add(new Entry(i, val, getResources().getDrawable(R.drawable.star)));
            }else{
                values.add(new Entry(i, 0, getResources().getDrawable(R.drawable.star)));
            }
        }

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "Time");

            set1.setDrawIcons(false);

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            }
            else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }
    }

    public void setTestData(){
        //getApplicationContext().deleteDatabase(dbHelper.getDatabaseName());
        //dbHelper.onReset(db);
        ContentValues values = new ContentValues();
        int mouth = 2;
        int year = 2018;
        for (int i = 1; i < 10; i += 1 ){
            values.put("day", i);
            values.put("mouth", mouth);
            values.put("year", year);
            values.put("time", i);
            //Insert発行
            db.insert("day_time", null, values);
        }

    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() + ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}
