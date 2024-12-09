package com.alibaba.push.android.demo

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import com.heytap.msp.push.HeytapPushManager
import com.hihonor.push.sdk.HonorPushCallback
import com.hihonor.push.sdk.HonorPushClient
import com.huawei.hms.aaid.HmsInstanceId
import com.meizu.cloud.pushsdk.PushManager
import com.meizu.cloud.pushsdk.utils.MzSystemUtils
import com.vivo.push.PushClient
import com.vivo.push.listener.IPushQueryActionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object PushManufacturerUtil {

    const val PUSH_HUAWEI = "huawei"
    const val PUSH_HONOR = "honor"
    const val PUSH_XIAOMI = "xiaomi"
    const val PUSH_VIVO = "vivo"
    const val PUSH_OPPO = "oppo"
    const val PUSH_MEIZU = "meizu"

    fun supportHuaweiPush():Boolean{
        var result = Build.BRAND.equals("huawei", ignoreCase = true)
        if (!result) {
            val emuiVersion = getProp("ro.build.version.emui")
            val harmonyVersion = getProp("hw_sc.build.platform.version")
            result = !TextUtils.isEmpty(emuiVersion) || !TextUtils.isEmpty(harmonyVersion)
        }
        return result && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
    }

    private fun getProp(key: String): String {
        var value = ""
        try {
            val cls = Class.forName("android.os.SystemProperties")
            val get = cls.getDeclaredMethod("get", String::class.java)
            value = get.invoke(null as Any?, key) as String
        } catch (ignored: Throwable) {
        }
        return value
    }

    fun supportHonorPush(context: Context): Boolean {
        return HonorPushClient.getInstance().checkSupportHonorPush(context)
    }

    fun supportXIAOMIPush(): Boolean {
        val brand = if (TextUtils.isEmpty(Build.BRAND)) Build.MANUFACTURER else Build.BRAND
        return "xiaomi" == brand.lowercase() || "redmi" == brand.lowercase() || "blackshark" ==brand.lowercase()
    }

    fun supportVIVOPush(context: Context): Boolean{
        return PushClient.getInstance(context).isSupport
    }

    fun supportOPPOPush(context: Context): Boolean {
        return HeytapPushManager.isSupportPush(context)
    }

    fun supportMEIZUPush(context: Context): Boolean {
        return try {
            var var1 = getProp("ro.meizu.product.model")
            var flag = (!TextUtils.isEmpty(var1) || "meizu".equals(Build.BRAND, ignoreCase = true)
                    || "22c4185e".equals(Build.BRAND, ignoreCase = true))
            if (!flag) {
                var1 = getProp("ro.product.other.brand")
                flag = var1.equals("Unisoc", ignoreCase = true)
            }
            flag
        } catch (e: Throwable) {
            // 直接调用判断，其内部会起不必要线程发POST请求
            MzSystemUtils.isBrandMeizu()
        }
    }

    fun getHuaweiToken(context: Context, callback: (String?) -> Unit) = runBlocking{
        launch(Dispatchers.IO) {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            val value = appInfo.metaData.getString("com.huawei.hms.client.appid")
            var appId = ""
            if (!TextUtils.isEmpty(value)) {
                appId = value!!.replace("appid=", "")
            }
            val token = if (TextUtils.isEmpty(appId)) {
                HmsInstanceId.getInstance(context).token
            } else {
                HmsInstanceId.getInstance(context).getToken(appId, "HCM")
            }
            callback.invoke(token)
        }

    }

    fun getHonorToken(callback: (String?)->Unit){
        HonorPushClient.getInstance().getPushToken(object : HonorPushCallback<String?> {
            override fun onSuccess(token: String?) {
                callback.invoke(token)
            }

            override fun onFailure(errorCode: Int, errorMsg: String) {

            }
        })
    }

    fun getVIVOToken(context: Context, callback: (String?) -> Unit){
        PushClient.getInstance(context).getRegId(object : IPushQueryActionListener {
            override fun onSuccess(regId: String) {
                callback.invoke(regId)
            }

            override fun onFail(errorCode: Int) {
            }
        })
    }

    fun getOPPOToken():String?{
        return HeytapPushManager.getRegisterID()
    }

    fun getMEIZUToken(context: Context): String?{
        return PushManager.getPushId(context)
    }

}