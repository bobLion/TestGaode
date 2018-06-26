package com.bob.android.testgaode;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.offlinemap.OfflineMapActivity;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.district.DistrictItem;
import com.amap.api.services.district.DistrictResult;
import com.amap.api.services.district.DistrictSearch;
import com.amap.api.services.district.DistrictSearchQuery;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationSource,
        AMapLocationListener,DistrictSearch.OnDistrictSearchListener{

    
    private  MapView mapView;
    private OnLocationChangedListener mListener;
    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private double latitude;
    private double longitude;
    private  AMap aMap;
    private boolean isFirstLoc = true;
    private UiSettings mUiSettings;
    private String localCity;
    private EditText mEdtArea;
    private DistrictSearch mDistrictSearch;
    DistrictSearchQuery mDistrictSearchQuery;
    private PolygonRunnable fromRunnable;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (18 == msg.what) {
                PolylineOptions options = (PolylineOptions) msg.obj;
                aMap.addPolyline(options);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mEdtArea = (EditText)findViewById(R.id.edt_input);
        mapView = (MapView) findViewById(R.id.map);//找到地图控件
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        setMap();
    }



    private void setMap() {
        if(null == aMap){
            aMap = mapView.getMap();//初始化地图控制器对象
            aMap.setLocationSource(this);
            mUiSettings = aMap.getUiSettings();
        }
        //设置logo位置
        mUiSettings.setLogoPosition(AMapOptions.LOGO_POSITION_BOTTOM_LEFT);//高德地图标志的摆放位置
        mUiSettings.setZoomControlsEnabled(true);//地图缩放控件是否可见
        mUiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_BUTTOM);//地图缩放控件的摆放位置
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);//地图定位标志是否可以点击
        aMap.getUiSettings().setCompassEnabled(true);//显示指南针
        aMap.getUiSettings().setScaleControlsEnabled(true);//显示比例尺
        aMap.setMyLocationEnabled(true);
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
//        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 设置卫星地图模式；
//        aMap.setMapType(AMap.MAP_TYPE_NIGHT);//夜景地图；
//        aMap.setTrafficEnabled(true);//显示实时路况图层；
       /* MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.mipmap.location_icon));
        myLocationStyle.strokeColor(R.color.material_blue);
        myLocationStyle.radiusFillColor(Color.argb(80,237,232,241));
        myLocationStyle.strokeWidth(1.0f);
        aMap.setMyLocationStyle(myLocationStyle);*/
        aMap.showIndoorMap(true);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(null != mLocationClient){
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        if(isFirstLoc){
            mListener = onLocationChangedListener;
            isFirstLoc = false;
            // 设置显示的焦点，即当前地图显示为当前位置
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(latitude,longitude),18));
        }
        if(mLocationClient == null){
            setLocation();
        }
    }

    private void setLocation() {
        // 初始化定位
        mLocationClient = new AMapLocationClient(this);
        // 设置定位回调监听
        mLocationClient.setLocationListener(this);

        // 初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(100000);

        //设置为高精度定位模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        mLocationClient.startLocation();
    }

    @Override
    public void deactivate() {
        mListener = null;
        if(null != mLocationClient){
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(null != mListener && null!= aMapLocation){
            if(null != aMapLocation && aMapLocation.getErrorCode() == 0){
                mListener.onLocationChanged(aMapLocation);//显示系统小蓝点
                latitude = aMapLocation.getLatitude();
                longitude = aMapLocation.getLongitude();
                localCity = aMapLocation.getDistrict();
                Log.e("getAccuracy", ""+aMapLocation.getAccuracy()+" 米");//获取精度信息
                Log.e("joe", "lat :-- " + latitude + " lon :--" + longitude);
                Log.e("joe", "Country : " + aMapLocation.getCountry()
                        + " province : " + aMapLocation.getProvince() + " City : "
                        + aMapLocation.getCity() + " District : "
                        + aMapLocation.getDistrict()+"Address"+aMapLocation.getAddress());
                aMap.clear();
                if(isFirstLoc){
                    // 设置显示的焦点，即当前地图显示为当前位置
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 18));
                    //aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
                    //aMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lon)));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(aMapLocation);
                    isFirstLoc=false;
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(latitude, longitude));
                markerOptions.title("当前位置\n"+aMapLocation.getCountry()+" "
                        +aMapLocation.getProvince()+" \n"+aMapLocation.getCity()
                        +" "+aMapLocation.getDistrict()
                        +" \n"+aMapLocation.getAddress());
//                markerOptions.visible(true);
                markerOptions.visible(false);
                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory
                        .fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.location_icon));
                markerOptions.icon(bitmapDescriptor);
                markerOptions.draggable(true);
                Marker marker = aMap.addMarker(markerOptions);
                marker.showInfoWindow();
            }else{
                String errorText = "定位失败！,"+aMapLocation.getErrorCode()+":"+aMapLocation.getErrorInfo();
                Log.e("AMAP_ERROR",errorText);
            }
        }
    }

    public void downLoadOfflineMap(View view){
        Intent intent = new Intent(MainActivity.this, OfflineMapActivity.class);
        startActivity(intent);
    }

    public void checkTemperature(View view){
        Intent intent = new Intent(MainActivity.this,WeatherSearchActivity.class);
        if(null != localCity && !localCity.equals("")){
            intent.putExtra("city",localCity);
        }
        startActivity(intent);
    }

    public void selectRoute(View view){
        Intent intent = new Intent(MainActivity.this,RestRouteShowActivity.class);
        intent.putExtra("latitude",latitude);
        intent.putExtra("longitude",longitude);
        startActivity(intent);
    }

    public void checkArea(View view){
        mDistrictSearch = new DistrictSearch(this);
        mDistrictSearchQuery = new DistrictSearchQuery();
        mDistrictSearchQuery.setKeywords(mEdtArea.getText().toString().trim());
        mDistrictSearchQuery.setShowBoundary(true);
        mDistrictSearch.setQuery(mDistrictSearchQuery);
        mDistrictSearch.setOnDistrictSearchListener(this);
        mDistrictSearch.searchDistrictAsyn();
    }

    @Override
    public void onDistrictSearched(DistrictResult districtResult) {
        DistrictItem districtItem = null;
        if (districtResult != null && districtResult.getDistrict() != null) {
            if (districtResult.getAMapException().getErrorCode() == AMapException.CODE_AMAP_SUCCESS) {
                ArrayList<DistrictItem> district = districtResult.getDistrict();
                if (district != null && district.size() > 0) {
                    //adcode 440106
                    //获取对应的行政区划的item
                    districtItem = district.get(0);
//                    districtItem = getDistrictItem(district, fromLocation.county.getId());
                    if (districtItem == null) {
                        return;
                    }
                    //创建划线子线程
                    fromRunnable = new PolygonRunnable(districtItem, mHandler);
                    //线程池执行
                    /*ThreadWorker.execute(fromRunnable);*/
                    fromRunnable.run();
                }
            }
        }
    }

    private class PolygonRunnable implements Runnable {
        private DistrictItem districtItem;
        private boolean isCancel = false;

        /**
         * districtBoundary()
         * 以字符串数组形式返回行政区划边界值。
         * 字符串拆分规则： 经纬度，经度和纬度之间用","分隔，坐标点之间用";"分隔。
         * 例如：116.076498,40.115153;116.076603,40.115071;116.076333,40.115257;116.076498,40.115153。
         * 字符串数组由来： 如果行政区包括的是群岛，则坐标点是各个岛屿的边界，各个岛屿之间的经纬度使用"|"分隔。
         * 一个字符串数组可包含多个封闭区域，一个字符串表示一个封闭区域
         */
        public PolygonRunnable(DistrictItem districtItem, Handler handler) {

            this.districtItem = districtItem;
            mHandler = handler;
        }

        public void cancel() {
            isCancel = true;
        }

        /**
         * Starts executing the active part of the class' code. This method is
         * called when a thread is started that has been created with a class which
         * implements {@code Runnable}.
         */
        @Override
        public void run() {
            if (!isCancel) {
                try {
                    String[] boundary = districtItem.districtBoundary();
                    if (boundary != null && boundary.length > 0) {
                        for (String b : boundary) {
                            if (!b.contains("|")) {
                                String[] split = b.split(";");
                                PolylineOptions polylineOptions = new PolylineOptions();
                                boolean isFirst = true;
                                LatLng firstLatLng = null;
                                for (String s : split) {
                                    String[] ll = s.split(",");
                                    if (isFirst) {
                                        isFirst = false;
                                        firstLatLng = new LatLng(Double.parseDouble(ll[1]), Double.parseDouble(ll[0]));
                                    }
                                    polylineOptions.add(new LatLng(Double.parseDouble(ll[1]), Double.parseDouble(ll[0])));
                                }
                                if (firstLatLng != null) {
                                    polylineOptions.add(firstLatLng);
                                }
                                polylineOptions.width(10).color(Color.BLUE).setDottedLine(true);
                                Message message = mHandler.obtainMessage();
                                message.what = 18;
                                message.obj = polylineOptions;
                                mHandler.sendMessage(message);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }



}
