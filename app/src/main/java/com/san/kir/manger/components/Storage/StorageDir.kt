package com.san.kir.manger.components.Storage

import java.io.File

data class StorageDir(val name: String = "",
                      val file: File = File(""),
                      val size: Long = 0L,
                      val countDir: Int = 0)
