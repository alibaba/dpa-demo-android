package com.alibaba.httpdns.android.demo.practice

import com.alibaba.httpdns.android.demo.BaseFragment
import alibaba.httpdns_android_demo.R
import alibaba.httpdns_android_demo.databinding.CaseBinding
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.httpdns.android.demo.view.CaseItemDecoration

/**
 * 最佳实践页面
 * @author 任伟
 * @date 2024/07/22
 */
class CaseFragment : BaseFragment<CaseBinding>() {

    private lateinit var viewModel: CaseViewModel

    override fun getLayoutId(): Int {
        return R.layout.httpdns_fragment_case
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[CaseViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvCase.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCase.addItemDecoration(CaseItemDecoration())
        binding.rvCase.adapter = CaseAdapter(mutableMapOf<Int, String>().apply {
            put(R.id.navigation_okhttp, getString(R.string.okhttp_case))
            put(R.id.navigation_web_okhttp, getString(R.string.web_okhttp_case))
            put(R.id.navigation_webview, getString(R.string.webview_case))
            put(R.id.navigation_ip_conn, getString(R.string.ip_conn_case))
            put(R.id.navigation_exo_player, getString(R.string.exo_player_case))
        }.toList()).apply {
            onItemClickListener = ::changeFragment
            selectItem = viewModel.currCaseIndex.value ?: 0
        }
    }

    /**
     * 最佳实践fragment切换
     */
    private fun changeFragment(id: Int, position: Int) {
        viewModel.currCaseIndex.value = position
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment_case) as NavHostFragment
        navHostFragment.navController.navigate(id)
    }
}