package com.example.kotlinpythonide.runtime

import android.content.Context
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import org.json.JSONObject
import java.io.File

class PythonRuntime(context: Context) {
    val version: String = "3.11"

    init {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    fun run(code: String, project: File): String {
        val result = Python.getInstance()
            .getModule("runner")
            .callAttr("run_code", code, project.absolutePath, packageDirectory(project))
            .toString()
        val json = JSONObject(result)
        val stdout = json.optString("stdout")
        val stderr = json.optString("stderr")
        return buildString {
            append(if (json.optBoolean("ok")) "Process finished successfully\n" else "Process failed\n")
            if (stdout.isNotBlank()) append("\n").append(stdout.trimEnd())
            if (stderr.isNotBlank()) append("\n\n").append(stderr.trimEnd())
        }
    }

    fun install(packageName: String, project: File): String {
        val result = Python.getInstance()
            .getModule("runner")
            .callAttr("install_package", packageName, packageDirectory(project))
            .toString()
        val json = JSONObject(result)
        return if (json.optBoolean("ok")) {
            "Package manager\n\n${json.optString("message")}"
        } else {
            "Package installation failed\n\n${json.optString("message")}"
        }
    }

    private fun packageDirectory(project: File): String =
        File(project, ".packages").apply { mkdirs() }.absolutePath
}