package com.alibaba.httpdns.android.demo.practice.okhttp

import alibaba.httpdns_android_demo.R
import android.app.Application
import android.widget.RadioGroup
import androidx.lifecycle.viewModelScope
import com.alibaba.httpdns.android.demo.HttpDnsApplication
import com.alibaba.httpdns.android.demo.SCHEME_HTTP
import com.alibaba.httpdns.android.demo.SCHEME_HTTPS
import com.alibaba.httpdns.android.demo.SingleLiveData
import com.alibaba.httpdns.android.demo.URL_DIVIDE
import com.alibaba.httpdns.android.demo.log
import com.alibaba.httpdns.android.demo.practice.ResolveResultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * okhttp + httpdns 最佳实践的VM
 * @author 任伟
 * @date 2024/07/23
 */
class OkHttpCaseViewModel(application: Application) : ResolveResultViewModel(application) {

    private var schemaType: SchemaType = SchemaType.HTTPS

    val path = SingleLiveData<String>().apply { value = "/example-resources.txt" }

    val showRequestAndResolveResult = SingleLiveData<Boolean>().apply { value = false }

    private lateinit var okHttpClient: OkHttpClient

    val responseStr = SingleLiveData<String>().apply { value = "" }

    private var response: Response? = null
    fun initData() {
        host.value = "ams-sdk-public-assets.oss-cn-hangzhou.aliyuncs.com"
        okHttpClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 10 * 1000, TimeUnit.MICROSECONDS))
            .hostnameVerifier { _, _ -> true }
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    val currMills = System.currentTimeMillis()
                    val inetAddresses = mutableListOf<InetAddress>()
                    resolveSync(host.value)?.apply {
                        showResolveResult(this, currMills)
                        processDnsResult(this, inetAddresses)
                        log(
                            this@OkHttpCaseViewModel,
                            String.format(
                                getApplication<HttpDnsApplication>().getString(R.string.log_resolve_result),
                                hostname,
                                this
                            )
                        )
                    }

                    if (inetAddresses.isEmpty()) {
                        log(
                            this@OkHttpCaseViewModel,
                            getApplication<HttpDnsApplication>().getString(R.string.log_dns_resolve_fail)
                        )
                        try {
                            val localResolveResult = Dns.SYSTEM.lookup(hostname)
                            inetAddresses.addAll(localResolveResult)
                        }catch (e: Exception) {
                            log(
                                this@OkHttpCaseViewModel,
                                String.format(
                                    getApplication<HttpDnsApplication>().getString(R.string.log_host_cannnot_resolve),
                                    hostname
                                )
                            )
                        }
                    }
                    return inetAddresses
                }
            })
            .build()
    }

    /**
     * 请求协议改变
     */
    fun onSchemaTypeChanged(radioGroup: RadioGroup, id: Int) {
        schemaType = when (id) {
            R.id.schema_http -> SchemaType.HTTP
            else -> SchemaType.HTTPS
        }
    }

    /**
     * 请求并解析
     */
    fun requestAndResolve() {
        if (path.value?.startsWith(URL_DIVIDE) != true) {
            path.value = "$URL_DIVIDE${path.value}"
        }
        val requestUrl =
            "${if (schemaType == SchemaType.HTTPS) SCHEME_HTTPS else SCHEME_HTTP}${host.value}${path.value}"
        log(
            this,
            String.format(
                getApplication<HttpDnsApplication>().getString(R.string.log_okhttp_request_url),
                requestUrl
            )
        )
        viewModelScope.launch(Dispatchers.IO) {
            val request = Request.Builder().url(requestUrl).build()
            runCatching {
                okHttpClient.newCall(request).execute().use {
                    val bodyStr = it.body?.string()
                    log(
                        this@OkHttpCaseViewModel,
                        bodyStr
                            ?: getApplication<HttpDnsApplication>().getString(R.string.log_okhttp_request_fail)
                    )
                    val code = it.code
                    responseStr.postValue("$code  $bodyStr")
                    showRequestAndResolveResult.postValue(true)
                    response = Response(code, bodyStr)
                }
            }
        }
    }

    /**
     * 重新输入
     */
    fun reInput() {
        showRequestAndResolveResult.value = false
    }
}