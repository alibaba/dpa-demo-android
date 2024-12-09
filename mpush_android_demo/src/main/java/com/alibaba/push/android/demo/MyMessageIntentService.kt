package com.alibaba.push.android.demo

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alibaba.sdk.android.push.AliyunMessageIntentService
import com.alibaba.sdk.android.push.MessageReceiver
import com.alibaba.sdk.android.push.notification.CPushMessage

/**
 * 通过Service 处理下发通知和消息
 * @author ren
 * @date 2024/09/20
 */
class MyMessageIntentService: AliyunMessageIntentService() {
    override fun onNotificationOpened(p0: Context?, p1: String?, p2: String?, p3: String?) {

    }

    override fun onNotificationRemoved(p0: Context?, p1: String?) {

    }

    override fun onNotification(
        context: Context?,
        p1: String?,
        p2: String?,
        p3: MutableMap<String, String>?
    ) {
        context?.let {
            Toast.makeText(it, it.getString(R.string.push_toast_service_deal_message), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMessage(context: Context?, p1: CPushMessage?) {
        context?.apply {
            p1?.let {
                val intent = Intent(MESSAGE_ACTION).apply {
                    putExtra(MESSAGE_TITLE, it.title)
                    putExtra(MESSAGE_CONTENT, it.content)
                    putExtra(MessageReceiver.MESSAGE_ID, it.messageId)
                    putExtra(MESSAGE_TRACE_INFO, it.traceInfo)
                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }
    }

    override fun onNotificationClickedWithNoAction(
        p0: Context?,
        p1: String?,
        p2: String?,
        p3: String?
    ) {

    }

    override fun onNotificationReceivedInApp(
        p0: Context?,
        p1: String?,
        p2: String?,
        p3: MutableMap<String, String>?,
        p4: Int,
        p5: String?,
        p6: String?
    ) {

    }
}