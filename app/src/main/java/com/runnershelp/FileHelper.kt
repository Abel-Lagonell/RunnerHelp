package com.runnershelp

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileHelper(

) {
    fun compressCsvFiles(directoryPath: String) {
        val directory = File(directoryPath)

        if (directory.exists() && directory.isDirectory) {
            val zipFilePath = "${directory.absolutePath}/compressed.zip"
            val zipFile = File(zipFilePath)

            ZipOutputStream(FileOutputStream(zipFile)).use { zipOutputStream ->
                val csvFiles = directory.listFiles { file -> file.isFile && file.name.endsWith(".csv") }

                csvFiles?.forEach { csvFile ->
                    FileInputStream(csvFile).use { fileInputStream ->
                        val entry = ZipEntry(csvFile.name)
                        zipOutputStream.putNextEntry(entry)
                        fileInputStream.copyTo(zipOutputStream)
                        zipOutputStream.closeEntry()
                    }
                }
            }
        }
    }
}