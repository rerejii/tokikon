package jp.ac.kochi_tech.info.krlab;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class BreakActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_break);

        //広告
        MobileAds.initialize(getApplicationContext(),"ca-app-pub-3940256099942544~3347511713");
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("9C08F511374C27EC7F49B26DE7B4B741")
                .build();
        mAdView.loadAd(adRequest);
    }
}
