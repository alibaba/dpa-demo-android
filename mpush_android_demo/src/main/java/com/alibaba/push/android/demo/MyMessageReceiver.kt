package com.alibaba.push.android.demo

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alibaba.sdk.android.push.MessageReceiver
import com.alibaba.sdk.android.push.notification.CPushMessage

/**
 * 通过广播接收下发的通知和消息
 * @author ren
 * @date 2024/09/20
 */
class MyMessageReceiver: MessageReceiver() {
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
        context?.toast(R.string.push_toast_receiver_deal_message)
    }

    override fun onMessage(context: Context?, p1: CPushMessage?) {
        context?.apply {
            p1?.let {
                val intent = Intent(MESSAGE_ACTION).apply {
                    putExtra(MESSAGE_TITLE, it.title)
                    putExtra(MESSAGE_CONTENT, it.content)
                    putExtra(MESSAGE_ID, it.messageId)
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