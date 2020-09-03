package com.ashlikun.audiorecorder.simple

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import permissions.dispatcher.PermissionUtils

/**
 * 作者　　: 李坤
 * 创建时间: 2020/9/3　15:34
 * 邮箱　　：496546144@qq.com
 *
 * 功能介绍：
 */

fun ComponentActivity.requestPermission(permission: Array<String>, showRationaleMessage: String? = null
                                        , denied: (() -> Unit)? = null
                                        , success: (() -> Unit)): ActivityResultLauncher<Array<String>> {
    val launcher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it.all { itt -> itt.value }) {
            success.invoke()
        } else {
            denied?.invoke()
        }
    }

    //弹窗提示
    fun showRationaleDialog(showRationaleMessage: String? = null) {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("权限申请")
                .setMessage(showRationaleMessage ?: "aaaaaa")
                .setPositiveButton("确认") { dialoog, which ->
                    launcher.launch(permission)
                }
                .setNegativeButton("取消") { dialog, which ->
                    denied?.invoke()
                }
                .show()
    }
    //是否已经有权限
    if (PermissionUtils.hasSelfPermissions(this, *permission)) {
        success.invoke()
        return launcher
    } else {
        //是否之前拒绝过
        if (PermissionUtils.shouldShowRequestPermissionRationale(this, *permission)) {
            showRationaleDialog(showRationaleMessage)
        } else {
            //请求权限
            launcher.launch(permission)
        }
    }
    return launcher
}