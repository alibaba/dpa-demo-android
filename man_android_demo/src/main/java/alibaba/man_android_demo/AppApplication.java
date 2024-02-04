package alibaba.man_android_demo;

import android.app.Application;

import com.alibaba.sdk.android.man.MANService;
import com.alibaba.sdk.android.man.MANServiceProvider;

/**
 * Created by LK on 16/1/30.
 */
public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * 初始化Mobile Analytics服务
         */

        // 获取MAN服务
        MANService manService = MANServiceProvider.getService();

        // 打开调试日志
        manService.getMANAnalytics().turnOnDebug();

        // MAN初始化方法之一，从AndroidManifest.xml中获取appKey和appSecret初始化
        manService.getMANAnalytics().init(this, getApplicationContext());

        // MAN另一初始化方法，手动指定appKey和appSecret
        // String appKey = "******";
        // String appSecret = "******";
        // manService.getMANAnalytics().init(this, getApplicationContext(), appKey, appSecret);

        // 若需要关闭 SDK 的自动异常捕获功能可进行如下操作,详见文档5.4
//        manService.getMANAnalytics().turnOffCrashReporter();

        // 通过此接口关闭页面自动打点功能，详见文档4.2
        //manService.getMANAnalytics().turnOffAutoPageTrack();

        // 设置渠道（用以标记该app的分发渠道名称），如果不关心可以不设置即不调用该接口，渠道设置将影响控制台【渠道分析】栏目的报表展现。如果文档3.3章节更能满足您渠道配置的需求，就不要调用此方法，按照3.3进行配置即可
        manService.getMANAnalytics().setChannel("某渠道");

        // 若AndroidManifest.xml 中的 android:versionName 不能满足需求，可在此指定；
        // 若既没有设置AndroidManifest.xml 中的 android:versionName，也没有调用setAppVersion，appVersion则为null
        manService.getMANAnalytics().setAppVersion("3.1.1");
    }
}
