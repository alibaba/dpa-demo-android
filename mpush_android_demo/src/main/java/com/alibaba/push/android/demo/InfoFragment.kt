package com.alibaba.push.android.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.alibaba.push.android.demo.databinding.InfoFragmentBinding
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.xiaomi.mipush.sdk.MiPushClient

/**
 * 信息Fragment
 * @author ren
 * @date 2024/09/20
 */
class InfoFragment : BaseFragment() {

    private lateinit var binding: InfoFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = InfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusBarHeight = requireContext().getStatusBarHeight()
        binding.vStatusBg.layoutParams?.height = statusBarHeight
        binding.nestedScrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            val alpha = if (scrollY * 1f / statusBarHeight > 1) {
                1f
            } else {
                scrollY * 1f / statusBarHeight
            }
            binding.vStatusBg.alpha = alpha
        }


        val service = PushServiceFactory.getCloudPushService()
        binding.tvAppKey.text = requireContext().getAppMetaData("com.alibaba.app.appkey")
        binding.tvAppSecret.text = requireContext().getAppMetaData("com.alibaba.app.appsecret")
        binding.tvDeviceId.text = service.deviceId
        binding.tvUTDID.text = service.utDeviceId
        binding.tvSDKVersion.text = String.format(
            requireContext().getString(R.string.push_version),
            BuildConfig.PUSH_VERSION
        )
        binding.tvPackage.text = requireContext().packageName

        binding.tvDeviceId.setOnClickListener {
           copy(binding.tvDeviceId.text)
        }

        binding.tvUTDID.setOnClickListener {
            copy(binding.tvUTDID.text)
        }

        binding.ivCopyDeviceId.setOnClickListener {
            copy(binding.tvDeviceId.text)
        }

        binding.ivCopyUTDID.setOnClickListener {
            copy(binding.tvUTDID.text)
        }

        binding.tvBrand.text = when {
            PushManufacturerUtil.supportHuaweiPush() -> PushManufacturerUtil.PUSH_HUAWEI
            PushManufacturerUtil.supportHonorPush(requireContext()) -> PushManufacturerUtil.PUSH_HONOR
            PushManufacturerUtil.supportXIAOMIPush() -> PushManufacturerUtil.PUSH_XIAOMI
            PushManufacturerUtil.supportVIVOPush(requireContext()) -> PushManufacturerUtil.PUSH_VIVO
            PushManufacturerUtil.supportOPPOPush(requireContext()) -> PushManufacturerUtil.PUSH_OPPO
            PushManufacturerUtil.supportMEIZUPush(requireContext()) -> PushManufacturerUtil.PUSH_MEIZU
            else -> getString(R.string.push_unknow_brand)
        }

        when{
            PushManufacturerUtil.supportHuaweiPush() -> PushManufacturerUtil.getHuaweiToken(requireContext()) {
                setToken(it)
            }
            PushManufacturerUtil.supportHonorPush(requireContext()) -> PushManufacturerUtil.getHonorToken {
                setToken(it)
            }
            PushManufacturerUtil.supportXIAOMIPush() -> setToken(MiPushClient.getRegId(requireContext()))
            PushManufacturerUtil.supportVIVOPush(requireContext()) -> PushManufacturerUtil.getVIVOToken(requireContext()) {
                setToken(it)
            }
            PushManufacturerUtil.supportOPPOPush(requireContext()) -> setToken(PushManufacturerUtil.getOPPOToken())
            PushManufacturerUtil.supportMEIZUPush(requireContext()) -> setToken(PushManufacturerUtil.getMEIZUToken(requireContext()))
            else -> setToken(null)
        }

        binding.tvToken.setOnClickListener {
            copy(binding.tvToken.text)
        }

        binding.ivCopyToken.setOnClickListener {
            copy(binding.tvToken.text)
        }
    }

    private fun setToken(token: String?) {
        if (TextUtils.isEmpty(token)) {
            binding.tvToken.isVisible = false
            binding.ivCopyToken.isVisible = false
            binding.tvTokenLabel.isVisible = false
        }else {
            binding.tvToken.text = token
            binding.tvToken.isVisible = true
            binding.ivCopyToken.isVisible = true
            binding.tvTokenLabel.isVisible = true
        }
    }

    private fun copy(text: CharSequence?) {
        if (TextUtils.isEmpty(text))return
        val clipboard: ClipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mimic Text", text)
        clipboard.setPrimaryClip(clip)
        showCustomToast(getString(R.string.push_toast_already_copy), R.drawable.push_success)
    }
}