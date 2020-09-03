package com.ashlikun.audiorecorder.simple

import android.Manifest
import android.content.Intent
import android.media.AudioFormat
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.appcompat.app.AppCompatActivity
import com.ashlikun.audiorecorder.RecordManager
import com.ashlikun.audiorecorder.recorder.RecordConfig
import com.ashlikun.audiorecorder.recorder.RecordHelper.RecordState
import com.ashlikun.audiorecorder.recorder.listener.RecordStateListener
import com.ashlikun.audiorecorder.simple.AudioView
import com.ashlikun.audiorecorder.simple.MainActivity
import com.ashlikun.audiorecorder.simple.base.MyApp
import com.ashlikun.audiorecorder.utils.Logger
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), OnItemSelectedListener, View.OnClickListener {

    private var isStart = false
    private var isPause = false
    val recordManager = RecordManager.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAudioView()
        initEvent()
        initRecord()
        requestPermission(arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO)
                , denied = {
        }) {
            initRecord()
        }
        btRecord!!.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        doStop()
        initRecordEvent()
    }

    override fun onStop() {
        super.onStop()
        doStop()
    }

    private fun initAudioView() {
        tvState!!.visibility = View.GONE
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, STYLE_DATA)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spUpStyle!!.adapter = adapter
        spDownStyle!!.adapter = adapter
        spUpStyle!!.onItemSelectedListener = this
        spDownStyle!!.onItemSelectedListener = this
    }

    private fun initEvent() {
        rgAudioFormat!!.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbPcm -> recordManager.changeFormat(RecordConfig.RecordFormat.PCM)
                R.id.rbMp3 -> recordManager.changeFormat(RecordConfig.RecordFormat.MP3)
                R.id.rbWav -> recordManager.changeFormat(RecordConfig.RecordFormat.WAV)
                else -> {
                }
            }
        }
        rgSimpleRate!!.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb8K -> recordManager.changeRecordConfig(recordManager.recordConfig.setSampleRate(8000))
                R.id.rb16K -> recordManager.changeRecordConfig(recordManager.recordConfig.setSampleRate(16000))
                R.id.rb44K -> recordManager.changeRecordConfig(recordManager.recordConfig.setSampleRate(44100))
                else -> {
                }
            }
        }
        tbEncoding!!.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb8Bit -> recordManager.changeRecordConfig(recordManager.recordConfig.setEncodingConfig(AudioFormat.ENCODING_PCM_8BIT))
                R.id.rb16Bit -> recordManager.changeRecordConfig(recordManager.recordConfig.setEncodingConfig(AudioFormat.ENCODING_PCM_16BIT))
                else -> {
                }
            }
        }
    }

    private fun initRecord() {
        recordManager.init(MyApp.getInstance(), BuildConfig.DEBUG)
        recordManager.changeFormat(RecordConfig.RecordFormat.WAV)
        val recordDir = String.format(Locale.getDefault(), "%s/Record/com.zlw.main/",
                Environment.getExternalStorageDirectory().absolutePath)
        recordManager.changeRecordDir(recordDir)
        initRecordEvent()
    }

    private fun initRecordEvent() {
        recordManager.setRecordStateListener(object : RecordStateListener {
            override fun onStateChange(state: RecordState) {
                Logger.i(TAG, "onStateChange %s", state.name)
                when (state) {
                    RecordState.PAUSE -> tvState!!.text = "暂停中"
                    RecordState.IDLE -> tvState!!.text = "空闲中"
                    RecordState.RECORDING -> tvState!!.text = "录音中"
                    RecordState.STOP -> tvState!!.text = "停止"
                    RecordState.FINISH -> {
                        tvState!!.text = "录音结束"
                        tvSoundSize!!.text = "---"
                    }
                    else -> {
                    }
                }
            }

            override fun onError(error: String) {
                Logger.i(TAG, "onError %s", error)
            }
        })
        recordManager.setRecordSoundSizeListener { soundSize ->
            tvSoundSize!!.text = String.format(Locale.getDefault(), "声音大小：%s db", soundSize)
            Log.e("aaaaa", "$soundSize")
        }
        recordManager.setRecordResultListener { result -> Toast.makeText(this@MainActivity, "录音文件： " + result.absolutePath, Toast.LENGTH_SHORT).show() }
        recordManager.setRecordFftDataListener { data, size -> audioView!!.setWaveData(data) }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btRecord -> doPlay()
            R.id.btStop -> doStop()
            R.id.jumpTestActivity -> startActivity(Intent(this, TestHzActivity::class.java))
            else -> {
            }
        }
    }

    private fun doStop() {
        recordManager.stop()
        btRecord!!.text = "开始"
        isPause = false
        isStart = false
    }

    private fun doPlay() {
        if (isStart) {
            recordManager.pause()
            btRecord!!.text = "开始"
            isPause = true
            isStart = false
        } else {
            if (isPause) {
                recordManager.resume()
            } else {
                recordManager.start()
            }
            btRecord!!.text = "暂停"
            isStart = true
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent.id) {
            R.id.spUpStyle -> audioView!!.setStyle(AudioView.ShowStyle.getStyle(STYLE_DATA[position]), audioView!!.downStyle)
            R.id.spDownStyle -> audioView!!.setStyle(audioView!!.upStyle, AudioView.ShowStyle.getStyle(STYLE_DATA[position]))
            else -> {
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //nothing
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val STYLE_DATA = arrayOf("STYLE_ALL", "STYLE_NOTHING", "STYLE_WAVE", "STYLE_HOLLOW_LUMP")
    }
}