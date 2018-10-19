package com.kmnfsw.work.repair.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.xutils.x;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Poi;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.kmnfsw.work.backstage.entity.SendLocalhostEntity;
import com.kmnfsw.work.backstage.mqtt.SendLocalhostMqtt;
import com.kmnfsw.work.question.entity.QuestionEntity;
import com.kmnfsw.work.repair.entity.AppointDetailsEntity;
import com.kmnfsw.work.repair.entity.AppointEntity;
import com.kmnfsw.work.repair.entity.AppointReportEntity;
import com.kmnfsw.work.repair.rabbitmq.ApplyDataPushMQ;
import com.kmnfsw.work.repair.service.AppointService.MBinder;
import com.kmnfsw.work.util.ProperTies;
import com.kmnfsw.work.util.ReceiveJson;
import com.kmnfsw.work.util.spring.RequestFactory;
import com.kmnfsw.work.welcomeLogin.utils.MacUtill;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

/**
 * 任务详情service
 *
 * @author YanFaBu
 */
public class AppointDetailsService extends Service {

    private static final String Tag = ".repair.service.AppointDetailsService";

    private String SERVICE_IP;
    private SharedPreferences shares;
    private AppointDetailsCallBack appointDetailsCallBack;

    // 声明AMapLocationClient类对象
    private AMapLocationClient mLocationClient;
    // 声明AMapLocationClientOption对象
    private AMapLocationClientOption mLocationOption;

    private LocationCallBack locationCallBack;
    private LoadPicCallBack loadPicCallBack;
    private LoadVoiceCallBack loadVoiceCallBack;
    private AppointReceiveBack appointReceiveBack;
    private AppointApplyBack appointApplyBack;

    @Override
    public void onCreate() {
        super.onCreate();
        shares = getSharedPreferences("config", Context.MODE_PRIVATE);
        Properties proper = ProperTies.getProperties(getApplicationContext());
        SERVICE_IP = proper.getProperty("serverIp");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MBinder();
    }

    public class MBinder extends Binder {
        //任务详情
        public void setAppointDetailsCallBack(AppointDetailsCallBack back) {
            appointDetailsCallBack = back;
        }

        public void getAppointDetails(String checkrepairno) {
            threadAppointDetails(checkrepairno);
        }


        //领取维修任务
        public void setAppointReceiveBack(AppointReceiveBack back) {
            appointReceiveBack = back;
        }

        public void receiveAppoint(String checkrepairno, String peopleno, String subscribedate) {
            threadReceiveAppoint(checkrepairno, peopleno, subscribedate);
        }


        //定位
        public void setLocationCallBack(LocationCallBack back) {
            locationCallBack = back;
        }

        public void getLocation() {
            new Thread() {
                @Override
                public void run() {
                    executeLocation();
                }

                ;
            }.start();
        }


        //后端下载语音
        public void setLoadVoiceCallBack(LoadVoiceCallBack back) {
            loadVoiceCallBack = back;
        }

        public void getVoice(String servicePath) {
            threadVoice(servicePath);
        }


        //后端下载图片
        public void setLoadPicCallBack(LoadPicCallBack back) {
            loadPicCallBack = back;
        }

        public void getPic(String servicePath) {
            threadPic(servicePath);
        }


        //维修任务上传数据
        public void setAppointApplyBack(AppointApplyBack back) {
            appointApplyBack = back;
        }

        public void AppointApply(AppointReportEntity appointReportEntity) {
            threadAppointApply(appointReportEntity);
        }

    }

    private void threadAppointApply(final AppointReportEntity appointReportEntity) {
        new Thread() {
            @Override
            public void run() {
                netAppointApply(appointReportEntity);
            }

            ;
        }.start();
    }

    /**
     * 维修任务申请验收
     */
    private void netAppointApply(AppointReportEntity appointReportEntity) {
        try {
            // The connection URL
            String url = SERVICE_IP + "/acceptanceTask";
            //String url= "http://192.168.0.36:8061/acceptanceTask";
            // Set the Accept header
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setContentType(new MediaType("application", "json"));
            HttpEntity<AppointReportEntity> requestEntity = new HttpEntity<AppointReportEntity>(appointReportEntity, requestHeaders);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            RequestFactory.configTimeoutByReflect(restTemplate, -1, 2 * 1000);//请求超时设置
            ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url,
                    HttpMethod.POST, requestEntity, ReceiveJson.class);

            if (responseEntity.getStatusCode().value() == 200) {//判断状态码是否正常
                ReceiveJson receiveJson = responseEntity.getBody();
                //Log.i(Tag+".ReceiveJson", ""+receiveJson);

                if (receiveJson.getState() == 200) {//效验成功
                    appointApplyBack.AppointApplySuccess("发送申请成功！");

                    //向rabbitmq推送维修任务编号
//					ApplyDataPushMQ.basicPublish(appointReportEntity.checkrepairno, getApplicationContext());
//					ApplyDataPushMQ.basicPublish("{\"checkrepairno\":"+appointReportEntity.checkrepairno+"}", getApplicationContext());

                    SendLocalhostEntity sendLocalhostEntity = new SendLocalhostEntity();
                    sendLocalhostEntity.setMac(MacUtill.getMac(getApplicationContext()));
                    new SendLocalhostMqtt().sendMagge(sendLocalhostEntity,getApplicationContext());

                } else {//效验失败
                    String msg = receiveJson.getMsg();
                    appointApplyBack.AppointApplyFail(msg);
                }

            } else if (responseEntity.getStatusCode().value() == 500) {
                appointApplyBack.AppointApplyFail("服务器后端报错500");
            }
        } catch (HttpClientErrorException e) {
            appointApplyBack.AppointApplyFail("401非法响应");
            Log.e(Tag + ".Exception", "" + e);
        } catch (ResourceAccessException e) {
            appointApplyBack.AppointApplyFail("网络异常！");
            Log.e(Tag + ".Exception", "" + e);
//		}catch (TimeoutException e) {
//			appointApplyBack.AppointApplyFail("消息中间件连接超时！");
//			Log.e(Tag+".Exception", ""+e);
        } catch (Exception e) {
            appointApplyBack.AppointApplyFail("未知程序异常！");
            Log.e(Tag + ".Exception", "" + e);
        }
    }


    private void threadVoice(final String servicePath) {
        new Thread() {
            @Override
            public void run() {
                netGetVoice(servicePath);
            }
        }.start();
    }

    /**
     * 网络加载语音
     */
    private void netGetVoice(String servicePath) {

        RequestParams params = new RequestParams(servicePath);
        //自定义保存路径
        params.setSaveFilePath(getDiskCacheDir("voice") + File.separator + "appointDetails_receive_raw.mp4");//question_report_raw.mp4
        //是否自动为文件命名
        params.setAutoRename(false);
        params.setConnectTimeout(2000);
        x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
                Log.i(Tag, "语音本地路径：" + result.toString());
                loadVoiceCallBack.getVoiceData(result.toString());
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                loadVoiceCallBack.getVoiceFail("加载语音失败！");
                Log.e(Tag, "语音" + ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }

            //网络请求之前回调
            @Override
            public void onWaiting() {
            }

            //网络请求开始的时候回调
            @Override
            public void onStarted() {
            }

            //下载的时候不断回调的方法
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                //当前进度和文件总大小
                //Log.i("JAVA","current："+ current +"，total："+total);
            }
        });
    }

    private void threadPic(final String servicePath) {
        new Thread() {
            @Override
            public void run() {
                netGetPic(servicePath);
            }
        }.start();
    }

    /**
     * 网络加载图片
     */
    private void netGetPic(String servicePath) {
        RequestParams params = new RequestParams(servicePath);
        //自定义保存路径
        params.setSaveFilePath(getDiskCacheDir("photo") + File.separator + "appointDetails_receive_img.jpg");//question_report_img.jpg
        //是否自动为文件命名
        params.setAutoRename(false);
        params.setConnectTimeout(2000);//设置连接超时两秒
        x.http().get(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
                Log.i(Tag, "图片本地路径：" + result.toString());
                loadPicCallBack.getPicData(result.toString());
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                loadPicCallBack.getPicFail("加载图片失败！");
                Log.e(Tag, "图片" + ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {
            }

            @Override
            public void onFinished() {
            }

            //网络请求之前回调
            @Override
            public void onWaiting() {
            }

            //网络请求开始的时候回调
            @Override
            public void onStarted() {
            }

            //下载的时候不断回调的方法
            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                //当前进度和文件总大小
                //Log.i("JAVA","current："+ current +"，total："+total);
            }
        });
    }

    private void threadReceiveAppoint(final String checkrepairno, final String peopleno, final String subscribedate) {
        new Thread() {
            @Override
            public void run() {
                netReceiveAppoint(checkrepairno, peopleno, subscribedate);
            }
        }.start();
    }

    /**
     * 接受维修任务
     */
    private void netReceiveAppoint(String checkrepairno, String peopleno, String subscribedate) {
        try {
            // The connection URL
            String url = SERVICE_IP + "/getRepairTask?checkrepairno={1}&peopleno={2}&subscribedate={3}";
            //String url = "http://192.168.0.36:8061/getRepairTask?checkrepairno={1}&peopleno={2}&subscribedate={3}";
            // Set the Accept header
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
            HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);
            //Log.i(Tag, "领取时间"+subscribedate);
            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            RequestFactory.configTimeoutByReflect(restTemplate, -1, 2 * 1000);//设置请求超时时间
            ResponseEntity<ReceiveJson> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
                    ReceiveJson.class, checkrepairno, peopleno, subscribedate);

            if (responseEntity.getStatusCode().value() == 200) {// 判断状态码是否正常
                ReceiveJson rt = responseEntity.getBody();
                if (rt.state == 200) {
                    appointReceiveBack.appointReceiveSuccess("接受维修任务成功！");
                } else {
                    appointReceiveBack.appointReceiveFail(rt.msg);
                }

            } else if (responseEntity.getStatusCode().value() == 500) {
                appointReceiveBack.appointReceiveFail("服务器后端报错500");
            }
        } catch (HttpClientErrorException e) {
            appointReceiveBack.appointReceiveFail("401非法响应");
            Log.e(Tag + ".Exception", "" + e);
        } catch (ResourceAccessException e) {
            appointReceiveBack.appointReceiveFail("网络异常！");
            Log.e(Tag + ".Exception", "" + e);
        } catch (Exception e) {
            appointReceiveBack.appointReceiveFail("未知程序异常！");
            Log.e(Tag + ".Exception", "" + e);
        }
    }

    private void threadAppointDetails(final String checkrepairno) {
        new Thread() {
            @Override
            public void run() {
                netAppointDetails(checkrepairno);
            }

            ;
        }.start();
    }

    /**
     * 网络获取莫任务详情
     */
    private void netAppointDetails(String checkrepairno) {
        try {
            // The connection URL
            String url = SERVICE_IP + "/ShowTask?taskNo={1}";
            //String url = "http://192.168.0.36:8061/ShowTask?taskNo={1}";
            // Set the Accept header
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.setAccept(Collections.singletonList(new MediaType("application", "json")));
            HttpEntity<?> requestEntity = new HttpEntity<Object>(requestHeaders);

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            RequestFactory.configTimeoutByReflect(restTemplate, -1, 2 * 1000);//设置请求超时时间
            ResponseEntity<AppointDetailsEntity> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,
                    AppointDetailsEntity.class, checkrepairno);
            //Log.i(Tag, "某任务详情："+responseEntity);
            if (responseEntity.getStatusCode().value() == 200) {// 判断状态码是否正常
                AppointDetailsEntity appointDetails = responseEntity.getBody();
                appointDetailsCallBack.AppointDetailsData(appointDetails);

            } else if (responseEntity.getStatusCode().value() == 500) {
                appointDetailsCallBack.AppointDetailsFail("服务器后端报错500");
            }
        } catch (HttpClientErrorException e) {
            appointDetailsCallBack.AppointDetailsFail("401非法响应");
            Log.e(Tag + ".Exception", "" + e);
        } catch (ResourceAccessException e) {
            appointDetailsCallBack.AppointDetailsFail("网络异常！");
            Log.e(Tag + ".Exception", "" + e);
        } catch (Exception e) {
            appointDetailsCallBack.AppointDetailsFail("未知程序异常！");
            Log.e(Tag + ".Exception", "" + e);
        }
    }

    /**
     * 执行定位
     */
    private void executeLocation() {
        // 初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        // 设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        // 初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        // 获取一次定位结果：
        // 该方法默认为false。0
        mLocationOption.setOnceLocation(true);
        mLocationOption.setOnceLocationLatest(true);
        // 关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);

        // 先停止定位
        mLocationClient.stopLocation();
        // 启动定位
        mLocationClient.startLocation();
    }

    private AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation amapLocation1) {
            if (amapLocation1 == null) {
                Log.e(Tag, "amapLocation is null!");
                locationCallBack.getLocationFail("点位失败！请检查网络、GPS是否打开");
                return;
            }
            if (amapLocation1.getErrorCode() != 0) {
                Log.e(Tag, "location Error, ErrCode:" + amapLocation1.getErrorCode() + ", errInfo:"
                        + amapLocation1.getErrorInfo());
                locationCallBack.getLocationFail("点位失败！请检查网络、GPS是否打开");
                return;
            }

            String exlocation = amapLocation1.getAddress();
            double exceptionlong = amapLocation1.getLongitude();
            double exceptionlat = amapLocation1.getLatitude();
            //Log.i(Tag, "exlocation；"+exlocation+"exceptionlong："+exceptionlong+"exceptionlat："+exceptionlat);
            Poi start = new Poi(exlocation, new LatLng(exceptionlat, exceptionlong), "");
            locationCallBack.getLocationData(start);

        }
    };

    public abstract interface AppointDetailsCallBack {
        public void AppointDetailsData(AppointDetailsEntity appointDetails);

        public void AppointDetailsFail(String msgEx);
    }

    /**
     * 定位
     */
    public abstract interface LocationCallBack {
        public void getLocationData(Poi start);

        public void getLocationFail(String msg);
    }

    public abstract interface LoadVoiceCallBack {
        public void getVoiceData(String voicePath);

        public void getVoiceFail(String msgEx);

    }

    public abstract interface LoadPicCallBack {
        public void getPicData(String picPath);

        public void getPicFail(String msgEx);
    }

    public abstract interface AppointReceiveBack {
        public void appointReceiveSuccess(String massge);

        public void appointReceiveFail(String msgEx);
    }

    public abstract interface AppointApplyBack {
        public void AppointApplySuccess(String massege);

        public void AppointApplyFail(String msgEx);
    }

    /**
     * 应用磁盘路径
     */
    private String getDiskCacheDir(String folder) {
        String cachepath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachepath = getApplicationContext().getExternalCacheDir().getPath() + File.separator + folder;
        } else {
            cachepath = getApplicationContext().getCacheDir().getPath() + File.separator + folder;
        }
        File fileDir = new File(cachepath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        return cachepath;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
        Log.e(Tag, "销毁！");
    }

}
