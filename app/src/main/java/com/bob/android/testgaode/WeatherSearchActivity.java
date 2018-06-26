package com.bob.android.testgaode;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;

public class WeatherSearchActivity extends AppCompatActivity implements WeatherSearch.OnWeatherSearchListener{

    private TextView mTvTime,mTvWeather,mTvTemperature,mTvWind,mTvHumidity,mTvArea;
    private WeatherSearchQuery mWeatherSearchQuery;
    private WeatherSearch mWeatherSearch;
    private LocalWeatherLive mLocalWeatherLive;
    private static final int GET_WEATHER_SUCCEED = 1000;
    private String localCity;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GET_WEATHER_SUCCEED:
                    mLocalWeatherLive = (LocalWeatherLive) msg.obj;
                    mTvTime.setText(mLocalWeatherLive.getReportTime()+"发布");
                    mTvWeather.setText(mLocalWeatherLive.getWeather());
                    mTvTemperature.setText(mLocalWeatherLive.getTemperature()+"°");
                    mTvWind.setText(mLocalWeatherLive.getWindDirection()+"风     "
                            +mLocalWeatherLive.getWindPower()+"级");
                    mTvHumidity.setText("湿度         "+mLocalWeatherLive.getHumidity()+"%");
                    mTvArea.setText(localCity);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_weather_search);
        localCity = getIntent().getStringExtra("city");
        getWeatherInfo();
        mTvWeather = (TextView)findViewById(R.id.tv_weather);
        mTvTime = (TextView)findViewById(R.id.tv_report_time);
        mTvTemperature = (TextView)findViewById(R.id.tv_temperature);
        mTvWind = (TextView)findViewById(R.id.tv_wind);
        mTvHumidity = (TextView)findViewById(R.id.tv_humidity);
        mTvArea = (TextView)findViewById(R.id.tv_area);
    }

    private void getWeatherInfo() {
        mWeatherSearchQuery = new WeatherSearchQuery(localCity, WeatherSearchQuery.WEATHER_TYPE_LIVE);
        mWeatherSearch=new WeatherSearch(this);
        mWeatherSearch.setOnWeatherSearchListener(this);
        mWeatherSearch.setQuery(mWeatherSearchQuery);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mWeatherSearch.searchWeatherAsyn(); //异步搜索
            }
        }).start();

    }
    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int resultCode) {
        if (resultCode == 1000) {
            if (localWeatherLiveResult != null&&localWeatherLiveResult.getLiveResult() != null) {
                mLocalWeatherLive = localWeatherLiveResult.getLiveResult();
                Message msg = mHandler.obtainMessage();
                msg.what = GET_WEATHER_SUCCEED;
                msg.obj = mLocalWeatherLive;
                mHandler.sendMessage(msg);

            }else {
//                ToastUtil.show(WeatherSearchActivity.this, R.string.no_result);
            }
        }else {
//            ToastUtil.showerror(WeatherSearchActivity.this, rCode);
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

    }
}
