package com.alibaba.push.android.demo

import android.content.Context
import org.json.JSONObject


object Config {

    private const val CONFIG_FILE_NAME = "aliyun-emas-services.json"


    var APP_KEY = ""

    var APP_SECRET = ""

    fun init(context: Context) {
        val inputStream = context.assets.open(CONFIG_FILE_NAME)
        val buffer = ByteArray(inputStream.available())
        inputStream.read(buffer)
        inputStream.close()
        val configJsonStr = String(buffer, Charsets.UTF_8)
        val configJsonObj = JSONObject(configJsonStr).getJSONObject("config")


        if (configJsonObj.has("push.appkey")) {
            APP_KEY = configJsonObj.optString("push.appkey")
        }

        if (configJsonObj.has("push.appsecret")) {
            APP_SECRET = configJsonObj.optString("push.appsecret")
        }
    }

}