package com.alibaba.push.android.demo

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.alibaba.push.android.demo.databinding.ToastDialogBinding
import java.util.Timer
import java.util.TimerTask

open class BaseActivity : Activity() {

    private var toastDialog: AlertDialog? = null
    private var toastTimer: Timer? = null
    private var toastBinding: ToastDialogBinding? = null
    private var toastTimerTask: TimerTask? = null

    fun showCustomToast(message: String, icon: Int) {
        toastTimerTask?.cancel()
        if (toastDialog == null) {
            toastDialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert).create()
            toastBinding = ToastDialogBinding.inflate(LayoutInflater.from(this), null, false)
            toastBinding?.tvMessage?.text = message
            toastBinding?.ivIcon?.setImageResource(icon)
            toastDialog?.window?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT)
            )
           toastDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
            toastDialog?.window?.setDimAmount(0f)
            toastDialog?.setView(toastBinding?.root)
            toastDialog?.setCanceledOnTouchOutside(false)
            toastDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            toastDialog?.show()
            toastTimer = Timer()
        } else if (true == toastDialog?.isShowing) {
            toastBinding?.tvMessage?.text = message
            toastBinding?.ivIcon?.setImageResource(icon)
        }else {
            toastBinding?.tvMessage?.text = message
            toastBinding?.ivIcon?.setImageResource(icon)
            toastDialog?.show()
        }
        toastTimerTask = object: TimerTask(){
            override fun run() {
                toastDialog?.dismiss()
            }
        }
        toastTimer?.schedule(toastTimerTask, 3000)
    }

    override fun onStop() {
        super.onStop()
        toastTimerTask?.cancel()
        if (true == toastDialog?.isShowing) {
            toastDialog?.dismiss()
        }
    }
}