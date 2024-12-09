package com.alibaba.push.android.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import com.alibaba.push.android.demo.databinding.AddTagBinding
import com.alibaba.push.android.demo.databinding.ToastDialogBinding
import com.alibaba.sdk.android.push.CloudPushService
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import java.util.Timer
import java.util.TimerTask

/**
 * 添加标签页面
 * @author ren
 * @date 2024-11-1
 */
class AddTagActivity: BaseActivity() {

    private lateinit var binding: AddTagBinding

    private var target = 0
    private var alias: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        //设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT

        binding = AddTagBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { finish() }
        binding.tvCancel.setOnClickListener { finish() }
        when(intent.getIntExtra(KEY_TAG_TARGET_TYPE, -1)) {
            CloudPushService.DEVICE_TARGET -> {
                binding.deviceTagEnable = true
                binding.ivDeviceTagStatus.setImageResource(R.drawable.push_check)
                binding.aliasTagEnable = false
                binding.ivAliasTagStatus.setImageResource(R.drawable.push_unable_check)
                binding.accountTagEnable = false
                binding.ivAccountTagStatus.setImageResource(R.drawable.push_unable_check)
                target = CloudPushService.DEVICE_TARGET
            }
            CloudPushService.ALIAS_TARGET -> {
                binding.deviceTagEnable = false
                binding.ivDeviceTagStatus.setImageResource(R.drawable.push_unable_check)
                binding.aliasTagEnable = true
                binding.ivAliasTagStatus.setImageResource(R.drawable.push_check)
                binding.accountTagEnable = false
                binding.ivAccountTagStatus.setImageResource(R.drawable.push_unable_check)
                target = CloudPushService.ALIAS_TARGET
                binding.tvAlias.text = getString(R.string.push_alias_no_choose)
            }
            CloudPushService.ACCOUNT_TARGET -> {
                binding.deviceTagEnable = false
                binding.ivDeviceTagStatus.setImageResource(R.drawable.push_unable_check)
                binding.aliasTagEnable = false
                binding.ivAliasTagStatus.setImageResource(R.drawable.push_unable_check)
                binding.accountTagEnable = true
                binding.ivAccountTagStatus.setImageResource(R.drawable.push_check)
                target = CloudPushService.ACCOUNT_TARGET
            }
            else -> {
                binding.deviceTagEnable = true
                binding.ivDeviceTagStatus.setImageResource(R.drawable.push_uncheck)
                getAlias()
                initAccount()
            }
        }

        binding.tvAccount.text = getAccount()?:""

        binding.llDeviceTag.setOnClickListener {
            if (true != binding.deviceTagEnable || target == CloudPushService.DEVICE_TARGET) return@setOnClickListener
            target = CloudPushService.DEVICE_TARGET
            resetCheckStatus()
            binding.ivDeviceTagStatus.setImageResource(R.drawable.push_check)
        }

        binding.llAliasTag.setOnClickListener {
            if (true != binding.aliasTagEnable) return@setOnClickListener
            val intent = Intent(this, AliasListActivity::class.java)
            startActivityForResult(intent, 100)
        }
        binding.llAccountTag.setOnClickListener {
            if (true != binding.accountTagEnable || target == CloudPushService.ACCOUNT_TARGET) return@setOnClickListener
            target = CloudPushService.ACCOUNT_TARGET
            resetCheckStatus()
            binding.ivAccountTagStatus.setImageResource(R.drawable.push_check)
        }

        binding.tvConfirm.setOnClickListener {
            clickConfirm()
        }
    }

    private fun clickConfirm(){
        val tag = binding.etTag.text.toString().trim()
        if (TextUtils.isEmpty(tag)) {
            showCustomToast(getString(R.string.push_tag_input_not), R.drawable.push_fail)
            return
        }
        if (target == 0) {
            showCustomToast(getString(R.string.push_tag_type_no_choose), R.drawable.push_fail)
            return
        }
        if (target == CloudPushService.ALIAS_TARGET && TextUtils.isEmpty(alias)) {
            showCustomToast(getString(R.string.push_alias_no_choose), R.drawable.push_fail)
            return
        }
        setResult(RESULT_OK, Intent().apply {
            putExtra(KEY_TAG, tag)
            putExtra(KEY_TAG_TARGET_TYPE, target)
            if (target == CloudPushService.ALIAS_TARGET) {
                putExtra(KEY_ALIAS, alias)
            }
        })
        finish()
    }

    private fun resetCheckStatus(){
        if (true == binding.deviceTagEnable){
            binding.ivDeviceTagStatus.setImageResource(R.drawable.push_uncheck)
        }
        if (true == binding.aliasTagEnable){
            binding.ivAliasTagStatus.setImageResource(R.drawable.push_uncheck)
        }
        if (true == binding.accountTagEnable){
            binding.ivAccountTagStatus.setImageResource(R.drawable.push_uncheck)
        }
    }

    private fun getAlias(){
        PushServiceFactory.getCloudPushService().listAliases(object : CommonCallback {
            override fun onSuccess(response: String?) {
                binding.aliasTagEnable = !TextUtils.isEmpty(response)
                if (true == binding.aliasTagEnable) {
                    binding.ivAliasTagStatus.setImageResource(R.drawable.push_uncheck)
                }else {
                    binding.ivAliasTagStatus.setImageResource(R.drawable.push_unable_check)
                }

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                binding.aliasTagEnable = false
                binding.ivAliasTagStatus.setImageResource(R.drawable.push_unable_check)
            }

        })
    }

    private fun initAccount(){
        val account = getAccount()
        binding.accountTagEnable = !TextUtils.isEmpty(account)
        if (true == binding.accountTagEnable) {
            binding.ivAccountTagStatus.setImageResource(R.drawable.push_uncheck)
        }else {
            binding.ivAccountTagStatus.setImageResource(R.drawable.push_unable_check)
        }
    }

    private fun getAccount():String?{
        return getSharedPreferences(
            SP_FILE_NAME,
            Context.MODE_PRIVATE
        ).getString(SP_KEY_BIND_ACCOUNT, "")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            data?.getStringExtra(KEY_ALIAS)?.let {
                alias = it
                binding.tvAlias.text = it
                target = CloudPushService.ALIAS_TARGET
                resetCheckStatus()
                binding.ivAliasTagStatus.setImageResource(R.drawable.push_check)
            }

        }
    }
}


