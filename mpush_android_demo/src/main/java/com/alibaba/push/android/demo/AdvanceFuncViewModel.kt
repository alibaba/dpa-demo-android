package com.alibaba.push.android.demo

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alibaba.sdk.android.push.CloudPushService
import com.alibaba.sdk.android.push.CommonCallback
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class AdvanceFuncViewModel(application: Application) : AndroidViewModel(application) {

    val showMoreTagCount = 9

    val showMoreAliasCount = 8

    val hasTag = SingleLiveData<Boolean>().apply { value = false }

    val hasDeviceTag = SingleLiveData<Boolean>().apply { value = false }
    val showMoreDeviceTag = SingleLiveData<Boolean>().apply { value = false }
    val deviceTagData = SingleLiveData<String>().apply { value = "" }
    val deviceTags = mutableListOf<String>()

    val hasAliasTag = SingleLiveData<Boolean>().apply { value = false }
    val showMoreAliasTag = SingleLiveData<Boolean>().apply { value = false }
    val aliasTagData = SingleLiveData<String>().apply { value = "" }
    val aliasTags = mutableListOf<String>()
    private var aliasTagMap = mutableMapOf<String, String>()

    val hasAccountTag = SingleLiveData<Boolean>().apply { value = false }
    val showMoreAccountTag = SingleLiveData<Boolean>().apply { value = false }
    val accountTagData = SingleLiveData<String>().apply { value = "" }
    val accountTags = mutableListOf<String>()

    val showMoreAlias = SingleLiveData<Boolean>().apply { value = false }
    val aliasListStr = SingleLiveData<String?>().apply { value = "" }
    val currAliasList = mutableListOf<String>()


    val account = SingleLiveData<String?>()

    val phone = SingleLiveData<String?>()

    val showDividerDeviceTag = SingleLiveData<Boolean>().apply { value = false }

    val showDividerAliasTag = SingleLiveData<Boolean>().apply { value = false }

    private val gson = Gson()

    val preferences: SharedPreferences = getApplication<MainApplication>().getSharedPreferences(
        SP_FILE_NAME,
        Context.MODE_PRIVATE
    )

    var showCustomToast: ((String, Int) -> Unit)? = null

    fun initData() {
        getDeviceTagFromServer()
        getAliasTagFromSp()
        getAccountTagFromSp()
        getAliasListFromServer()
        viewModelScope.launch {
            account.value =
                preferences.getString(SP_KEY_BIND_ACCOUNT, "")
            phone.value =
                preferences.getString(SP_KEY_BIND_PHONE, "")
        }
    }

    /**
     * 获取设备标签
     */
    fun getDeviceTagFromServer() {
        PushServiceFactory.getCloudPushService()
            .listTags(CloudPushService.DEVICE_TARGET, object : CommonCallback {
                override fun onSuccess(response: String?) {
                    deviceTags.clear()
                    if (TextUtils.isEmpty(response)){
                        updateTagStatus()
                        deviceTagData.value = ""
                    }else {
                        response?.let {
                            it.split(",").forEach { deviceTag ->
                                deviceTags.add(0, deviceTag)
                            }
                            updateTagStatus()
                            deviceTagData.value = response
                        }
                    }
                }

                override fun onFailed(p0: String?, errorMessage: String?) {
                    showCustomToast?.invoke(String.format(getString(R.string.push_get_device_tag_list_fail), errorMessage), R.drawable.push_fail)
                }
            })
    }

    /**
     * 获取别名标签
     */
    fun getAliasTagFromSp() {
        val spAliasTagData = preferences.getString(SP_KEY_ALIAS_TAG, "")
        aliasTags.clear()
        if (TextUtils.isEmpty(spAliasTagData)) {
            updateTagStatus()
            aliasTagData.value = ""
        }else {
            spAliasTagData!!.split(",").forEach {
                aliasTags.add(it)
            }
            updateTagStatus()
            aliasTagData.value = spAliasTagData
            val spAliasTagMap = preferences.getString(SP_KEY_ALIAS_TAG_MAP, "")
            aliasTagMap =
                gson.fromJson(spAliasTagMap, object : TypeToken<Map<String, String>>() {}.type)
        }
    }

    /**
     * 获取账号标签
     */
    fun getAccountTagFromSp() {
        val spAccountTagData = preferences.getString(SP_KEY_ACCOUNT_TAG, "")
        accountTags.clear()
        if (TextUtils.isEmpty(spAccountTagData)) {
            updateTagStatus()
            accountTagData.value = ""
        }else {
            spAccountTagData!!.split(",").forEach {
                accountTags.add(it)
            }
            updateTagStatus()
            accountTagData.value = spAccountTagData
        }
    }

    /**
     * 获取别名列表
     */
    fun getAliasListFromServer() {
        PushServiceFactory.getCloudPushService().listAliases(object : CommonCallback {
            override fun onSuccess(response: String?) {
                currAliasList.clear()
                if (TextUtils.isEmpty(response)) {
                    showMoreAlias.value = currAliasList.size > showMoreAliasCount
                    aliasListStr.value = response
                }else {
                    response!!.split(",").reversed().forEach { alias ->
                        currAliasList.add(alias)
                    }
                    showMoreAlias.value = currAliasList.size > showMoreAliasCount
                    aliasListStr.value = response
                }
            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                showCustomToast?.invoke(String.format(getString(R.string.push_get_alias_list_fail), errorMessage), R.drawable.push_fail)
            }

        })
    }

    /**
     * 添加标签
     */
    fun addTag(tag: String?, target: Int, alias: String?) {

        if (TextUtils.isEmpty(tag) || tag == null) return

        if ((target == CloudPushService.DEVICE_TARGET && deviceTags.contains(tag))
            || (target == CloudPushService.ALIAS_TARGET && aliasTags.contains(tag))
            || (target == CloudPushService.ACCOUNT_TARGET && accountTags.contains(tag))
        ) {
            showCustomToast?.invoke(getString(R.string.push_already_add), R.drawable.push_fail)
            return
        }
        PushServiceFactory.getCloudPushService()
            .bindTag(target, arrayOf(tag), alias, object : CommonCallback {
                override fun onSuccess(p0: String?) {
                    hasTag.value = true
                    when (target) {
                        CloudPushService.DEVICE_TARGET -> addDeviceTagSuccess(tag)
                        CloudPushService.ALIAS_TARGET -> addAliasTagSuccess(tag, alias)
                        CloudPushService.ACCOUNT_TARGET -> addAccountTagSuccess(tag)
                    }
                }

                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    showCustomToast?.invoke(String.format(getString(R.string.push_toast_add_tag_fail), errorMessage), R.drawable.push_fail)
                }

            })
    }

    //添加设备标签成功,更新状态
    private fun addDeviceTagSuccess(tag: String) {
        deviceTags.add(0, tag)
        updateTagStatus()
        deviceTagData.value = deviceTags.joinToString(",")
    }

    //添加别名标签成功,更新状态
    private fun addAliasTagSuccess(tag: String, alias: String?) {
        if (TextUtils.isEmpty(alias)) return
        aliasTags.add(0, tag)
        updateTagStatus()
        aliasTagData.value = aliasTags.joinToString(",")
        if (aliasTagMap.containsKey(alias)) {
            aliasTagMap[alias!!] = "${aliasTagMap[alias]},$tag"
        } else {
            aliasTagMap[alias!!] = tag
        }
        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putString(SP_KEY_ALIAS_TAG, aliasTagData.value)
            editor.putString(SP_KEY_ALIAS_TAG_MAP, gson.toJson(aliasTagMap))
            editor.apply()
        }
    }

    //添加账号标签成功,更新状态
    private fun addAccountTagSuccess(tag: String) {
        accountTags.add(0, tag)
        updateTagStatus()
        accountTagData.value = accountTags.joinToString(",")
        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putString(SP_KEY_ACCOUNT_TAG, accountTagData.value)
            editor.apply()
        }
    }

    /**
     * 移除设备标签
     */
    fun removeDeviceTag(tag: String) {
        PushServiceFactory.getCloudPushService()
            .unbindTag(CloudPushService.DEVICE_TARGET, arrayOf(tag), null, object : CommonCallback {
                override fun onSuccess(p0: String?) {
                    removeDeviceTagSuccess(tag)
                }

                override fun onFailed(p0: String?, errorMessage: String?) {
                    showCustomToast?.invoke(String.format(getString(R.string.push_toast_delete_device_tag_fail), errorMessage), R.drawable.push_fail)
                }

            })
    }

    //移除设备标签成功,更新状态
    private fun removeDeviceTagSuccess(tag: String) {
        deviceTags.remove(tag)
        updateTagStatus()
        deviceTagData.value = deviceTags.joinToString(",")
        showCustomToast?.invoke(getString(R.string.push_toast_delete_tag_success), R.drawable.push_success)
    }

    /**
     * 移除别名标签
     */
    fun removeAliasTag(tag: String) {
        for (entry in aliasTagMap) {
            if (entry.value.contains(tag)) {
                PushServiceFactory.getCloudPushService()
                    .unbindTag(CloudPushService.ALIAS_TARGET, arrayOf(tag), entry.key, object : CommonCallback {
                        override fun onSuccess(p0: String?) {
                            removeAliasTagSuccess(tag, entry.key)
                        }

                        override fun onFailed(p0: String?, errorMessage: String?) {
                            showCustomToast?.invoke(String.format(getString(R.string.push_toast_delete_alias_tag_fail), errorMessage), R.drawable.push_fail)
                        }

                    })
                break
            }
        }
    }

    //移除别名标签成功,更新状态
    private fun removeAliasTagSuccess(tag: String, alias: String) {
        aliasTags.remove(tag)
        updateTagStatus()
        aliasTagData.value = aliasTags.joinToString(",")

        aliasTagMap[alias]?.split(",")?.let {
            val aliasTagList = it.toMutableList()
            aliasTagList.remove(tag)
            aliasTagMap[alias] = aliasTagList.joinToString(",")
        }

        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putString(SP_KEY_ALIAS_TAG, aliasTagData.value)
            editor.putString(SP_KEY_ALIAS_TAG_MAP, gson.toJson(aliasTagMap))
            editor.apply()
        }
        showCustomToast?.invoke(getString(R.string.push_toast_delete_tag_success), R.drawable.push_success)
    }

    /**
     * 移除账号标签
     */
    fun removeAccountTag(tag: String) {
        PushServiceFactory.getCloudPushService()
            .unbindTag(CloudPushService.ACCOUNT_TARGET, arrayOf(tag), null, object : CommonCallback {
                override fun onSuccess(p0: String?) {
                    removeAccountTagSuccess(tag)
                }

                override fun onFailed(p0: String?, errorMessage: String?) {
                    showCustomToast?.invoke(String.format(getString(R.string.push_unbind_account_tag_fail), errorMessage), R.drawable.push_fail)
                }

            })
    }

   //移除账号标签成功,更新状态
    private fun removeAccountTagSuccess(tag: String) {
        accountTags.remove(tag)
        updateTagStatus()
        accountTagData.value = accountTags.joinToString(",")
        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putString(SP_KEY_ACCOUNT_TAG, accountTagData.value)
            editor.apply()
        }
       showCustomToast?.invoke(getString(R.string.push_toast_delete_tag_success), R.drawable.push_success)
    }

    /**
     * 更新UI状态
     */
    private fun updateTagStatus(){
        hasTag.value = deviceTags.isNotEmpty() || aliasTags.isNotEmpty() || accountTags.isNotEmpty()
        hasDeviceTag.value = deviceTags.isNotEmpty()
        showMoreDeviceTag.value = deviceTags.size > showMoreTagCount
        showDividerDeviceTag.value = deviceTags.isNotEmpty() && (aliasTags.isNotEmpty() || accountTags.isNotEmpty())

        hasAliasTag.value = aliasTags.isNotEmpty()
        showMoreAliasTag.value = aliasTags.size > showMoreTagCount
        showDividerAliasTag.value = aliasTags.isNotEmpty() && accountTags.isNotEmpty()

        hasAccountTag.value = accountTags.isNotEmpty()
        showMoreAccountTag.value = accountTags.size > showMoreTagCount
    }

    /**
     * 添加别名
     */
    fun addAlias(alias: String) {
        PushServiceFactory.getCloudPushService().addAlias(alias, object : CommonCallback {
            override fun onSuccess(p0: String?) {
                currAliasList.add(0, alias)
                showMoreAlias.value = currAliasList.size > showMoreAliasCount
                aliasListStr.value = currAliasList.joinToString(",")
            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                showCustomToast?.invoke(String.format(getString(R.string.push_toast_add_alias_fail), errorMessage), R.drawable.push_fail)
            }
        })
    }

    /**
     * 移除别名
     */
    fun removeAlias(alias: String) {
        PushServiceFactory.getCloudPushService().removeAlias(alias, object : CommonCallback {
            override fun onSuccess(p0: String?) {
                updateTagAfterRemoveAlias(alias)
                currAliasList.remove(alias)
                showMoreAlias.value = currAliasList.size > showMoreAliasCount
                aliasListStr.value = currAliasList.joinToString(",")
                showCustomToast?.invoke(getString(R.string.push_toast_delete_alias_success), R.drawable.push_success)
            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                showCustomToast?.invoke(String.format(getString(R.string.push_toast_delete_alias_fail), errorMessage), R.drawable.push_fail)
            }
        })
    }

    /**
     * 移除别名后,更新标签
     */
    private fun updateTagAfterRemoveAlias(alias: String){
        aliasTagMap[alias]?.split(",")?.forEach {
            aliasTags.remove(it)
        }
        aliasTagData.value = aliasTags.joinToString(",")
        updateTagStatus()
        aliasTagMap.remove(alias)
        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putString(SP_KEY_ALIAS_TAG, aliasTagData.value)
            editor.putString(SP_KEY_ALIAS_TAG_MAP, gson.toJson(aliasTagMap))
            editor.apply()
        }
    }

    fun alreadyAddAlias(alias: String): Boolean {
        return currAliasList.contains(alias)
    }

    /**
     * 绑定账号
     */
    fun bindAccount(accountStr: String) {
        PushServiceFactory.getCloudPushService().bindAccount(accountStr, object : CommonCallback {
            override fun onSuccess(p0: String?) {
                updateTagAfterBindAccount()
                account.value = accountStr
                viewModelScope.launch {
                    val editor = preferences.edit()
                    editor.putString(SP_KEY_BIND_ACCOUNT, accountStr)
                    editor.apply()
                }
            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                showCustomToast?.invoke(String.format(getString(R.string.push_bind_account_fail), errorMessage), R.drawable.push_fail)
            }
        })
    }

    fun unbindAccount() {
        account.value = ""
        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putString(SP_KEY_BIND_ACCOUNT, "")
            editor.apply()
        }
        updateTagAfterBindAccount()
        PushServiceFactory.getCloudPushService().unbindAccount(object : CommonCallback {
            override fun onSuccess(p0: String?) {

            }

            override fun onFailed(errorCode: String?, errorMessage: String?) {
                showCustomToast?.invoke(String.format(getString(R.string.push_unbind_account_fail), errorMessage), R.drawable.push_fail)
            }
        })
    }

    /**
     * 绑定账号后,更新账号标签
     */
    private fun updateTagAfterBindAccount(){
        accountTags.clear()
        accountTagData.value = ""
        updateTagStatus()
        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putString(SP_KEY_ACCOUNT_TAG, accountTagData.value)
            editor.apply()
        }
    }

    /**
     * 绑定手机号
     */
    fun bindPhone(phoneNumber: String) {
        PushServiceFactory.getCloudPushService()
            .bindPhoneNumber(phoneNumber, object : CommonCallback {
                override fun onSuccess(p0: String?) {
                    phone.value = phoneNumber
                    viewModelScope.launch {
                        val editor = preferences.edit()
                        editor.putString(SP_KEY_BIND_PHONE, phoneNumber)
                        editor.apply()
                    }
                }

                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    showCustomToast?.invoke(String.format(getString(R.string.push_toast_bind_phone_fail), errorMessage), R.drawable.push_fail)
                }
            })
    }

    /**
     * 解绑手机号
     */
    fun unbindPhone() {
        phone.value = ""
        viewModelScope.launch {
            val editor = preferences.edit()
            editor.putString(SP_KEY_BIND_PHONE, "")
            editor.apply()
        }
        PushServiceFactory.getCloudPushService()
            .unbindPhoneNumber(object : CommonCallback {
                override fun onSuccess(p0: String?) {

                }

                override fun onFailed(errorCode: String?, errorMessage: String?) {
                    showCustomToast?.invoke(String.format(getString(R.string.push_toast_unbind_phone_fail), errorMessage), R.drawable.push_fail)
                }
            })
    }

    private fun getString(res: Int, ): String {
        return getApplication<MainApplication>().getString(res)
    }
}