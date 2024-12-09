package com.alibaba.push.android.demo

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.push.android.demo.databinding.AdvancedFuncFragmentBinding
import com.alibaba.push.android.demo.databinding.CountLimitDialogBinding
import com.alibaba.sdk.android.push.CloudPushService
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * 高级功能Fragment
 * @author ren
 * @date 2024/09/20
 */
class AdvancedFuncFragment : BaseFragment() {

    private lateinit var binding: AdvancedFuncFragmentBinding

    private lateinit var viewModel: AdvanceFuncViewModel

    private var deviceTagAdapter: LabelAdapter? = null
    private var aliasTagAdapter: LabelAdapter? = null
    private var accountTagAdapter: LabelAdapter? = null
    private var aliasAdapter: AliasLabelAdapter? = null

    //查看全部标签页面启动,接收返回数据
    private val requestDataLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val type = result.data?.getIntExtra(TYPE, 0)
                when (type) {
                    LABEL_DEVICE_TAG -> viewModel.getDeviceTagFromServer()
                    LABEL_ALIAS_TAG -> viewModel.getAliasTagFromSp()
                    LABEL_ACCOUNT_TAG -> viewModel.getAccountTagFromSp()
                    LABEL_ALIAS -> viewModel.getAliasListFromServer()
                }
            }
        }

    //添加标签页启动,并接收回调
    private val addTagLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.apply {
                    val tag = getStringExtra(KEY_TAG)
                    val target =
                        getIntExtra(KEY_TAG_TARGET_TYPE, CloudPushService.DEVICE_TARGET)
                    var alias: String? = ""
                    if (target == CloudPushService.ALIAS_TARGET) {
                        alias = getStringExtra(KEY_ALIAS)
                    }
                    viewModel.addTag(tag, target, alias)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AdvanceFuncViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = AdvancedFuncFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //滚动与状态栏联动
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

        initTag()
        initAlias()
        initObserver()
        viewModel.initData()
        binding.tvCountLimit.setOnClickListener { showCountLimitDialog() }
        binding.tvAddTagEmpty.setOnClickListener { addTag() }
        binding.tvAlreadyAddTag.setOnClickListener { addTag() }
        binding.tvMoreDeviceTag.setOnClickListener { lookAllTag(LABEL_DEVICE_TAG) }
        binding.tvMoreAliasTag.setOnClickListener { lookAllTag(LABEL_ALIAS_TAG) }
        binding.tvMoreAccountTag.setOnClickListener { lookAllTag(LABEL_ACCOUNT_TAG) }
        binding.tvAliasMore.setOnClickListener { lookAllTag(LABEL_ALIAS) }
        binding.clAccount.setOnClickListener { showAccountInputDialog() }
        binding.clPhone.setOnClickListener { showPhoneInputDialog() }
        viewModel.showCustomToast = {message, icon ->
            showCustomToast(message, icon)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tvAddTagEmpty.layoutParams.width = (Resources.getSystem().displayMetrics.widthPixels - 80.toPx())/3
    }

    private fun initTag() {
        //设备标签
        binding.rvDeviceTag.apply {
            layoutManager =
                GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
            addItemDecoration(GridSpacingItemDecoration(3, 8.toDp()))
            deviceTagAdapter = LabelAdapter(mutableListOf()).apply {
                deleteLabelCallback = {
                    viewModel.removeDeviceTag(it)
                }
            }
            adapter = deviceTagAdapter
        }

        //别名标签
        binding.rvAliasTag.apply {
            layoutManager =
                GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
            addItemDecoration(GridSpacingItemDecoration(3, 8.toDp()))
            aliasTagAdapter = LabelAdapter(mutableListOf()).apply {
                deleteLabelCallback = {
                    viewModel.removeAliasTag(it)
                }
            }
            adapter = aliasTagAdapter
        }

        //账号标签
        binding.rvAccountTag.apply {
            layoutManager =
                GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
            addItemDecoration(GridSpacingItemDecoration(3, 8.toDp()))
            accountTagAdapter = LabelAdapter(mutableListOf()).apply {
                deleteLabelCallback = {
                    viewModel.removeAccountTag(it)
                }
            }
            adapter = accountTagAdapter
        }
    }

    private fun initAlias() {
        binding.rvAlias.apply {
            layoutManager =
                GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
            addItemDecoration(GridSpacingItemDecoration(3, 8.toDp()))
            aliasAdapter = AliasLabelAdapter(mutableListOf()).apply {
                deleteLabelCallback = {
                    viewModel.removeAlias(it)
                }
                addLabelCallback = {
                    addAlias()
                }
            }
            adapter = aliasAdapter
        }
    }

    private fun initObserver() {
        //设备别名数据改变监听
        viewModel.deviceTagData.observe(viewLifecycleOwner) {
            deviceTagAdapter?.labels?.clear()
            deviceTagAdapter?.labels?.addAll(
                if (true == viewModel.showMoreDeviceTag.value) viewModel.deviceTags.subList(
                    0,
                    viewModel.showMoreTagCount
                ) else viewModel.deviceTags
            )
            deviceTagAdapter?.notifyDataSetChanged()
        }

        //别名标签数据改变监听
        viewModel.aliasTagData.observe(viewLifecycleOwner) {
            aliasTagAdapter?.labels?.clear()
            aliasTagAdapter?.labels?.addAll(if (true == viewModel.showMoreAliasTag.value) viewModel.aliasTags.subList(
                0,
                viewModel.showMoreTagCount
            ) else viewModel.aliasTags)
            aliasTagAdapter?.notifyDataSetChanged()
        }

        //账号标签数据改变监听
        viewModel.accountTagData.observe(viewLifecycleOwner) {
            accountTagAdapter?.labels?.clear()
            accountTagAdapter?.labels?.addAll(if (true == viewModel.showMoreAccountTag.value) viewModel.accountTags.subList(
                0,
                viewModel.showMoreTagCount
            ) else viewModel.accountTags)
            accountTagAdapter?.notifyDataSetChanged()
        }

        //别名数据改变监听
        viewModel.aliasListStr.observe(viewLifecycleOwner) {
            val showAliasData = mutableListOf<String>().apply {
                if (true == viewModel.showMoreAlias.value){
                    addAll(viewModel.currAliasList.subList(0, viewModel.showMoreAliasCount))
                } else {
                    addAll(viewModel.currAliasList)
                }
                add(getString(R.string.push_add_alias))
            }
            aliasAdapter?.labels?.clear()
            aliasAdapter?.labels?.addAll(showAliasData)
            aliasAdapter?.notifyDataSetChanged()
        }
    }

    /**
     * 展示设备标签和别名数量限制说明dialog
     */
    private fun showCountLimitDialog() {
        val countLimitDialogBinding =
            CountLimitDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = BottomSheetDialog(requireContext(), R.style.RoundedBottomSheetDialog).apply {
            setContentView(countLimitDialogBinding.root)
            countLimitDialogBinding.lifecycleOwner = this
            show()
        }
        countLimitDialogBinding.ivClose.setOnClickListener { dialog.dismiss() }
    }

    //打开添加标签页面
    private fun addTag() {
        val intent = Intent(requireContext(), AddTagActivity::class.java)
        addTagLauncher.launch(intent)
    }

    //跳转查看全部标签页面
    private fun lookAllTag(type: Int) {
        val intent = Intent(requireContext(), AllLabelActivity::class.java).apply {
            putExtra("type", type)
        }
        requestDataLauncher.launch(intent)
    }

    //添加别名
    private fun addAlias() {
        requireContext().showInputDialog(
            R.string.push_add_alias, R.string.push_input_alias_hint,
            showAlert = true,
            showAliasInput = false
        ) { it, _ ->
            if (viewModel.alreadyAddAlias(it)) {
                Toast.makeText(requireContext(), getString(R.string.push_already_add), Toast.LENGTH_SHORT).show()
                return@showInputDialog
            }
            viewModel.addAlias(it)
        }
    }

    //账号输入弹窗弹出
    private fun showAccountInputDialog() {
        if (TextUtils.isEmpty(viewModel.account.value)) {
            requireContext().showInputDialog(
                R.string.push_bind_account, R.string.push_bind_account_hint,
                showAlert = false,
                showAliasInput = false
            ) { it, _ ->
                viewModel.bindAccount(it)
            }
        }else {
            requireContext().showBindDialog(
                R.string.push_bind_account, R.string.push_bind_account_hint, viewModel) {
                viewModel.bindAccount(it)
            }
        }

    }

    //手机号输入弹窗弹出
    private fun showPhoneInputDialog() {
        if (TextUtils.isEmpty(viewModel.phone.value)) {
            requireContext().showInputDialog(
                R.string.push_text_notification, R.string.push_text_notification_hint,
                inputMaxLength = 11,
                showAlert = false,
                showAliasInput = false
            ) { it, _ ->
                viewModel.bindPhone(it)
            }
        }else {
            requireContext().showBindDialog(
                R.string.push_text_notification, R.string.push_text_notification_hint,
                viewModel = viewModel,
                isBindAccount = false,
                inputMaxLength = 11) {
                viewModel.bindPhone(it)
            }
        }
    }
}