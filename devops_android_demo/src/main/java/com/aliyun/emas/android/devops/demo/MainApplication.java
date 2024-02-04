package com.aliyun.emas.android.devops.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.taobao.update.IUpdateLog;
import com.taobao.update.adapter.UpdateAdapter;
import com.taobao.update.adapter.UserAction;
import com.taobao.update.apk.ApkDownloadListener;
import com.taobao.update.apk.ApkUpdater;
import com.taobao.update.apk.UpdateResultListener;
import com.taobao.update.common.Config;
import com.taobao.update.common.dialog.CustomUpdateInfo;
import com.taobao.update.common.dialog.UpdateNotifyListener;
import com.taobao.update.common.framework.UpdateRuntime;
import com.taobao.update.datasource.UpdateDataSource;

public class MainApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        Utils.initConfig(this);

        initUpdate();
    }

    private void initUpdate() {
        Config config = new Config();
        config.group = Utils.sAppkey + "@android";//AppInfoHelper.getGroup();
        config.ttid = Utils.sChannelID;
        config.isOutApk = false;
        config.appName = "EMAS Demo";
        //设置自定义弹窗
        setCustomDialog(this, false);
        UpdateRuntime.init(this, config.ttid, config.appName, config.group);
        ApkUpdater apkupdate = new ApkUpdater(getApplicationContext(), Utils.sAppkey, Utils.sAppSecret, config.group, Utils.sChannelID, config.ttid);
        UpdateAdapter updateAdapter = new UpdateAdapter();
        UpdateDataSource.getInstance().init(this, config.group, config.ttid, config.isOutApk, Utils.sAppkey, Utils.sAppSecret, Utils.sChannelID, updateAdapter);

        UpdateDataSource.getInstance().startUpdate(false);
    }
    /* 设置自定义弹窗
     * @param context Context
     * @param customDialog 是否自定义弹窗
     */
    private void setCustomDialog(final Context context, boolean customDialog) {
        ApkUpdater apkUpdate = new ApkUpdater(context);
        apkUpdate.setUpdateLog(new IUpdateLog() {
            @Override
            public void d(String s) {

            }

            @Override
            public void d(String s, Throwable throwable) {

            }

            @Override
            public void e(String s) {

            }

            @Override
            public void e(String s, Throwable throwable) {

            }

            @Override
            public void i(String s) {

            }

            @Override
            public void i(String s, Throwable throwable) {

            }

            @Override
            public void w(String s) {

            }

            @Override
            public void w(String s, Throwable throwable) {

            }

            @Override
            public void v(String s) {

            }

            @Override
            public void v(String s, Throwable throwable) {

            }
        });
        //设置apk下载进度回调
        apkUpdate.setApkDownloadListener(new ApkDownloadListener() {
            @Override
            public void onPreDownload() {
                //下载开始
                Log.i("devops","下载开始");
            }

            @Override
            public void onDownloadProgress(int progress) {
                //下载进行中
                Log.i("devops","下载进行中");
            }

            @Override
            public void onStartFileMd5Valid(String filePath, String fileSize) {
                //开启文件md5校验
                Log.i("devops","开启文件md5校验");
            }

            @Override
            public void onFinishFileMd5Valid(boolean success) {
                //完成文件校验
                Log.i("devops","完成文件校验");
            }

            @Override
            public void onDownloadFinish(String url, String filePath) {
                //下载完成
                Log.i("devops","下载完成");
            }

            @Override
            public void onDownloadError(String url, int errorCode, String msg) {
                //下载出错
                Log.i("devops","下载出错");
            }
        });
        if (customDialog) {
            //自定义有更新信息时的Dialog
            apkUpdate.setUpdateNotifyListener(new UpdateNotifyListener() {
                @Override
                public void onNotify(Activity activity, CustomUpdateInfo customUpdateInfo, final UserAction userAction) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("新版本升级");
                    builder.setMessage(customUpdateInfo.getInfo() + "\n版本：" + customUpdateInfo.getVersion() + "\n安装包大小：" + customUpdateInfo.getSize() + "\n是否强制升级：" + customUpdateInfo.isForceUpdate());
                    builder.setCancelable(false);
                    builder.setPositiveButton(userAction.getConfirmText(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userAction.onConfirm();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton(userAction.getCancelText(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userAction.onCancel();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
            //如果是强制更新，用户点击更新Dialog的取消按钮时会弹出这个Dialog；如果不是强制更新，则不会弹出这个Dialog
            apkUpdate.setCancelUpdateNotifyListener(new UpdateNotifyListener() {
                @Override
                public void onNotify(Activity activity, CustomUpdateInfo customUpdateInfo, final UserAction userAction) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("强制升级取消提示");
                    builder.setMessage(customUpdateInfo.getInfo());
                    builder.setCancelable(false);
                    builder.setPositiveButton(userAction.getConfirmText(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userAction.onConfirm();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton(userAction.getCancelText(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userAction.onCancel();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
            //apk下载完成后，安装Dialog
            apkUpdate.setInstallUpdateNotifyListener(new UpdateNotifyListener() {
                @Override
                public void onNotify(Activity activity, CustomUpdateInfo customUpdateInfo, final UserAction userAction) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("个性化安装");
                    builder.setMessage(customUpdateInfo.getInfo());
                    builder.setCancelable(false);
                    builder.setPositiveButton(userAction.getConfirmText(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userAction.onConfirm();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.setNegativeButton(userAction.getCancelText(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            userAction.onCancel();
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
            });
            //自定义更新结果提示
            apkUpdate.setUpdateResultListener(new UpdateResultListener() {
                @Override
                public void onFinish(int i, int i1, String s) {

                }
            });
        } else {
            //接口设为null，则弹窗默认使用SDK内置的Dialog
            apkUpdate.setUpdateNotifyListener(null);
            apkUpdate.setCancelUpdateNotifyListener(null);
            apkUpdate.setInstallUpdateNotifyListener(null);
        }

    }
}
