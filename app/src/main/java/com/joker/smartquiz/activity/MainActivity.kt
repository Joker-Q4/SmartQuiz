@file:OptIn(DelicateCoroutinesApi::class)
@file:Suppress("DeferredResultUnused", "LocalVariableName")

package com.joker.smartquiz.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.hutool.extra.tokenizer.engine.ikanalyzer.IKAnalyzerEngine
import cn.vove7.andro_accessibility_api.AccessibilityApi
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.base.IPermission
import com.hjq.permissions.permission.dangerous.PostNotificationsPermission
import com.hjq.permissions.permission.special.SystemAlertWindowPermission
import com.joker.smartquiz.R
import com.joker.smartquiz.action.*
import com.joker.smartquiz.adapter.ActionAdapter
import com.joker.smartquiz.database.AppDatabase
import com.joker.smartquiz.database.entity.InputData
import com.joker.smartquiz.database.entity.InputTitle
import com.joker.smartquiz.service.BaseAccessibilityService
import com.joker.smartquiz.service.ForegroundService
import com.joker.smartquiz.service.NoneService
import com.joker.smartquiz.utils.NanoIdUtils
import com.joker.smartquiz.utils.SPStaticUtils
import com.joker.smartquiz.utils.launchWithExpHandler
import com.joker.smartquiz.utils.toast
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupAnimation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.apache.commons.lang3.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream

/**
 * @author Joker
 * @since 2026/08/11
 */
class MainActivity : AppCompatActivity() {

    lateinit var foregroundServiceIntent: Intent
    val actions: MutableList<Action<*>> = mutableListOf(
        object : Action<NoneService>(null) {
            override val name = "使用教程"
            override suspend fun run(act: Activity) {
                actionJob?.cancel()
                act.runOnUiThread { (act as MainActivity).showGuide {} }
            }
        },
        RequireAction(),
        AnswerAction(),
        object : Action<NoneService>(null) {
            override val name = "选择文件"
            override suspend fun run(act: Activity) {
                actionJob?.cancel()
                act.runOnUiThread { pickExcelFile() } // 选择题库文件（xls/xlsx/csv）
            }
        },
        object : Action<NoneService>(null) {
            override val name = "题库管理"
            override suspend fun run(act: Activity) {
                actionJob?.cancel()
                act.startActivity(Intent(act, BankListActivity::class.java))
            }
        },
//        object : Action<NoneService>(null) {
//            override val name = "退出"
//            override suspend fun run(act: Activity) {
//                actionJob?.cancel()
//                doStop(foregroundServiceIntent)
//            }
//        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 15+ 强制使用边缘到边缘模式
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        // 设置状态栏占位 View 的高度
        val statusBarPlaceholder = findViewById<View>(R.id.status_bar_placeholder)
        ViewCompat.setOnApplyWindowInsetsListener(statusBarPlaceholder) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.layoutParams.height = insets.top
            v.requestLayout()
            WindowInsetsCompat.CONSUMED
        }

        System.setProperty("log4j2.disable.jmx", "true")
        System.setProperty("log4j2.disable", "true")
        foregroundServiceIntent = Intent(this, ForegroundService::class.java)
        if (!SPStaticUtils.contains(KEY_GUIDE_SHOWN)) {
            // 首次使用：先看图文引导，点「开始使用」后再进入授权流程
            showGuide {
                SPStaticUtils.put(KEY_GUIDE_SHOWN, true)
                startPermissionFlow()
            }
        } else {
            startPermissionFlow()
        }
    }

    /**
     * 弹出图文使用教程。onStart 在用户点击「开始使用」后回调。
     */
    fun showGuide(onStart: () -> Unit) {
        XPopup.Builder(this)
            .dismissOnTouchOutside(false)
            .dismissOnBackPressed(false)
            .isDestroyOnDismiss(true)
            .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
            .asCustom(object : BasePopupView(this) {
                override fun getPopupLayoutId(): Int = R.layout.view_guide

                override fun onCreate() {
                    super.onCreate()
                    findViewById<View>(R.id.guide_confirm).setOnClickListener {
                        dismiss()
                        onStart()
                    }
                }
            })
            .show()
    }

    private fun startPermissionFlow() {
        val key = "isShow"
        val alertWindow = SystemAlertWindowPermission()
        val postNotifications = PostNotificationsPermission()
        if(!SPStaticUtils.contains(key) || !alertWindow.isGrantedPermission(this@MainActivity) || !postNotifications.isGrantedPermission(this@MainActivity)) {
            XPopup.Builder(this)
                .dismissOnTouchOutside(false)
                .isDestroyOnDismiss(true)
                .isRequestFocus(true)
                .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                .asConfirm("提示", "本APP为内部测试，请勿传播\n请授予悬浮窗、通知和无障碍权限\n同意请点击确定继续使用",  {
                    startForegroundService(foregroundServiceIntent)
                    SPStaticUtils.put(key, true)
                    XXPermissions.with(this)
                        .permission(alertWindow)
                        .permission(postNotifications)
                        .request(object : OnPermissionCallback {
                            override fun onPermissionResult(grantedList: MutableList<IPermission>, deniedList: MutableList<IPermission>) {
                                if (deniedList.isNotEmpty()) {
                                    val anyDoNotAskAgain = deniedList.any { it.isDoNotAskAgainPermission(this@MainActivity) }
                                    if (anyDoNotAskAgain) {
                                        toast("被永久拒绝授权，请手动授予悬浮窗权限和通知权限")
                                        XXPermissions.startPermissionActivity(this@MainActivity, deniedList)
                                    } else if (grantedList.isNotEmpty()) {
                                        toast("获取部分权限成功，但部分权限未正常授予")
                                    } else {
                                        toast("获取悬浮窗权限和通知权限失败")
                                    }
                                    return
                                }
                                toast("获取悬浮窗权限和通知权限成功")
                                setupActionList()
                            }
                        })
                }, {
                    doStop(foregroundServiceIntent)
                }).show()
        }else {
            setupActionList()
        }
    }

    private fun setupActionList() {
        val rv = findViewById<RecyclerView>(R.id.recycler_view)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = ActionAdapter { action -> onActionClick(action) }
        rv.adapter = adapter
        adapter.submitList(actions)
    }

    private fun doStop(service: Intent){
        stopService(service)
        if(AccessibilityApi.isBaseServiceEnable) {
            val intent = Intent(this@MainActivity, BaseAccessibilityService::class.java)
            intent.putExtra("command", "please stop")
            startService(intent)
        }
        finish()
//                    exitProcess(0)
    }

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            importFilesBatch(uris)
        } else {
            Log.e("FilePicker", "未选择文件")
        }
    }

    /**
     * 批量导入文件，只显示一次加载框
     */
    private fun importFilesBatch(uris: List<Uri>) {
        val loading = XPopup.Builder(this)
            .dismissOnTouchOutside(false)
            .isDestroyOnDismiss(true)
            .dismissOnBackPressed(false)
            .enableDrag(false)
            .autoDismiss(false)
            .positionByWindowCenter(true)
            .enableShowWhenAppBackground(true)
            .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
            .asLoading("批量导入中...")
            .show()

        GlobalScope.async {
            val results = mutableListOf<Pair<String, Int>>() // 文件名 -> 导入数量
            val errors = mutableListOf<String>()

            for (uri in uris) {
                try {
                    val result = readExcelFileForBatch(uri)
                    if (result != null) {
                        results.add(result)
                    }
                } catch (e: Exception) {
                    val fileName = getFileName(uri)
                    errors.add("$fileName: ${e.message}")
                }
            }

            CoroutineScope(Dispatchers.Main).launch {
                loading.dismiss()
                // 显示汇总结果
                if (results.isNotEmpty()) {
                    val totalFiles = results.size
                    val totalQuestions = results.sumOf { it.second }
                    var message = "成功导入 $totalFiles 个文件，共 $totalQuestions 道题"
                    if (errors.isNotEmpty()) {
                        message += "\n\n失败：\n${errors.joinToString("\n")}"
                    }
                    toast(message)
                } else if (errors.isNotEmpty()) {
                    toast("导入失败：\n${errors.joinToString("\n")}")
                }
            }
        }
    }

    /**
     * 读取单个文件并返回结果（不显示加载框）
     */
    private fun readExcelFileForBatch(uri: Uri): Pair<String, Int>? {
        val fileName = getFileName(uri)
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val mimeType = contentResolver.getType(uri)
        val ext = fileName.substringAfterLast('.', "").lowercase()

        val count = when {
            mimeType == MIME_XLSX || ext == "xlsx" -> readXlsxFileForBatch(inputStream, uri)
            mimeType == MIME_XLS || ext == "xls" -> readXlsFileForBatch(inputStream, uri)
            ext == "csv" || mimeType == MIME_CSV -> readCsvFileForBatch(inputStream, uri)
            else -> {
                throw IllegalArgumentException("不支持的文件类型")
            }
        }
        return Pair(fileName, count)
    }

    fun pickExcelFile() {
        filePickerLauncher.launch("*/*") // 兼容各文件管理器，具体类型由 readExcelFile 识别
    }

    /**
     * 批量版本：读取 XLS 文件，不显示加载框，返回导入数量
     */
    private fun readXlsFileForBatch(inputStream: InputStream, uri: Uri): Int {
        val fileName = getFileName(uri)
        return try {
            val workbook = HSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val mutableListOf = mutableListOf<InputData>()
            val id = NanoIdUtils.randomNanoId()
            var count = 0

            for ((index, cells) in sheet.withIndex()) {
                if (index == 0) continue
                if (cells.all { StringUtils.isBlank(getCellValue(it)) }) continue
                if (cells.lastCellNum <= 3) continue

                val cell_0 = getCellValue(cells.getCell(0))?.trim()
                val cell_1 = getCellValue(cells.getCell(1))?.trim()
                val cell_2 = getCellValue(cells.getCell(2))?.trim()
                if (StringUtils.isBlank(cell_0) || StringUtils.isBlank(cell_1) || StringUtils.isBlank(cell_2)) continue

                mutableListOf.add(InputData(NanoIdUtils.randomNanoId(), id, cell_0, cell_1,
                    null, cell_2, getCellValue(cells.getCell(3))?.trim(),
                    getCellValue(cells.getCell(4))?.trim(), getCellValue(cells.getCell(5))?.trim(),
                    getCellValue(cells.getCell(6))?.trim(), getCellValue(cells.getCell(7))?.trim()))
                count++
            }
            workbook.close()

            // 处理分词和入库
            val inputTitleDao = AppDatabase.getInstance().inputTitleDao()
            val inputDataDao = AppDatabase.getInstance().inputDataDao()
            val engine = IKAnalyzerEngine()

            for (data in mutableListOf) {
                val key = mutableListOf(data.question, data.col_a, data.col_b, data.col_c, data.col_d, data.col_e, data.col_f)
                    .asSequence().filterNotNull().map { it.trim() }.filter { it.isNotEmpty() }.toList().joinToString(" ")
                val parse = engine.parse(key)
                data.key = parse.map { it.text }.filter { it != null && it.isNotEmpty() }.toList().joinToString("||||")
            }
            inputDataDao.addAll(mutableListOf)
            inputTitleDao.add(InputTitle(id, fileName))
            count
        } catch (e: Exception) {
            throw Exception("解析失败: ${e.message}")
        }
    }

    fun getCellValue(cell :Cell?):String? {
        if(cell == null)
            return ""
        if(cell.cellType == CellType.NUMERIC) {
            return cell.numericCellValue.toString()
        }else if(cell.cellType == CellType.FORMULA || cell.cellType == CellType.ERROR || cell.cellType == CellType.BLANK) {
            return ""
        }else if(cell.cellType == CellType.BOOLEAN) {
            return cell.booleanCellValue.toString()
        }
        return cell.stringCellValue
    }

    private fun csvCol(record: CSVRecord, idx: Int): String? =
        if (idx < record.size()) record.get(idx)?.trim()?.ifEmpty { null } else null

    /**
     * 批量版本：读取 XLSX 文件，不显示加载框，返回导入数量
     */
    private fun readXlsxFileForBatch(inputStream: InputStream, uri: Uri): Int {
        val fileName = getFileName(uri)
        return try {
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val inputTitleDao = AppDatabase.getInstance().inputTitleDao()
            val inputDataDao = AppDatabase.getInstance().inputDataDao()
            val id = NanoIdUtils.randomNanoId()
            var count = 0
            val dataList = mutableListOf<InputData>()

            for ((index, cells) in sheet.withIndex()) {
                if (index == 0) continue
                if (cells.all { StringUtils.isBlank(getCellValue(it)) }) continue
                if (cells.lastCellNum <= 3) continue

                val cell_0 = getCellValue(cells.getCell(0))?.trim()
                val cell_1 = getCellValue(cells.getCell(1))?.trim()
                val cell_2 = getCellValue(cells.getCell(2))?.trim()
                if (StringUtils.isBlank(cell_0) || StringUtils.isBlank(cell_1) || StringUtils.isBlank(cell_2)) continue

                dataList.add(InputData(NanoIdUtils.randomNanoId(), id, cell_0, cell_1,
                    null, cell_2, getCellValue(cells.getCell(3))?.trim(),
                    getCellValue(cells.getCell(4))?.trim(), getCellValue(cells.getCell(5))?.trim(),
                    getCellValue(cells.getCell(6))?.trim(), getCellValue(cells.getCell(7))?.trim()))
                count++
            }
            workbook.close()

            // 处理分词和入库
            for (data in dataList) {
                val key = mutableListOf(data.question, data.col_a, data.col_b, data.col_c, data.col_d, data.col_e, data.col_f)
                    .asSequence().filterNotNull().map { it.trim() }.filter { it.isNotEmpty() }.toList().joinToString(" ")
                val split = key.split("[\\p{Punct}\\s]+")
                data.key = split.filter { it.isNotEmpty() }.toList().joinToString(" ")
                inputDataDao.add(data)
            }
            inputTitleDao.add(InputTitle(id, fileName))
            count
        } catch (e: Exception) {
            throw Exception("解析失败: ${e.message}")
        }
    }

    /**
     * 批量版本：读取 CSV 文件，不显示加载框，返回导入数量
     */
    private fun readCsvFileForBatch(inputStream: InputStream, uri: Uri): Int {
        val fileName = getFileName(uri)
        return try {
            val dataList = mutableListOf<InputData>()
            val id = NanoIdUtils.randomNanoId()
            var count = 0
            val format = CSVFormat.DEFAULT.builder().setTrim(true).get()

            CSVParser.parse(inputStream.bufferedReader(), format).use { parser ->
                for ((index, record) in parser.withIndex()) {
                    if (index == 0) continue
                    val size = record.size()
                    if ((0 until size).all { StringUtils.isBlank(record.get(it)) }) continue
                    if (size <= 3) continue

                    val cell_0 = record.get(0)?.trim()
                    val cell_1 = record.get(1)?.trim()
                    val cell_2 = record.get(2)?.trim()
                    if (StringUtils.isBlank(cell_0) || StringUtils.isBlank(cell_1) || StringUtils.isBlank(cell_2)) continue

                    dataList.add(InputData(NanoIdUtils.randomNanoId(), id, cell_0, cell_1,
                        null, cell_2, csvCol(record, 3), csvCol(record, 4), csvCol(record, 5),
                        csvCol(record, 6), csvCol(record, 7)))
                    count++
                }
            }

            // 处理分词和入库
            val inputTitleDao = AppDatabase.getInstance().inputTitleDao()
            val inputDataDao = AppDatabase.getInstance().inputDataDao()
            val engine = IKAnalyzerEngine()

            for (data in dataList) {
                val key = mutableListOf(data.question, data.col_a, data.col_b, data.col_c, data.col_d, data.col_e, data.col_f)
                    .asSequence().filterNotNull().map { it.trim() }.filter { it.isNotEmpty() }.toList().joinToString(" ")
                val parse = engine.parse(key)
                data.key = parse.map { it.text }.filter { it != null && it.isNotEmpty() }.toList().joinToString("||||")
            }
            inputDataDao.addAll(dataList)
            inputTitleDao.add(InputTitle(id, fileName))
            count
        } catch (e: Exception) {
            throw Exception("解析失败: ${e.message}")
        }
    }

    fun getFileName(uri: Uri): String {
        var fileName = "未知文件"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && it.moveToFirst()) {
                fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }

    var actionJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    private fun onActionClick(action: Action<*>) {
        if (action.name == "Stop") {
            actionJob?.cancel()
            return
        }
        if (actionJob?.isCompleted.let { it != null && !it }) {
            toast("有正在运行的任务")
            return
        }
        actionJob = launchWithExpHandler {
            action.run(this@MainActivity)
        }
        actionJob?.invokeOnCompletion {
//            toast("执行结束")
        }
    }

    override fun onDestroy() {
        actionJob?.cancel()
        super.onDestroy()
    }

    companion object {
        const val MIME_XLS = "application/vnd.ms-excel"
        const val MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        const val MIME_CSV = "text/csv"
        const val KEY_GUIDE_SHOWN = "guide_shown"
    }

}
