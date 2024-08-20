package com.alibaba.httpdns.android.demo.practice.exoplayer

import alibaba.httpdns_android_demo.R
import android.app.Application
import com.alibaba.httpdns.android.demo.HttpDnsApplication
import com.alibaba.httpdns.android.demo.log
import com.alibaba.httpdns.android.demo.practice.ResolveResultViewModel
import okhttp3.ConnectionPool
import okhttp3.Dns
import okhttp3.OkHttpClient
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * ExoPlayer最佳实践的VM
 * @author 任伟
 * @date 2024/07/25
 */
class ExoPlayerCaseViewModel(application: Application) : ResolveResultViewModel(application) {

    val playerUrl = "https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd"

    lateinit var okHttpClient: OkHttpClient

    fun initData() {
        host.value = URL(playerUrl).host

        okHttpClient = OkHttpClient.Builder()
            .connectionPool(ConnectionPool(0, 10 * 1000, TimeUnit.MICROSECONDS))
            .hostnameVerifier { _, _ -> true }
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    val currMills = System.currentTimeMillis()
                    val inetAddresses = mutableListOf<InetAddress>()
                    resolveSync(host.value!!)?.apply {
                        showResolveResult(this, currMills)
                        processDnsResult(this, inetAddresses)
                        log(
                            this@ExoPlayerCaseViewModel,
                            String.format(
                                getApplication<HttpDnsApplication>().getString(R.string.log_resolve_result),
                                hostname,
                                this
                            )
                        )
                    }

                    if (inetAddresses.isEmpty()) {
                        log(
                            this@ExoPlayerCaseViewModel,
                            getApplication<HttpDnsApplication>().getString(R.string.log_dns_resolve_fail)
                        )
                        return Dns.SYSTEM.lookup(hostname)
                    }
                    return inetAddresses
                }
            })
            .build()
    }


}