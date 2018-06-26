package com.bob.android.testgaode;

import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.Polyline;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapCarInfo;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapRestrictionInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.autonavi.tbt.TrafficFacilityInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RestRouteShowActivity extends AppCompatActivity implements AMapNaviListener,
        View.OnClickListener,AMap.OnPolylineClickListener,TextWatcher,PoiSearch.OnPoiSearchListener{

    // 声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    // 定位参数配置
    private AMapLocationClientOption mLocationOption;
    //纬度
    private double Latitude;
    //经度
    private double Longitude;
    //查询实例
    private PoiSearch.Query query;
    // 结果列表
    private ListView result_listview;
    // 开始搜索
    private Button search_btn;
    // 输入要查找的内容
    private EditText input;

    /**
     * to_receive_address
     */
    private TextView mTvTitle;
    /**
     * 收货地
     */
    private EditText mTvFrom;
    /**
     * 目的地
     */
    private EditText mTvTo;
    private ImageView mIvCall;
    private LinearLayout mLinlist;

    /**
     * 导航对象（单利）
     */
    private AMapNavi mAMapNavi;
    private AMap mAMap;

    /**
     * 地图对象
     */
    private MapView mRouteMapView;
    private Marker mStartMarker;
    private Marker mEndMarker;
    private NaviLatLng endLatlng ;
    private NaviLatLng startLatLng;
    private List<NaviLatLng> startList = new ArrayList<>();
    //途经点坐标集合
    private List<NaviLatLng> wayList = new ArrayList<>();
    // 终点坐标集合
    private List<NaviLatLng> endList = new ArrayList<>();

    // 保存当前算好的路线
    private SparseArray<RouteOverLay> routeOverLays = new SparseArray<>();

    // 当前用户选好的路线，在下一个页面进行导航
    private int routeIndex;
    // 路线的权值，重合路线情况下，权值高的路线会覆盖权值低的路径
    private int zIndex = 1;

    // 路径计算成功标志位
    private boolean calculateSuccess = false;
    private boolean chooserRouteSuccess = false;
    private LatLngBounds.Builder boundBuilder = LatLngBounds.builder();
    private RelativeLayout mMain;
    private double startLatitude;
    private double startLongitude;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_rest_route_show);
        getSupportActionBar().hide();
        initData();
        initView(savedInstanceState);
    }

    private void initData() {
        startLatitude = getIntent().getDoubleExtra("latitude",0);
        startLongitude = getIntent().getDoubleExtra("longitude",0);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initView(Bundle savedInstanceState) {
        mRouteMapView = (MapView) findViewById(R.id.map);
        mRouteMapView.onCreate(savedInstanceState);
        mAMap = mRouteMapView.getMap();
        mAMap.getUiSettings().setZoomControlsEnabled(false);
        mAMap.setOnPolylineClickListener(this);
        mAMap.setMapType(AMap.MAP_TYPE_NAVI);
        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        if(null != mAMapNavi){
            mAMapNavi.addAMapNaviListener(this);
        }

        result_listview = (ListView)findViewById(R.id.list_view);
        mLinlist = (LinearLayout)findViewById(R.id.lin_list);
//        mLinlist.getBackground().setAlpha(80);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvFrom = (EditText) findViewById(R.id.tv_from);
        mTvTo = (EditText) findViewById(R.id.tv_to);
        mIvCall = (ImageView) findViewById(R.id.iv_call);
        mIvCall.setOnClickListener(this);
        mMain = (RelativeLayout) findViewById(R.id.main);
        mMain.setVisibility(View.VISIBLE);
        mTvFrom.addTextChangedListener(this);
        loadData();
    }

    private void initLocation() {
        // 初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        // 设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        // 初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        // 设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        // 设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        // 设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();

    }

    private void loadData() {
        startLatLng = new NaviLatLng(startLatitude,startLongitude);
        startList.clear();
        startList.add(startLatLng);
        endLatlng = new NaviLatLng(31.223923,121.557671);
        endList.clear();
        endList.add(endLatlng);
        calculateRoute();

        boundBuilder.include(new LatLng(31.227505,121.631858));
        mRouteMapView.post(new Runnable() {
            @Override
            public void run() {
                mAMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(boundBuilder.build(),14));
            }
        });
    }

    // 声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    Latitude=amapLocation.getLatitude();//获取纬度
                    Longitude=amapLocation.getLongitude();//获取经度

                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError","location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };

    private void search() {
        String content= mTvFrom.getText().toString().trim();
        if(content==null){
            Toast.makeText(RestRouteShowActivity.this, "输入为空", Toast.LENGTH_LONG).show();
        }else{
//            query = new PoiSearch.Query(content, "汽车维修|修车服务", "");
            query = new PoiSearch.Query(content, "住宿服务|风景名胜", "");
            // keyWord表示搜索字符串，第二个参数表示POI搜索类型，默认为：生活服务、餐饮服务、商务住宅
            // 共分为以下20种：汽车服务|汽车销售|
            // 汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|
            // 住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|
            // 金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
            // cityCode表示POI搜索区域，（这里可以传空字符串，空字符串代表全国在全国范围内进行搜索）
            query.setPageSize(10);// 设置每页最多返回多少条poiitem
            query.setPageNum(1);// 设置查第一页
            PoiSearch poiSearch = new PoiSearch(this, query);
            //如果不为空值
            if(startLatitude!=0.0&&startLongitude!=0.0){
                poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(startLatitude,
                        startLongitude), 6000));// 设置周边搜索的中心点以及区域
                poiSearch.setOnPoiSearchListener(this);// 设置数据返回的监听器
                poiSearch.searchPOIAsyn();// 开始搜索
            }else{
                Toast.makeText(RestRouteShowActivity.this, "定位失败", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void calculateRoute() {
        clearRoute();
        int strategyFlag = 0;
        try {
            strategyFlag = mAMapNavi.strategyConvert(true,true,true,false,true);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(strategyFlag >= 0){
            AMapCarInfo carInfo = new AMapCarInfo();
            // 设置车牌
//            carInfo.setCarNumber(carNumber);
            //设置车牌是否参与限行算路
//            carInfo.setRestriction(true);
            mAMapNavi.setCarInfo(carInfo);
            mAMapNavi.calculateDriveRoute(startList,endList,wayList,strategyFlag);
        }
    }

    /**
     * 清除当前地图上计算好的路线
     */
    private void clearRoute() {
        for(int i = 0;i<routeOverLays.size();i++){
            RouteOverLay routeOverLay = routeOverLays.valueAt(i);
            routeOverLay.setTrafficLine(true);
            routeOverLay.removeFromMap();
        }
        routeOverLays.clear();
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        if(null != routeOverLays && routeOverLays.size() == 1){
            return;
        }
        List<LatLng> latLngs = polyline.getPoints();
        if(latLngs.size() == 0){
            return;
        }

        LatLng latLng = latLngs.get(0);
        outer:
        for(int i = 0;i<routeOverLays.size();i++){
            int key = routeOverLays.keyAt(i);
            List<NaviLatLng> naviLatLngs = routeOverLays.get(key).getAMapNaviPath().getCoordList();
            for (NaviLatLng naviLatLng : naviLatLngs) {
                if(Math.abs((naviLatLng.getLatitude() - latLng.latitude))<=0.000001
                        && Math.abs((naviLatLng.getLongitude() - latLng.longitude)) <= 0.000001){
                    if(i == routeIndex){
                        continue outer;
                    }
                    for(int j = 0; j<routeOverLays.size();j++){
                        if(i == j){
                            continue ;
                        }
                        int key2 = routeOverLays.keyAt(j);
                        routeOverLays.get(key2).setTransparency(0.4f);
                        routeOverLays.get(key2).setZindex(0);
                    }
                    routeOverLays.get(key).setTransparency(1.0f);

                    routeOverLays.get(key).setZindex(1);
                    mAMapNavi.selectRouteId(key);
                    routeIndex = i;
                    chooserRouteSuccess = true;
                    return;
                }
            }
        }
    }

    @Override
    public void onCalculateRouteFailure(int i) {
        calculateSuccess = false;
        Toast.makeText(RestRouteShowActivity.this,"路径计算失败",Toast.LENGTH_LONG).show();
    }



    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        // 清空上次计算的路径列表
        routeOverLays.clear();
        HashMap<Integer,AMapNaviPath> paths = mAMapNavi.getNaviPaths();
        for (int i = 0; i < ints.length; i++) {
            AMapNaviPath path = paths.get(ints[i]);
            if(null != path){
                drawRoutes(ints[i],path);
            }
            changeRoute();
            mMain.setVisibility(View.VISIBLE);
        }
    }

    private void changeRoute() {
        if(!calculateSuccess){
            Toast.makeText(RestRouteShowActivity.this,"请先计算路程",Toast.LENGTH_LONG).show();
            return;
        }
        // 计算出来的路径只有一条
        if(routeOverLays.size() == 1){
            chooserRouteSuccess = true;
            mAMapNavi.selectRouteId(routeOverLays.keyAt(0));
            return;
        }

        if(routeIndex >= routeOverLays.size()){
            routeIndex = 0;
            int routeId = routeOverLays.keyAt(routeIndex);
            for(int i = 0; i<routeOverLays.size();i++){
                int key = routeOverLays.keyAt(i);
                routeOverLays.get(key).setTransparency(0.4f);
                routeOverLays.get(key).setZindex(0);
            }
            routeOverLays.get(routeId).setTransparency(1);
            // 把选中的路径的权值变高，使其路线高亮的同时重合路径不会变的透明
            routeOverLays.get(routeId).setZindex(1);

            mAMapNavi.selectRouteId(routeId);
            routeIndex ++;
            chooserRouteSuccess = true;

            // 选完之后确定是否限行路段
            AMapRestrictionInfo info = mAMapNavi.getNaviPath().getRestrictionInfo();
            if(!TextUtils.isEmpty(info.getRestrictionTitle())){
                if(routeIndex == 0){
                    return;
                }
                changeRoute();
            }
        }
    }

    private void drawRoutes(int routeId, AMapNaviPath path) {
        calculateSuccess = true;
        RouteOverLay routeOverLay = new RouteOverLay(mAMap,path,this);
        routeOverLay.setTrafficLine(true);
        routeOverLay.setStartPointBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.icon_start));
        routeOverLay.setEndPointBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.icon_end));
        routeOverLay.addToMap();
        routeOverLay.zoomToSpan(120);
        routeOverLays.put(routeId,routeOverLay);
    }



    @Override
    protected void onResume() {
        super.onResume();
        mRouteMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRouteMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mRouteMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startList.clear();
        wayList.clear();
        endList.clear();
        routeOverLays.clear();
        mRouteMapView.onDestroy();

        mAMapNavi.removeAMapNaviListener(this);
        mAMapNavi.destroy();
        mLocationClient.stopLocation();//停止定位
    }

    @Override
    public void onPoiSearched(final PoiResult poiResult, int code) {
        System.out.println("Result" + poiResult.getPois().get(0).getLatLonPoint());
        System.out.println("Code" + code);
        if(null != poiResult){
            mLinlist.setVisibility(View.VISIBLE);
            result_listview.setVisibility(View.VISIBLE);
            MyAdapter mAdapter=new MyAdapter(RestRouteShowActivity.this,poiResult.getPois());
            result_listview.setAdapter(mAdapter);
            result_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    PoiItem poiItem = poiResult.getPois().get(position);
                    mTvFrom.setText(String.valueOf(poiItem));
                    result_listview.setVisibility(View.GONE);
                    mLinlist.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        search();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }


    @Override
    public void onReCalculateRouteForYaw() {

    }

    @Override
    public void onReCalculateRouteForTrafficJam() {

    }

    @Override
    public void onArrivedWayPoint(int i) {

    }

    @Override
    public void onGpsOpenStatus(boolean b) {

    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {

    }

    @Override
    public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {

    }

    @Override
    public void hideCross() {

    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {

    }

    @Override
    public void notifyParallelRoad(int i) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

    }

    @Override
    public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

    }

    @Override
    public void onPlayRing(int i) {

    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onInitNaviFailure() {

    }

    @Override
    public void onInitNaviSuccess() {

    }

    @Override
    public void onStartNavi(int i) {

    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

    }

    @Override
    public void onGetNavigationText(int i, String s) {

    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
