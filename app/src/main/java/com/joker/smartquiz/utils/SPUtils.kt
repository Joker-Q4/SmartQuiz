package com.joker.smartquiz.utils

import android.content.Context
import android.content.SharedPreferences
import com.joker.smartquiz.JokerApp
import androidx.core.content.edit

/**
 * SharedPreferences 工具类
 *
 * @author Blankj
 * @since 2016/08/02
 */
@Suppress("unused")
class SPUtils private constructor(spName: String) {

    private val sp: SharedPreferences =
        JokerApp.INS.getSharedPreferences(spName, Context.MODE_PRIVATE)

    ///////////////////////////////////////////////////////////////////////////
    // String 操作
    ///////////////////////////////////////////////////////////////////////////

    fun putString(key: String, value: String?) {
        sp.edit { putString(key, value) }
    }

    fun getString(key: String, defaultValue: String? = ""): String? {
        return sp.getString(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Int 操作
    ///////////////////////////////////////////////////////////////////////////

    fun putInt(key: String, value: Int) {
        sp.edit { putInt(key, value) }
    }

    fun getInt(key: String, defaultValue: Int = -1): Int {
        return sp.getInt(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Long 操作
    ///////////////////////////////////////////////////////////////////////////

    fun putLong(key: String, value: Long) {
        sp.edit { putLong(key, value) }
    }

    fun getLong(key: String, defaultValue: Long = -1L): Long {
        return sp.getLong(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Float 操作
    ///////////////////////////////////////////////////////////////////////////

    fun putFloat(key: String, value: Float) {
        sp.edit { putFloat(key, value) }
    }

    fun getFloat(key: String, defaultValue: Float = -1f): Float {
        return sp.getFloat(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Boolean 操作
    ///////////////////////////////////////////////////////////////////////////

    fun putBoolean(key: String, value: Boolean) {
        sp.edit { putBoolean(key, value) }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sp.getBoolean(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // StringSet 操作
    ///////////////////////////////////////////////////////////////////////////

    fun putStringSet(key: String, value: Set<String>?) {
        sp.edit { putStringSet(key, value) }
    }

    fun getStringSet(key: String, defaultValue: Set<String>? = emptySet()): Set<String>? {
        return sp.getStringSet(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // 其他操作
    ///////////////////////////////////////////////////////////////////////////

    val all: Map<String, *>
        get() = sp.all

    operator fun contains(key: String): Boolean {
        return sp.contains(key)
    }

    fun remove(key: String) {
        sp.edit { remove(key) }
    }

    fun clear() {
        sp.edit { clear() }
    }

    companion object {
        private val SP_UTILS_MAP = HashMap<String, SPUtils>()

        /**
         * 获取默认 SPUtils 实例
         */
        val instance: SPUtils
            get() = getInstance("")

        /**
         * 获取指定名称的 SPUtils 实例
         *
         * @param spName SP 名称
         * @return SPUtils 实例
         */
        fun getInstance(spName: String = ""): SPUtils {
            val name = spName.ifBlank { "spUtils" }
            return SP_UTILS_MAP.getOrPut(name) { SPUtils(name) }
        }
    }
}
