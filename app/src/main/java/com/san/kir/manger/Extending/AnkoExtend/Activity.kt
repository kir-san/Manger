package com.san.kir.manger.Extending.AnkoExtend

import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

fun AppCompatActivity.compatCheckSelfPermission(permission: String): Int {
    return ContextCompat.checkSelfPermission(this, permission)
}

fun AppCompatActivity.compatRequestPermissions(permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
}
