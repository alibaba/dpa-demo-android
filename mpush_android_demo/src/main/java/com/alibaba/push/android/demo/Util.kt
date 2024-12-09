package com.alibaba.push.android.demo

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.alibaba.push.android.demo.databinding.BindDialogBinding
import com.alibaba.push.android.demo.databinding.InputDialogBinding
import com.alibaba.push.android.demo.databinding.MessageShowDialogBinding
import com.alibaba.push.android.demo.databinding.ToastDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.Objects
import java.util.Timer


fun Int.toDp(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}

fun Int.toPx():Int {
    val metrics = Resources.getSystem().displayMetrics
    return Math.round(this * (metrics.densityDpi / 160f))
}

fun Context.showInputDialog(
    title: Int,
    hint: Int,
    showAlert: Boolean,
    showAliasInput: Boolean,
    inputMaxLength:Int = 0,
    inputCallback: ((String, String?) -> Unit)? = null
) {
    val inputDialogBinding = InputDialogBinding.inflate(LayoutInflater.from(this))
    inputDialogBinding.title = getString(title)
    inputDialogBinding.hint = getString(hint)
    inputDialogBinding.showAlert = showAlert
    if (inputMaxLength != 0) {
        inputDialogBinding.etInput.filters = arrayOf(LengthFilter(inputMaxLength))
    }
    inputDialogBinding.showAliasInput = showAliasInput
    val dialog = BottomSheetDialog(this, R.style.RoundedBottomSheetDialog).apply {

        setContentView(inputDialogBinding.root)
        inputDialogBinding.lifecycleOwner = this
        show()
    }
    inputDialogBinding.ivClose.setOnClickListener { dialog.dismiss() }
    inputDialogBinding.tvCancel.setOnClickListener { dialog.dismiss() }
    inputDialogBinding.tvConfirm.setOnClickListener {
        val inputText = inputDialogBinding.etInput.text.toString().trim()
        if (TextUtils.isEmpty(inputText)) {
            Toast.makeText(this, getString(R.string.push_input_empty), Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        inputCallback?.invoke(inputText, inputDialogBinding.etAlias.text.toString().trim())
        dialog.dismiss()
    }
}

/**
 * 展示下发消息内容
 */
fun Context.showMessageDialog(messageTitle: String?, messageContent: String?, messageId: String?, traceInfo: String?) {

    val mAlertDialog = AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert).create()

    val binding = MessageShowDialogBinding.inflate(LayoutInflater.from(this), null, false)
    binding.title = String.format(getString(R.string.push_message_title), messageTitle)
    binding.content = String.format(getString(R.string.push_message_content, messageContent))
    binding.messageId = String.format(getString(R.string.push_message_id), messageId)
    binding.traceInfo = String.format(getString(R.string.push_message_trace_info), traceInfo)
    binding.tvKnow.setOnClickListener {
        mAlertDialog.dismiss()
    }
    mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    mAlertDialog.setView(binding.root)
    mAlertDialog.show()

}

fun Context.getStatusBarHeight(): Int {
    val statusBarId = resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (statusBarId > 0) {
        resources.getDimensionPixelSize(statusBarId)
    } else {
        22.toDp()
    }
}

fun Context.getAppMetaData(key: String): String {
    try {
        val info = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )
        if (info.metaData.containsKey(key)) {
            return "${info.metaData.get(key)}"
        }
    } catch (e: PackageManager.NameNotFoundException) {

    }
    return ""
}

fun Context.showBindDialog(
    title: Int,
    hint: Int,
    viewModel: AdvanceFuncViewModel,
    isBindAccount: Boolean = true,
    inputMaxLength: Int = 0,
    inputCallback: ((String) -> Unit)? = null
) {
    val bindDialogBinding = BindDialogBinding.inflate(LayoutInflater.from(this))

    bindDialogBinding.title = getString(title)
    bindDialogBinding.hint = getString(hint)
    if (inputMaxLength != 0) {
        bindDialogBinding.etInput.filters = arrayOf(LengthFilter(inputMaxLength))
    }
    bindDialogBinding.etInput.setBackgroundResource(R.drawable.push_bind_data_bg)
    bindDialogBinding.etInput.setText(if (isBindAccount) {viewModel.account.value} else {viewModel.phone.value})
    bindDialogBinding.etInput.setTextColor(ContextCompat.getColor(this, R.color.push_color_text_gray))
    bindDialogBinding.etInput.isEnabled = false
    bindDialogBinding.leftBtnText = getString(R.string.push_unbind)
    val dialog = BottomSheetDialog(this, R.style.RoundedBottomSheetDialog).apply {
        setContentView(bindDialogBinding.root)
        bindDialogBinding.lifecycleOwner = this
        show()
    }
    bindDialogBinding.ivClose.setOnClickListener { dialog.dismiss() }
    bindDialogBinding.tvCancel.setOnClickListener {
        val data = if (isBindAccount) {viewModel.account.value} else {viewModel.phone.value}
        if (TextUtils.isEmpty(data)) {
            dialog.dismiss()
        }else {
            if (isBindAccount) {
                viewModel.unbindAccount()
            }else {
                viewModel.unbindPhone()
            }
            bindDialogBinding.etInput.setBackgroundResource(R.drawable.push_bg_input)
            bindDialogBinding.etInput.setText("")
            bindDialogBinding.leftBtnText = getString(R.string.push_cancel)
            bindDialogBinding.etInput.isEnabled = true
        }

    }
    bindDialogBinding.tvConfirm.setOnClickListener {
        val data = if (isBindAccount) {viewModel.account.value } else {viewModel.phone.value}
        if (!TextUtils.isEmpty(data)){
            Toast.makeText(this, getString(R.string.push_unbind_first), Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        val inputText = bindDialogBinding.etInput.text.toString().trim()
        if (TextUtils.isEmpty(inputText)) {
            Toast.makeText(this, getString(R.string.push_input_empty), Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        inputCallback?.invoke(inputText)
        dialog.dismiss()
    }
}

