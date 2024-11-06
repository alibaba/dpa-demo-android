package com.alibaba.push.android.demo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alibaba.push.android.demo.databinding.InfoFragmentBinding
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory


/**
 * 信息Fragment
 * @author ren
 * @date 2024/09/20
 */
class InfoFragment : Fragment() {

    private lateinit var binding: InfoFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = InfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        binding.tvBrand.text = when {
            PushManufacturerUtil.supportHuaweiPush() -> PushManufacturerUtil.PUSH_HUAWEI
            PushManufacturerUtil.supportHonorPush(requireContext()) -> PushManufacturerUtil.PUSH_HONOR
            PushManufacturerUtil.supportXIAOMIPush() -> PushManufacturerUtil.PUSH_XIAOMI
            PushManufacturerUtil.supportVIVOPush(requireContext()) -> PushManufacturerUtil.PUSH_VIVO
            PushManufacturerUtil.supportOPPOPush(requireContext()) -> PushManufacturerUtil.PUSH_OPPO
            PushManufacturerUtil.supportMEIZUPush(requireContext()) -> PushManufacturerUtil.PUSH_MEIZU
            else -> getString(R.string.push_unknow_brand)
        }

//        when{
//            PushManufacturerUtil.supportHuaweiPush() -> PushManufacturerUtil.getHuaweiToken(requireContext(), ::setToken)
//            PushManufacturerUtil.supportHonorPush(requireContext()) -> PushManufacturerUtil.getHonorToken(::setToken)
//            PushManufacturerUtil.supportXIAOMIPush() -> binding.tvToken.text = getString(R.string.push_cannot_get_token)
//            PushManufacturerUtil.supportVIVOPush(requireContext()) -> PushManufacturerUtil.getVIVOToken(requireContext(), ::setToken)
//            PushManufacturerUtil.supportOPPOPush(requireContext()) -> setToken(PushManufacturerUtil.getOPPOToken())
//            PushManufacturerUtil.supportMEIZUPush(requireContext()) -> setToken(PushManufacturerUtil.getMEIZUToken(requireContext()))
//            else -> setToken(null)
//        }

        setToken(null)

        binding.tvToken.setOnClickListener {
            copy(binding.tvToken.text)
        }

    }

    private fun setToken(token: String?) {
        binding.tvToken.text = if (TextUtils.isEmpty(token)) {
            getString(R.string.push_cannot_get_token)
        }else {
            token
        }
    }

    private fun copy(text: CharSequence?) {
        if (TextUtils.isEmpty(text))return
        val clipboard: ClipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Mimic Text", text)
        clipboard.setPrimaryClip(clip)
        requireContext().toast(R.string.push_toast_already_copy)
    }
}