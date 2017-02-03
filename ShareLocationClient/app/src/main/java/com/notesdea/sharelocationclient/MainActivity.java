package com.notesdea.sharelocationclient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //位置权限的请求码
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 100;
    //需要请求的权限
    private static final String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocClient;
    private LocationListener mLocationListener = new LocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        initView();
        initData();

        String[] needPermissions = getNeedPermissions();
        if (needPermissions.length > 0) {
            ActivityCompat.requestPermissions(this, needPermissions, REQUEST_CODE_LOCATION_PERMISSION);
        } else {
            startPosition();
        }
    }

    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
    }

    private void initData() {
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(mLocationListener); //注册定位监听器
    }

    //开启定位
    private void startPosition() {
        LocationClientOption locOption = new LocationClientOption(); //配置定位等各种参数
        locOption.setOpenGps(true); //设置打开gps
        locOption.setCoorType("bd09ll"); //返回的定位结果是百度经纬度,默认值gcj02
        mLocClient.setLocOption(locOption);

        mLocClient.start(); //启动定位
    }

    //获取需要请求的权限
    private String[] getNeedPermissions() {
        List<String> neededPermissions = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            if (!isHasPermission(permission)) {
                neededPermissions.add(permission);
            }
        }
        return neededPermissions.toArray(new String[neededPermissions.size()]);
    }

    //判断是否有权限
    private boolean isHasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    //请求权限返回的结果
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION_PERMISSION:
                //权限通过时启动定位
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPosition();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    //定位监听器
    public class LocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //当不没接收到位置或地图销毁时不处理操作
            if (bdLocation == null || mMapView == null) {
                return;
            }

            //构建定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius()) //设置定位数据的精度信息
                    .direction(100) //设置定位的方向
                    .latitude(bdLocation.getLatitude()) //设置定位的纬度
                    .longitude(bdLocation.getLongitude()) //设置定位的经度
                    .build();
            mBaiduMap.setMyLocationData(locData);
            
            //获取经纬度对象
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatus.Builder builder = new MapStatus.Builder();
            //target:设置中心点， zoom: 设置地图缩放级别
            builder.target(latLng).zoom(18.0f);
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }
    }
}
