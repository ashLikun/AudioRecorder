package com.ashlikun.audiorecorder.simple

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ashlikun.audiorecorder.RecordManager
import com.ashlikun.audiorecorder.recorder.RecordConfig
import com.ashlikun.audiorecorder.recorder.RecordHelper.RecordState
import com.ashlikun.audiorecorder.recorder.listener.RecordStateListener
import com.ashlikun.audiorecorder.simple.AudioView
import com.ashlikun.audiorecorder.simple.TestHzActivity
import com.ashlikun.audiorecorder.simple.base.MyApp
import com.ashlikun.audiorecorder.utils.Logger
import kotlinx.android.synthetic.main.activity_hz.*
import java.util.*

class TestHzActivity : AppCompatActivity(), OnItemSelectedListener, View.OnClickListener {
    private var isStart = false
    private var isPause = false
    val recordManager = RecordManager.getInstance()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_hz)
        requestPermission(arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)
                , denied = {
        }) {
            initAudioView()
        }

    }

    override fun onResume() {
        super.onResume()
        initRecord()
    }

    override fun onStop() {
        super.onStop()
        recordManager.stop()
    }

    private fun initAudioView() {
        audioView.setStyle(AudioView.ShowStyle.STYLE_ALL, AudioView.ShowStyle.STYLE_ALL)
        tvState.setVisibility(View.GONE)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, STYLE_DATA)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spUpStyle.setAdapter(adapter)
        spDownStyle.setAdapter(adapter)
        spUpStyle.setOnItemSelectedListener(this)
        spDownStyle.setOnItemSelectedListener(this)
        btRecord.setOnClickListener(this)
        btStop.setOnClickListener(this)
    }


    private fun initRecord() {
        recordManager.init(MyApp.getInstance(), BuildConfig.DEBUG)
        recordManager.changeFormat(RecordConfig.RecordFormat.WAV)
        val recordDir = String.format(Locale.getDefault(), "%s/Record/com.zlw.main/",
                Environment.getExternalStorageDirectory().absolutePath)
        recordManager.changeRecordDir(recordDir)
        recordManager.setRecordStateListener(object : RecordStateListener {
            override fun onStateChange(state: RecordState) {
                Logger.i(TAG, "onStateChange %s", state.name)
                when (state) {
                    RecordState.PAUSE -> tvState.setText("暂停中")
                    RecordState.IDLE -> tvState.setText("空闲中")
                    RecordState.RECORDING -> tvState.setText("录音中")
                    RecordState.STOP -> tvState.setText("停止")
                    RecordState.FINISH -> tvState.setText("录音结束")
                    else -> {
                    }
                }
            }

            override fun onError(error: String) {
                Logger.i(TAG, "onError %s", error)
            }
        })
        recordManager.setRecordResultListener { result -> Toast.makeText(this@TestHzActivity, "录音文件： " + result.absolutePath, Toast.LENGTH_SHORT).show() }
        recordManager.setRecordFftDataListener { data, size ->
            val newdata = ByteArray(data.size - 36)
            for (i in newdata.indices) {
                newdata[i] = data[i + 36]
            }
            audioView.setWaveData(data)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btRecord -> if (isStart) {
                recordManager.pause()
                btRecord.setText("开始")
                isPause = true
                isStart = false
            } else {
                if (isPause) {
                    recordManager.resume()
                } else {
                    recordManager.start()
                }
                btRecord.setText("暂停")
                isStart = true
            }
            R.id.btStop -> {
                recordManager.stop()
                btRecord.setText("开始")
                isPause = false
                isStart = false
            }
            else -> {
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent.id) {
            R.id.spUpStyle -> audioView.setStyle(AudioView.ShowStyle.getStyle(STYLE_DATA[position]), audioView.getDownStyle())
            R.id.spDownStyle -> audioView.setStyle(audioView.getUpStyle(), AudioView.ShowStyle.getStyle(STYLE_DATA[position]))
            else -> {
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    companion object {
        private val TAG = TestHzActivity::class.java.simpleName
        private val STYLE_DATA = arrayOf("STYLE_ALL", "STYLE_NOTHING", "STYLE_WAVE", "STYLE_HOLLOW_LUMP")
    }
}