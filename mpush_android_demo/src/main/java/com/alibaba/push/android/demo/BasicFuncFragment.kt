package com.alibaba.push.android.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.alibaba.push.android.demo.databinding.BasicFuncFragmentBinding
import com.alibaba.push.android.demo.databinding.LogLevelDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog


/**
 * 基础功能fragment
 * @author ren
 * @date 2024/09/20
 */
class BasicFuncFragment : BaseFragment() {

    private lateinit var binding: BasicFuncFragmentBinding
    private lateinit var viewModel: BasicFuncViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[BasicFuncViewModel::class.java]
        viewModel.init()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BasicFuncFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.registerBtnText.value = getString(R.string.push_register_receive)
        binding.tvLogLevel.setOnClickListener {
            showLogLevelDialog()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.showCustomToast = {message, icon ->
            showCustomToast(message, icon)
        }
        binding.scMessageReceiver.setOnCheckedChangeListener {_, isChecked ->
            viewModel.toggleMessageReceiver(isChecked)
        }
        binding.scToggleGroup.setOnCheckedChangeListener{_, isChecked ->
            viewModel.toggleGroupShowNotification(isChecked)
        }
    }

    private fun showLogLevelDialog(){
        val logLevelDialogBinding = LogLevelDialogBinding.inflate(layoutInflater)
        viewModel.tempLogLevel.value = viewModel.logLevel.value
        logLevelDialogBinding.viewModel = viewModel
        val dialog = BottomSheetDialog(requireContext(), R.style.RoundedBottomSheetDialog).apply {
            setContentView(logLevelDialogBinding.root)
            logLevelDialogBinding.lifecycleOwner = this
            show()
        }
        logLevelDialogBinding.ivClose.setOnClickListener { dialog.dismiss() }
        logLevelDialogBinding.tvCancel.setOnClickListener { dialog.dismiss() }
        logLevelDialogBinding.tvConfirm.setOnClickListener {
            viewModel.setLogLevel()
            dialog.dismiss()
        }
    }

    fun isRegistered() = viewModel.hasRegistered.value
}