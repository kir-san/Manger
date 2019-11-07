package com.san.kir.manger.utils.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun AppCompatActivity.compatCheckSelfPermission(permission: String): Int {
    return ContextCompat.checkSelfPermission(this, permission)
}

fun AppCompatActivity.compatRequestPermissions(permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(this, permissions, requestCode)
}

