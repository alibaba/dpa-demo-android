package com.alibaba.push.android.demo

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.TextUtils
import com.alibaba.sdk.android.push.CloudPushService
import com.alibaba.sdk.android.push.HonorRegister
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister
import com.alibaba.sdk.android.push.noonesdk.PushInitConfig
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.alibaba.sdk.android.push.register.MeizuRegister
import com.alibaba.sdk.android.push.register.MiPushRegister
import com.alibaba.sdk.android.push.register.OppoRegister
import com.alibaba.sdk.android.push.register.VivoRegister

class MainApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        Config.init(this)
        initPushSdk(this)
    }

    private fun initPushSdk(context: Context) {
        if (TextUtils.isEmpty(Config.APP_KEY)) {
            PushServiceFactory.init(context)
        }else {
            PushServiceFactory.init(PushInitConfig.Builder()
                .application(this)
                .appKey(Config.APP_KEY)
                .appSecret(Config.APP_SECRET)
                .build())
        }
        PushServiceFactory.getCloudPushService().setDebug(true)
        PushServiceFactory.getCloudPushService().setLogLevel(CloudPushService.LOG_DEBUG)
        PushServiceFactory.getCloudPushService().setNotificationSmallIcon(R.mipmap.ic_launcher)
        createNotificationChannel()
        initOthers()
    }

    private fun initOthers() {
        HuaWeiRegister.register(this) // 接入华为辅助推送
        HonorRegister.register(this)  //荣耀推送
        MiPushRegister.register(this, "2882303761520226056", "5882022666056") // 初始化小米辅助推送
        VivoRegister.registerAsync(applicationContext) //接入vivo辅助推送
        OppoRegister.registerAsync(applicationContext, "9a2d869058e94b3b9ef8ad254d45d722", "e519313fd30545afb8338810b6b941f7") //OPPO辅助推送
        MeizuRegister.registerAsync(applicationContext, "121026", "ba7cd1a355d8416dab0142e390170e5a") //接入魅族辅助推送
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // 通知渠道的id
            val id = "8.0up"
            // 用户可以看到的通知渠道的名字.
            val name: CharSequence = "notification channel"
            val mChannel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            // 配置通知渠道的属性
            mChannel.description = "notification description"
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true)
            mChannel.setVibrationPattern(longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400))
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

}