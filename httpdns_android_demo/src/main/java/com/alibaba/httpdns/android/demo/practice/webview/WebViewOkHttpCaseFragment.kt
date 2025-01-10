package com.alibaba.httpdns.android.demo.practice.webview

import com.alibaba.httpdns.android.demo.BaseFragment
import alibaba.httpdns_android_demo.R
import alibaba.httpdns_android_demo.databinding.WebViewCaseBinding
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.alibaba.httpdns.android.demo.practice.ResolveResultViewModel
import com.alibaba.sdk.android.httpdns.HTTPDNSResult
import com.alibaba.sdk.android.httpdns.NetType
import com.alibaba.sdk.android.httpdns.net.HttpDnsNetworkDetector
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import okhttp3.Cookie
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.URL

/**
 * WebView最佳实践
 * @author 任伟
 * @date 2024/07/22
 */
class WebViewOkHttpCaseFragment : BaseFragment<WebViewCaseBinding>() {

    private lateinit var viewModel: ResolveResultViewModel
    private var url: String = ""

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(object: okhttp3.CookieJar {
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    val cookies = mutableListOf<Cookie>()
                    val cookieStr =  CookieManager.getInstance().getCookie(url.toString())
                    if (TextUtils.isEmpty(cookieStr)) {
                        return cookies
                    }
                    cookieStr.split(";").forEach {
                        Cookie.parse(url, it.trim())?.apply {
                            cookies.add(this)
                        }
                    }
                    return cookies
                }

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookies.forEach {
                        CookieManager.getInstance().setCookie(url.toString(), "${it.name}=${it.value}")
                    }
                    CookieManager.getInstance().flush()
                }

            })
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    val currMills = System.currentTimeMillis()
                    val inetAddresses = mutableListOf<InetAddress>()
                    viewModel.resolveSync(hostname)?.apply {
                        if (hostname == viewModel.host.value) {
                            viewModel.showResolveResult(this, currMills)
                        }
                        processDnsResult(this, inetAddresses)
                    }

                    if (inetAddresses.isEmpty()) {
                        try {
                            val localResolveResult = Dns.SYSTEM.lookup(hostname)
                            inetAddresses.addAll(localResolveResult)
                        }catch (e: Exception) {
                        }
                    }
                    return inetAddresses
                }
            })
            .build()
    }

    /**
     * 根据网络环境,返回ip
     */
    fun processDnsResult(httpDnsResult: HTTPDNSResult, inetAddresses: MutableList<InetAddress>) {
        val ipStackType = HttpDnsNetworkDetector.getInstance().getNetType(requireContext())
        val isV6 = ipStackType == NetType.v6 || ipStackType == NetType.both
        val isV4 = ipStackType == NetType.v4 || ipStackType == NetType.both

        if (httpDnsResult.ipv6s != null && httpDnsResult.ipv6s.isNotEmpty() && isV6) {
            for (i in httpDnsResult.ipv6s.indices) {
                inetAddresses.addAll(
                    InetAddress.getAllByName(httpDnsResult.ipv6s[i]).toList()
                )
            }
        } else if (httpDnsResult.ips != null && httpDnsResult.ips.isNotEmpty() && isV4) {
            for (i in httpDnsResult.ips.indices) {
                inetAddresses.addAll(
                    InetAddress.getAllByName(httpDnsResult.ips[i]).toList()
                )
            }
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.httpdns_fragment_webview_case
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[ResolveResultViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.tlWebViewCase.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.llResolveResult.isVisible = getString(R.string.resolve_result) == tab?.text
                binding.httpdnsWebview.isVisible = getString(R.string.content_show) == tab?.text
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

        })
        binding.tvLoad.setOnClickListener {
            clickLoad()
        }
        binding.tvInputAgain.setOnClickListener {
            binding.llInput.isVisible = true
            binding.clLoadContent.isVisible = false
        }
        binding.httpdnsWebview.webViewClient = object : WebViewClient() {

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                if (shouldIntercept(request)) {
                    return getResponseByOkHttp(request)
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    private fun shouldIntercept(webResourceRequest: WebResourceRequest?): Boolean {
        if (webResourceRequest == null) {
            return false
        }
        val url = webResourceRequest.url ?: return false
        if (!"GET".equals(webResourceRequest.method, true)) {
            return false
        }

        if ("https" == url.scheme || "http" == url.scheme) {
            return true
        }
        return false
    }

    private fun getResponseByOkHttp(webResourceRequest: WebResourceRequest?): WebResourceResponse? {
        if (webResourceRequest == null) {return null}
        try {
            val url = webResourceRequest.url.toString()
            val requestBuilder =
                Request.Builder().url(url).method(webResourceRequest.method, null)
            val requestHeaders = webResourceRequest.requestHeaders
            if (!requestHeaders.isNullOrEmpty()) {
                requestHeaders.forEach {
                    requestBuilder.addHeader(it.key, it.value)
                }
            }
            val response = okHttpClient.newCall(requestBuilder.build()).execute()
            val code = response.code
            if (code != 200) {
                return null
            }
            val body = response.body
            if (body != null) {
                val contentType = body.contentType()
                val encoding = contentType?.charset()
                val mediaType = contentType?.toString()
                var mimeType = "text/plain"
                if (!TextUtils.isEmpty(mediaType)) {
                    val mediaTypeElements = mediaType?.split(";")
                    if (!mediaTypeElements.isNullOrEmpty()) {
                        mimeType = mediaTypeElements[0]
                    }
                }
                val responseHeaders = mutableMapOf<String, String>()
                for (header in response.headers) {
                    responseHeaders[header.first] = header.second
                }
                var message = response.message
                if (message.isBlank()) {
                    message = "OK"
                }
                val resourceResponse =
                    WebResourceResponse(mimeType, encoding?.name(), body.byteStream())
                resourceResponse.responseHeaders = responseHeaders
                resourceResponse.setStatusCodeAndReasonPhrase(code, message)
                return resourceResponse
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    private fun clickLoad() {
        val urlStr = binding.resolveHostInputLayout.editText?.text?.trim().toString()
        if (TextUtils.isEmpty(urlStr)) {
            Toast.makeText(context, getString(R.string.toast_url_empty), Toast.LENGTH_SHORT).show()
            return
        }

        val url = URL(urlStr)
        val host = url.host
        if (TextUtils.isEmpty(host)) {
            Toast.makeText(context, getString(R.string.toast_url_mistake), Toast.LENGTH_SHORT).show()
            return
        }

        if (urlStr != this.url) {
            viewModel.host.value = host
            binding.httpdnsWebview.loadUrl(urlStr)
        }
        this.url = urlStr
        binding.llInput.isVisible = false
        binding.clLoadContent.isVisible = true
        binding.tlWebViewCase.selectTab(binding.tlWebViewCase.getTabAt(0))
    }
}