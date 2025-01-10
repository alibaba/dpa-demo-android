package com.alibaba.httpdns.android.demo.practice.webview

import com.alibaba.httpdns.android.demo.BaseFragment
import alibaba.httpdns_android_demo.R
import alibaba.httpdns_android_demo.databinding.WebViewCaseBinding
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.alibaba.httpdns.android.demo.HTTP_COOKIE
import com.alibaba.httpdns.android.demo.HTTP_REQUEST_GET
import com.alibaba.httpdns.android.demo.HTTP_SCHEME_HEADER_HOST
import com.alibaba.httpdns.android.demo.LOCATION
import com.alibaba.httpdns.android.demo.LOCATION_UP
import com.alibaba.httpdns.android.demo.SCHEME_DIVIDE
import com.alibaba.httpdns.android.demo.SCHEME_HTTP
import com.alibaba.httpdns.android.demo.SCHEME_HTTPS
import com.alibaba.httpdns.android.demo.log
import com.alibaba.httpdns.android.demo.practice.ResolveResultViewModel
import com.alibaba.sdk.android.httpdns.RequestIpType
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

/**
 * WebView最佳实践
 * @author 任伟
 * @date 2024/07/22
 */
class WebViewCaseFragment : BaseFragment<WebViewCaseBinding>() {

    private lateinit var viewModel: ResolveResultViewModel
    private var url: String = ""
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
                val url = request?.url.toString()
                val schema = request?.url?.scheme?.trim()
                val method = request?.method
                //WebResourceRequest 不包含body信息 , 所以只拦截Get请求
                if (!HTTP_REQUEST_GET.equals(method, true)) {
                    return super.shouldInterceptRequest(view, request)
                }

                schema?.let {
                    //非http协议不拦截
                    if (!SCHEME_HTTPS.startsWith(schema) && !SCHEME_HTTP.startsWith(schema)) {
                        return super.shouldInterceptRequest(view, request)
                    }
                    val headers = request.requestHeaders
                    try {
                        //获取资源失败不拦截,还是走原来的请求
                        val urlConnection = recursiveRequest(url, headers)
                            ?: return super.shouldInterceptRequest(view, request)
                        //实例化 WebResourceResponse 需要入参 mimeType
                        val contentType = urlConnection.contentType
                        val mimeType = contentType?.split(";")?.get(0)
                        //实例化 WebResourceResponse 需要入参 charset
                        val charset = getCharset(contentType)
                        val httpURLConnection = urlConnection as HttpURLConnection
                        val statusCode = httpURLConnection.responseCode
                        var response = httpURLConnection.responseMessage
                        val headerFields = httpURLConnection.headerFields

                        val resourceResponse = WebResourceResponse(
                            mimeType,
                            charset,
                            httpURLConnection.inputStream
                        )
                        if (TextUtils.isEmpty(response)) {
                            response = "OK"
                        }
                        //设置statusCode 和 response
                        resourceResponse.setStatusCodeAndReasonPhrase(statusCode, response)
                        //构造响应头
                        val responseHeader: MutableMap<String?, String> = HashMap()
                        for ((key) in headerFields) {
                            // HttpUrlConnection可能包含key为null的报头，指向该http请求状态码
                            responseHeader[key] = httpURLConnection.getHeaderField(key)
                        }
                        resourceResponse.responseHeaders = responseHeader
                        return resourceResponse

                    } catch (e: Exception) {
                        log(this@WebViewCaseFragment, e.message)
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
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
            Toast.makeText(context, getString(R.string.toast_url_mistake), Toast.LENGTH_SHORT)
                .show()
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

    private fun getCharset(contentType: String?): String? {
        if (contentType == null) {
            return null
        }
        val fields = contentType.split(";")
        if (fields.size <= 1) {
            return null
        }
        var charset = fields[1]
        if (!charset.contains("=")) {
            return null
        }
        charset = charset.substring(charset.indexOf("=") + 1)
        return charset
    }

    private fun recursiveRequest(path: String, headers: Map<String, String>?): URLConnection? {
        try {
            val url = URL(path)
            val currMill = System.currentTimeMillis()
            val result =
                viewModel.httpDnsService?.getHttpDnsResultForHostSync(url.host, RequestIpType.auto)
                    ?.apply {
                        log(this@WebViewCaseFragment, this.toString())
                        if (url.host == viewModel.host.value) {
                            //展示loadUrl的解析结果
                            viewModel.showResolveResult(this, currMill)
                        }
                    }

            val hostIP = viewModel.getIp(result)
            if (TextUtils.isEmpty(hostIP)) {
                return null
            }

            //host 替换为 ip 之后的 url
            val newUrl = path.replaceFirst(url.host, hostIP!!)
            val urlConnection: HttpURLConnection = URL(newUrl).openConnection() as HttpURLConnection
            if (headers != null) {
                for ((key, value) in headers) {
                    urlConnection.setRequestProperty(key, value)
                }
            }
            //用于证书校验
            urlConnection.setRequestProperty(HTTP_SCHEME_HEADER_HOST, url.host)
            urlConnection.connectTimeout = 30000
            urlConnection.readTimeout = 30000
            //禁止重定向,抛出异常,通过异常code , 处理重定向
            urlConnection.instanceFollowRedirects = false
            if (urlConnection is HttpsURLConnection) {
                //https场景 , 证书校验
                val sniFactory = SNISocketFactory(urlConnection)
                urlConnection.sslSocketFactory = sniFactory
                urlConnection.hostnameVerifier = HostnameVerifier { _, session ->
                    var host: String? = urlConnection.getRequestProperty(HTTP_SCHEME_HEADER_HOST)
                    if (null == host) {
                        host = urlConnection.getURL().host
                    }
                    HttpsURLConnection.getDefaultHostnameVerifier().verify(host, session)
                }
            }

            val responseCode = urlConnection.responseCode
            if (responseCode in 300..399) {
                //有缓存, 不发起请求, 没有解析必要 , 返回空
                if (containCookie(headers)) {
                    return null
                }
                //处理重定向逻辑
                var location: String? = urlConnection.getHeaderField(LOCATION_UP)
                if (location == null) {
                    location = urlConnection.getHeaderField(LOCATION)
                }

                return if (location != null) {
                    if (!(location.startsWith(SCHEME_HTTP) || location.startsWith(SCHEME_HTTPS))) {
                        //某些时候会省略host，只返回后面的path，所以需要补全url
                        val originalUrl = URL(path)
                        location =
                            (originalUrl.protocol + SCHEME_DIVIDE + originalUrl.host + location)
                    }
                    recursiveRequest(location, headers)
                } else {
                    null
                }
            } else {
                return urlConnection
            }
        } catch (e: MalformedURLException) {
            log(this, e.message)
        } catch (e: IOException) {
            log(this, e.message)
        }
        return null
    }

    private fun containCookie(headers: Map<String, String>?): Boolean {
        if (headers == null) {
            return false
        }
        for ((key) in headers) {
            if (key.contains(HTTP_COOKIE)) {
                return true
            }
        }
        return false
    }


}