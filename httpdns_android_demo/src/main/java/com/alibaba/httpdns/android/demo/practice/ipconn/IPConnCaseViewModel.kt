package com.alibaba.httpdns.android.demo.practice.ipconn

import alibaba.httpdns_android_demo.R
import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.alibaba.httpdns.android.demo.HTTP_SCHEME_HEADER_HOST
import com.alibaba.httpdns.android.demo.HttpDnsApplication
import com.alibaba.httpdns.android.demo.LOCATION
import com.alibaba.httpdns.android.demo.LOCATION_UP
import com.alibaba.httpdns.android.demo.SCHEME_DIVIDE
import com.alibaba.httpdns.android.demo.SCHEME_HTTP
import com.alibaba.httpdns.android.demo.SCHEME_HTTPS
import com.alibaba.httpdns.android.demo.SingleLiveData
import com.alibaba.httpdns.android.demo.log
import com.alibaba.httpdns.android.demo.practice.ResolveResultViewModel
import com.alibaba.httpdns.android.demo.readStringFrom
import com.alibaba.sdk.android.httpdns.RequestIpType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection

/**
 * IP直连的VM
 * @author 任伟
 * @date 2024/07/24
 */
class IPConnCaseViewModel(application: Application) : ResolveResultViewModel(application) {

    val responseStr = SingleLiveData<String>().apply { value = "" }

    val ipConnUrl = SingleLiveData<String>().apply {
        value = "https://ams-sdk-public-assets.oss-cn-hangzhou.aliyuncs.com/example-resources.txt"
    }

    val showRequestAndResolveResult = SingleLiveData<Boolean>().apply { value = false }

    fun sniRequest() {
        host.value = URL(ipConnUrl.value).host
        showRequestAndResolveResult.value = true
        viewModelScope.launch(Dispatchers.IO) {
            recursiveRequest(ipConnUrl.value!!) { message ->
                responseStr.postValue(message)
            }
        }
    }

    private suspend fun recursiveRequest(url: String, callback: suspend (message: String) -> Unit) {
        val host = URL(url).host
        val ipURL: String?

        val currMill = System.currentTimeMillis()
        //解析
        val result = httpDnsService?.getHttpDnsResultForHostSync(host, RequestIpType.auto)?.apply {
            log(this@IPConnCaseViewModel, this.toString())
            showResolveResult(this, currMill)
        }

        val hostIP = getIp(result)
        if (TextUtils.isEmpty(hostIP)) {
            return
        }

        ipURL = url.replace(host, hostIP!!)
        val conn: HttpsURLConnection =
            URL(ipURL).openConnection() as HttpsURLConnection
        conn.setRequestProperty(HTTP_SCHEME_HEADER_HOST, host)
        conn.connectTimeout = 30000
        conn.readTimeout = 30000
        conn.instanceFollowRedirects = false

        //设置SNI
        val sslSocketFactory = TLSSNISocketFactory(conn)
        // SNI场景，创建SSLSocket
        conn.sslSocketFactory = sslSocketFactory
        conn.hostnameVerifier = HostnameVerifier { _, session ->
            val requestHost = conn.getRequestProperty(HTTP_SCHEME_HEADER_HOST) ?: conn.url.host
            HttpsURLConnection.getDefaultHostnameVerifier().verify(requestHost, session)
        }
        val code = conn.responseCode
        if (needRedirect(code)) {
            //临时重定向和永久重定向location的大小写有区分
            var location = conn.getHeaderField(LOCATION_UP)
            if (location == null) {
                location = conn.getHeaderField(LOCATION)
            }
            if (!(location!!.startsWith(SCHEME_HTTP) || location.startsWith(SCHEME_HTTPS))) {
                //某些时候会省略host，只返回后面的path，所以需要补全url
                val originalUrl = URL(url)
                location = (originalUrl.protocol + SCHEME_DIVIDE
                        + originalUrl.host + location)
            }
            recursiveRequest(location, callback)
        } else {
            val inputStream: InputStream?
            val streamReader: BufferedReader?
            if (code != HttpURLConnection.HTTP_OK) {
                inputStream = conn.errorStream
                var errMsg: String? = null
                if (inputStream != null) {
                    streamReader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                    errMsg = readStringFrom(streamReader).toString()
                }
                log(
                    this@IPConnCaseViewModel,
                    String.format(
                        getApplication<HttpDnsApplication>().getString(R.string.log_sni_request_error),
                        errMsg
                    )
                )
                callback("$code - $errMsg")
            } else {
                inputStream = conn.inputStream
                streamReader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                val body: String = readStringFrom(streamReader).toString()
                log(
                    this@IPConnCaseViewModel,
                    String.format(
                        getApplication<HttpDnsApplication>().getString(R.string.log_sni_request_response),
                        body
                    )
                )
                callback("$code - $body")
            }
        }
    }

    private fun needRedirect(code: Int): Boolean {
        return code in 300..399
    }

    fun reInput() {
        showRequestAndResolveResult.value = false
    }

}