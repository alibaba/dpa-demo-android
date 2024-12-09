package com.alibaba.push.android.demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alibaba.push.android.demo.databinding.MainActivityBinding
import com.alibaba.push.android.demo.databinding.ToastDialogBinding
import java.util.Timer
import java.util.TimerTask


/**
 * main activity
 * @author ren
 * @date 2024/09/20
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding

    private var basicFragment: BasicFuncFragment? = null

    private var mBackKeyPressedTime = 0L

    private var toastDialog: AlertDialog? = null
    private var toastTimer: Timer? = null
    private var toastBinding: ToastDialogBinding? = null
    private var toastTimerTask: TimerTask? = null
    private var isResume: Boolean = false

    //处理透传消息
    private val msgReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                if (it.action == MESSAGE_ACTION) {
                    this@MainActivity.showMessageDialog(
                        it.getStringExtra(MESSAGE_TITLE),
                        it.getStringExtra(MESSAGE_CONTENT),
                        it.getStringExtra(MESSAGE_ID),
                        it.getStringExtra(MESSAGE_TRACE_INFO)
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //浸入状态栏
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        controller.isAppearanceLightStatusBars = true
        //设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT

        binding = MainActivityBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        binding.viewPager.adapter = MainFragmentStateAdapter(
            this,
            mutableListOf(
                BasicFuncFragment().apply {
                      basicFragment = this
                },
                AdvancedFuncFragment(),
                InfoFragment())
        )
        binding.viewPager.isUserInputEnabled = false
        binding.navView.itemIconTintList = null
        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_basic -> {
                    it.setChecked(true)
                    binding.viewPager.setCurrentItem(0, false)
                }
                R.id.navigation_advance -> {
                    if (true == basicFragment?.isRegistered()) {
                        it.setChecked(true)
                        binding.viewPager.setCurrentItem(1, false)
                    }else {
                        showCustomToast(getString(R.string.push_toast_no_register), R.drawable.push_fail)
                    }

                }
                R.id.navigation_info -> {
                    if (true == basicFragment?.isRegistered()) {
                        it.setChecked(true)
                        binding.viewPager.setCurrentItem(2, false)
                    }else {
                        showCustomToast(getString(R.string.push_toast_no_register), R.drawable.push_fail)
                    }
                }
            }
            false
        }
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - mBackKeyPressedTime > 2000) {
            showCustomToast(getString(R.string.toast_double_click_exit), R.drawable.push_success)
            mBackKeyPressedTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        isResume = true
        LocalBroadcastManager.getInstance(this).registerReceiver(msgReceiver, IntentFilter(
            MESSAGE_ACTION))
    }

    override fun onPause() {
        super.onPause()
        isResume = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(msgReceiver)
    }

    private fun showCustomToast(message: String, icon: Int) {
        if (!isResume) {
            return
        }
        toastTimerTask?.cancel()
        if (toastDialog == null) {
            toastDialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert).create()
            toastBinding = ToastDialogBinding.inflate(LayoutInflater.from(this), null, false)
            toastBinding?.tvMessage?.text = message
            toastBinding?.ivIcon?.setImageResource(icon)
            toastDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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