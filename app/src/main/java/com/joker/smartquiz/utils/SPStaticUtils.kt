package com.joker.smartquiz.utils

/**
 * SharedPreferences 工具类（静态方法版本）
 *
 * @author Blankj
 * @since 2019/01/04
 */
@Suppress("unused")
object SPStaticUtils {
    private var defaultInstance: SPUtils? = null

    /**
     * 获取默认 SPUtils 实例
     */
    private val defaultSPUtils: SPUtils
        get() = defaultInstance ?: SPUtils.instance

    ///////////////////////////////////////////////////////////////////////////
    // String 操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取字符串值
     *
     * @param key 键
     * @return 值，不存在返回 null
     */
    fun getString(key: String): String? {
        return getString(key, defaultSPUtils)
    }

    /**
     * 获取字符串值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，不存在返回默认值
     */
    fun getString(key: String, defaultValue: String?): String? {
        return getString(key, defaultValue, defaultSPUtils)
    }

    /**
     * 获取字符串值
     *
     * @param key     键
     * @param spUtils SPUtils 实例
     * @return 值，不存在返回 null
     */
    fun getString(key: String, spUtils: SPUtils): String? {
        return spUtils.getString(key)
    }

    /**
     * 获取字符串值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param spUtils      SPUtils 实例
     * @return 值，不存在返回默认值
     */
    fun getString(key: String, defaultValue: String?, spUtils: SPUtils): String? {
        return spUtils.getString(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Int 操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取整数值
     *
     * @param key 键
     * @return 值，不存在返回 -1
     */
    fun getInt(key: String): Int {
        return getInt(key, defaultSPUtils)
    }

    /**
     * 获取整数值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，不存在返回默认值
     */
    fun getInt(key: String, defaultValue: Int): Int {
        return getInt(key, defaultValue, defaultSPUtils)
    }

    /**
     * 获取整数值
     *
     * @param key     键
     * @param spUtils SPUtils 实例
     * @return 值，不存在返回 -1
     */
    fun getInt(key: String, spUtils: SPUtils): Int {
        return spUtils.getInt(key)
    }

    /**
     * 获取整数值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param spUtils      SPUtils 实例
     * @return 值，不存在返回默认值
     */
    fun getInt(key: String, defaultValue: Int, spUtils: SPUtils): Int {
        return spUtils.getInt(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Long 操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取长整型值
     *
     * @param key 键
     * @return 值，不存在返回 -1
     */
    fun getLong(key: String): Long {
        return getLong(key, defaultSPUtils)
    }

    /**
     * 获取长整型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，不存在返回默认值
     */
    fun getLong(key: String, defaultValue: Long): Long {
        return getLong(key, defaultValue, defaultSPUtils)
    }

    /**
     * 获取长整型值
     *
     * @param key     键
     * @param spUtils SPUtils 实例
     * @return 值，不存在返回 -1
     */
    fun getLong(key: String, spUtils: SPUtils): Long {
        return spUtils.getLong(key)
    }

    /**
     * 获取长整型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param spUtils      SPUtils 实例
     * @return 值，不存在返回默认值
     */
    fun getLong(key: String, defaultValue: Long, spUtils: SPUtils): Long {
        return spUtils.getLong(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Float 操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取浮点型值
     *
     * @param key 键
     * @return 值，不存在返回 -1f
     */
    fun getFloat(key: String): Float {
        return getFloat(key, defaultSPUtils)
    }

    /**
     * 获取浮点型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，不存在返回默认值
     */
    fun getFloat(key: String, defaultValue: Float): Float {
        return getFloat(key, defaultValue, defaultSPUtils)
    }

    /**
     * 获取浮点型值
     *
     * @param key     键
     * @param spUtils SPUtils 实例
     * @return 值，不存在返回 -1f
     */
    fun getFloat(key: String, spUtils: SPUtils): Float {
        return spUtils.getFloat(key)
    }

    /**
     * 获取浮点型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param spUtils      SPUtils 实例
     * @return 值，不存在返回默认值
     */
    fun getFloat(key: String, defaultValue: Float, spUtils: SPUtils): Float {
        return spUtils.getFloat(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Boolean 操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取布尔值
     *
     * @param key 键
     * @return 值，不存在返回 false
     */
    fun getBoolean(key: String): Boolean {
        return getBoolean(key, defaultSPUtils)
    }

    /**
     * 获取布尔值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，不存在返回默认值
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getBoolean(key, defaultValue, defaultSPUtils)
    }

    /**
     * 获取布尔值
     *
     * @param key     键
     * @param spUtils SPUtils 实例
     * @return 值，不存在返回 false
     */
    fun getBoolean(key: String, spUtils: SPUtils): Boolean {
        return spUtils.getBoolean(key)
    }

    /**
     * 获取布尔值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param spUtils      SPUtils 实例
     * @return 值，不存在返回默认值
     */
    fun getBoolean(key: String, defaultValue: Boolean, spUtils: SPUtils): Boolean {
        return spUtils.getBoolean(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // StringSet 操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取字符串集合
     *
     * @param key 键
     * @return 值，不存在返回 null
     */
    fun getStringSet(key: String): Set<String>? {
        return getStringSet(key, defaultSPUtils)
    }

    /**
     * 获取字符串集合
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值，不存在返回默认值
     */
    fun getStringSet(key: String, defaultValue: Set<String>?): Set<String>? {
        return getStringSet(key, defaultValue, defaultSPUtils)
    }

    /**
     * 获取字符串集合
     *
     * @param key     键
     * @param spUtils SPUtils 实例
     * @return 值，不存在返回 null
     */
    fun getStringSet(key: String, spUtils: SPUtils): Set<String>? {
        return spUtils.getStringSet(key)
    }

    /**
     * 获取字符串集合
     *
     * @param key          键
     * @param defaultValue 默认值
     * @param spUtils      SPUtils 实例
     * @return 值，不存在返回默认值
     */
    fun getStringSet(key: String, defaultValue: Set<String>?, spUtils: SPUtils): Set<String>? {
        return spUtils.getStringSet(key, defaultValue)
    }

    ///////////////////////////////////////////////////////////////////////////
    // 获取所有值
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 获取所有值
     *
     * @return 所有键值对
     */
    val all: Map<String, *>
        get() = getAll(defaultSPUtils)

    /**
     * 获取所有值
     *
     * @param spUtils SPUtils 实例
     * @return 所有键值对
     */
    fun getAll(spUtils: SPUtils): Map<String, *> {
        return spUtils.all
    }

    ///////////////////////////////////////////////////////////////////////////
    // 存储操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 存储字符串值
     *
     * @param key   键
     * @param value 值
     */
    @JvmOverloads
    fun put(key: String, value: String?, spUtils: SPUtils = defaultSPUtils) {
        spUtils.putString(key, value)
    }

    /**
     * 存储整数值
     *
     * @param key   键
     * @param value 值
     */
    @JvmOverloads
    fun put(key: String, value: Int, spUtils: SPUtils = defaultSPUtils) {
        spUtils.putInt(key, value)
    }

    /**
     * 存储长整型值
     *
     * @param key   键
     * @param value 值
     */
    @JvmOverloads
    fun put(key: String, value: Long, spUtils: SPUtils = defaultSPUtils) {
        spUtils.putLong(key, value)
    }

    /**
     * 存储浮点型值
     *
     * @param key   键
     * @param value 值
     */
    @JvmOverloads
    fun put(key: String, value: Float, spUtils: SPUtils = defaultSPUtils) {
        spUtils.putFloat(key, value)
    }

    /**
     * 存储布尔值
     *
     * @param key   键
     * @param value 值
     */
    @JvmOverloads
    fun put(key: String, value: Boolean, spUtils: SPUtils = defaultSPUtils) {
        spUtils.putBoolean(key, value)
    }

    /**
     * 存储字符串集合
     *
     * @param key   键
     * @param value 值
     */
    @JvmOverloads
    fun put(key: String, value: Set<String?>?, spUtils: SPUtils = defaultSPUtils) {
        @Suppress("UNCHECKED_CAST")
        spUtils.putStringSet(key, value as Set<String>?)
    }

    ///////////////////////////////////////////////////////////////////////////
    // 其他操作
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 检查是否包含指定键
     *
     * @param key 键
     * @return true 包含，false 不包含
     */
    @JvmOverloads
    fun contains(key: String, spUtils: SPUtils = defaultSPUtils): Boolean {
        return spUtils.contains(key)
    }

    /**
     * 移除指定键值对
     *
     * @param key 键
     */
    @JvmOverloads
    fun remove(key: String, spUtils: SPUtils = defaultSPUtils) {
        spUtils.remove(key)
    }

    /**
     * 清除所有数据
     */
    @JvmOverloads
    fun clear(spUtils: SPUtils = defaultSPUtils) {
        spUtils.clear()
    }

    /**
     * 设置默认 SPUtils 实例
     *
     * @param spUtils 默认实例
     */
    fun setDefaultSPUtils(spUtils: SPUtils) {
        defaultInstance = spUtils
    }
}