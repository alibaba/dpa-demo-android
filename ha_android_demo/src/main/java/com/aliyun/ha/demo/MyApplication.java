package com.aliyun.ha.demo;

import android.app.Application;
import android.util.Log;

import com.alibaba.ha.adapter.AliHaAdapter;
import com.alibaba.ha.adapter.AliHaConfig;
import com.alibaba.ha.adapter.Plugin;
import com.alibaba.ha.adapter.service.tlog.TLogLevel;
import com.alibaba.ha.adapter.service.tlog.TLogService;
import com.alibaba.ha.protocol.crash.ErrorCallback;
import com.alibaba.ha.protocol.crash.ErrorInfo;

import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    private final static String mAppKey = "xxx";
    private final static String mAppSecret = "xxxxxxxx";
    private final static String mHARSAPublicKey = "xxxxx";

    @Override
    public void onCreate() {
        super.onCreate();
        initHa();
    }

    private void initHa() {
        AliHaAdapter.getInstance().preStart(this);
        Log.e("ha", "init");

        //ha init
        AliHaConfig config = new AliHaConfig();
        config.appKey = mAppKey;
        config.appVersion = BuildConfig.VERSION_NAME;
        config.appSecret = mAppSecret;
        config.channel = "mqc_test";
        config.userNick = null;
        config.application = this;
        config.context = getApplicationContext();
        config.isAliyunos = false;
        config.rsaPublicKey = mHARSAPublicKey;

        AliHaAdapter.getInstance().addCustomInfo("custom", "value");
        AliHaAdapter.getInstance().setErrorCallback(new ErrorCallback() {
            @Override
            public Map<String, String> onError(ErrorInfo errorInfo) {
                Map<String, String> map = new HashMap<>();
                map.put("key", "value");
                return map;
            }
        });
        AliHaAdapter.getInstance().addPlugin(Plugin.networkmonitor);
        //启动CrashReporter
        AliHaAdapter.getInstance().addPlugin(Plugin.crashreporter);
        AliHaAdapter.getInstance().addPlugin(Plugin.apm);
        AliHaAdapter.getInstance().addPlugin(Plugin.tlog);             //移动日志，如不需要可注释掉
        AliHaAdapter.getInstance().openDebug(true);          //调试日志开关

        AliHaAdapter.getInstance().start(config);                     //启动
        TLogService.updateLogLevel(TLogLevel.DEBUG); //配置项：控制台可拉取的日志级别
    }
}
