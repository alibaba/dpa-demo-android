package com.alibaba.push.android.demo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alibaba.sdk.android.push.CloudPushService
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import kotlinx.coroutines.launch

/**
 * 基础功能的ViewModel
 * @author ren
 * @date 2024/09/20
 */
class BasicFuncViewModel(application: Application): AndroidViewModel(application) {

    companion object {
        const val RESPONSE_CHANNEL_OPEN = "on"
    }

    val channelState = SingleLiveData<Boolean>().apply { value = false }

    val divideGroupState = SingleLiveData<Boolean>().apply { value = false }

    val msgReceiveByService = SingleLiveData<Boolean>().apply { value = false }

    val logLevel = SingleLiveData<Int>().apply { value = CloudPushService.LOG_OFF }

    private val pushService = PushServiceFactory.getCloudPushService()

    var tempLogLevel = SingleLiveData<Int>().apply { value = CloudPushService.LOG_OFF }

    var hasRegistered = SingleLiveData<Boolean>().apply { value = false }

    var registerBtnText = SingleLiveData<String>()

    val registerBtnAlpha = SingleLiveData<Float>().apply { value = 1.0f }

    var showCustomToast: ((String, Int) ->Unit)? = null

    private val preferences: SharedPreferences = getApplication<MainApplication>().getSharedPreferences(
        SP_FILE_NAME,
        Context.MODE_PRIVATE
    )

    fun init(){
        logLevel.value = preferences.getInt(SP_KEY_LOG_LEVEL, CloudPushService.LOG_OFF)
        pushService.setLogLevel(logLevel.value?:CloudPushService.LOG_OFF)
    }

    /**
     * 注册
     */
    fun register(){
        if (true == hasRegistered.value) {
            return
        }
        hasRegistered.value = true
        registerBtnAlpha.value = 0.6f
        registerBtnText.value = getApplication<Application>().getString(R.string.push_registering)
        pushService.register(getApplication(), object: CommonCallback{
            override fun onSuccess(response: String?) {
                registerBtnText.value = getApplication<Application>().getString(R.string.push_register_btn_success)
                showCustomToast?.invoke(getString(R.string.push_toast_register_success), R.drawable.push_success)
                getChannelStateFromServer()
            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                registerBtnText.value = getApplication<Application>().getString(R.string.push_register_btn_fail)
                showCustomToast?.invoke(String.format(getString(R.string.push_toast_register_fail), errorMessage), R.drawable.push_fail)
            }

        })
    }

    /**
     * 获取通道状态
     */
    fun getChannelStateFromServer(){
        pushService.checkPushChannelStatus(object:CommonCallback{
            override fun onSuccess(response: String?) {
                channelState.value = RESPONSE_CHANNEL_OPEN == response
            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {

            }

        })
    }

    /**
     * 打开/关闭通知通道
     */
    fun switchPushChannel(){
        if (true == channelState.value) {
            pushService.turnOffPushChannel(object:CommonCallback{
                override fun onSuccess(response: String?) {
                    channelState.value = false
                    showCustomToast?.invoke(getString(R.string.push_toast_close_push_channel_success), R.drawable.push_success)
                }

                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    showCustomToast?.invoke(String.format(getString(R.string.push_toast_close_push_channel_fail), errorMessage), R.drawable.push_fail)
                }

            })
        }else {
            pushService.turnOnPushChannel(object:CommonCallback{
                override fun onSuccess(response: String?) {
                    channelState.value = true
                    showCustomToast?.invoke(getString(R.string.push_toast_open_push_channel_success), R.drawable.push_success)
                }

                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    showCustomToast?.invoke(String.format(getString(R.string.push_toast_open_push_channel_fail), errorMessage), R.drawable.push_fail)
                }
            })
        }
    }

    /**
     * 是否分组展示通知
     */
    fun switchDivideGroup(){
        toggleGroupShowNotification(!(divideGroupState.value!!))
    }

    /**
     * 点击是否分组展示通知开关
     */
    fun toggleGroupShowNotification(checked: Boolean) {
        divideGroupState.value = checked
        pushService.setNotificationShowInGroup(checked)
        if (true == divideGroupState.value) {
            showCustomToast?.invoke(getString(R.string.push_open_group_show_notificaion), R.drawable.push_success)
        }else {
            showCustomToast?.invoke(getString(R.string.push_close_group_show_notificaion), R.drawable.push_success)
        }
    }

    /**
     * 改变通知接收方式
     */
    fun switchMsgReceiver(){
        toggleMessageReceiver(!msgReceiveByService.value!!)
    }

    /**
     * 点击使用Service接收推送开关
     */
    fun toggleMessageReceiver(checked: Boolean) {
        msgReceiveByService.value = checked
        if (true == msgReceiveByService.value) {
            pushService.setPushIntentService(MyMessageIntentService::class.java)
            showCustomToast?.invoke(getString(R.string.push_toast_use_service_deal_message), R.drawable.push_success)
        }else {
            pushService.setPushIntentService(null)
            showCustomToast?.invoke(getString(R.string.push_toast_use_receiver_deal_message), R.drawable.push_success)
        }
    }

    /**
     * 清除通知
     */
    fun clearAllNotification(){
        pushService.clearNotifications()
        showCustomToast?.invoke(getString(R.string.push_toast_already_clear), R.drawable.push_success)
    }

    /**
     * 设置日志输出等级
     */
    fun setLogLevel(){
        if (tempLogLevel.value == logLevel.value) {
            return
        }
        logLevel.value = tempLogLevel.value
        pushService.setLogLevel(tempLogLevel.value?:CloudPushService.LOG_OFF)
        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putInt(SP_KEY_LOG_LEVEL, logLevel.value?:CloudPushService.LOG_OFF)
            editor.apply()
        }
    }

    fun setTempLogLevel(level: Int) {
        if (level == tempLogLevel.value) {
            return
        }
        tempLogLevel.value = level
    }

    private fun getString(res: Int): String {
        return getApplication<MainApplication>().getString(res)
    }

}