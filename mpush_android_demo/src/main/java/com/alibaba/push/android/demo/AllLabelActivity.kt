package com.alibaba.push.android.demo

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.push.android.demo.databinding.AllLabelActivityBinding
import com.alibaba.sdk.android.push.CloudPushService

/**
 * 全部标签页面
 * @author ren
 * @date 2024-10-31
 */
class AllLabelActivity : AppCompatActivity() {

    private lateinit var binding: AllLabelActivityBinding
    private lateinit var viewModel: AdvanceFuncViewModel

    private var labelType: Int = 0

    private var labelAdapter: LabelAdapter? = null

    //添加标签启动器,并接收返回的标签数据
    private val addTagLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.apply {
                    val tag = getStringExtra(KEY_TAG)
                    val target =
                        getIntExtra(KEY_TAG_TARGET_TYPE, CloudPushService.DEVICE_TARGET)
                    var alias: String? = null
                    if (target == CloudPushService.ALIAS_TARGET) {
                        alias = getStringExtra(KEY_ALIAS)
                    }
                    viewModel.addTag(tag, target, alias)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        //状态栏浸入
        controller.isAppearanceLightStatusBars = true
        //状态栏透明
        window.statusBarColor = Color.TRANSPARENT

        binding = AllLabelActivityBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this)[AdvanceFuncViewModel::class.java]
        viewModel.initData()
        setContentView(binding.root)

        labelType = intent.getIntExtra(TYPE, 0)

        initView()
        initObserver()
    }

    private fun initView(){

        binding.title = when (labelType) {
            LABEL_DEVICE_TAG -> getString(R.string.push_device_tag)
            LABEL_ALIAS_TAG -> getString(R.string.push_alias_tag)
            LABEL_ACCOUNT_TAG -> getString(R.string.push_account_tag)
            LABEL_ALIAS -> getString(R.string.push_alias_set)
            else -> getString(R.string.push_device_tag)
        }

        binding.tvAdd.text = if (labelType == LABEL_ALIAS) {
            getString(R.string.push_add_alias)
        }else {
            getString(R.string.push_add_tag)
        }

        binding.ivBack.setOnClickListener {
            back()
        }

        binding.rvLabel.apply {
            layoutManager =
                GridLayoutManager(this@AllLabelActivity, 3, RecyclerView.VERTICAL, false)
            addItemDecoration(GridSpacingItemDecoration(3, 8.toDp()))
            labelAdapter = LabelAdapter(mutableListOf()).apply {
                deleteLabelCallback = {
                    when (labelType) {
                        LABEL_DEVICE_TAG -> viewModel.removeDeviceTag(it)
                        LABEL_ALIAS_TAG -> viewModel.removeAliasTag(it)
                        LABEL_ACCOUNT_TAG -> viewModel.removeAccountTag(it)
                        LABEL_ALIAS -> viewModel.removeAlias(it)
                    }
                }
            }
            adapter = labelAdapter
        }

        binding.tvAdd.setOnClickListener {
            if (labelType == LABEL_ALIAS) addAlias() else addTag()
        }
    }

    private fun initObserver() {
        when (labelType) {
            LABEL_DEVICE_TAG -> viewModel.deviceTagData.observe(this) {
                updateLabels(viewModel.deviceTags)
            }
            LABEL_ALIAS_TAG -> viewModel.aliasTagData.observe(this) {
                updateLabels(viewModel.aliasTags)
            }
            LABEL_ACCOUNT_TAG -> viewModel.accountTagData.observe(this) {
                updateLabels(viewModel.accountTags)
            }
            LABEL_ALIAS -> viewModel.aliasListStr.observe(this) {
                updateLabels(viewModel.currAliasList)
            }

        }
    }

    private fun updateLabels(labels: MutableList<String>){
        labelAdapter?.labels?.clear()
        labelAdapter?.labels?.addAll(labels)
        labelAdapter?.notifyDataSetChanged()
    }

    private fun addTag(){
        val intent = Intent(this, AddTagActivity::class.java).apply {
            putExtra(KEY_TAG_TARGET_TYPE, when(labelType){
                LABEL_DEVICE_TAG -> CloudPushService.DEVICE_TARGET
                LABEL_ALIAS_TAG -> CloudPushService.ALIAS_TARGET
                LABEL_ACCOUNT_TAG -> CloudPushService.ACCOUNT_TARGET
                else -> CloudPushService.DEVICE_TARGET
            })
        }
        addTagLauncher.launch(intent)
    }

    private fun addAlias(){
        showInputDialog(
            R.string.push_add_alias, R.string.push_input_alias_hint,
            showAlert = true,
            showAliasInput = false
        ) { it, _ ->
            if (viewModel.alreadyAddAlias(it)) {
                Toast.makeText(this, getString(R.string.push_already_add), Toast.LENGTH_SHORT).show()
                return@showInputDialog
            }
            viewModel.addAlias(it)
        }
    }

    override fun onBackPressed() {
        back()
    }

    private fun back(){
        setResult(RESULT_OK, Intent().apply {
            putExtra(TYPE, labelType)
        })
        finish()
    }

}