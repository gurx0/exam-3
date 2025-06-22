package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var tvPass: TextView
    private lateinit var tvDue: TextView
    private lateinit var trackName: TextView
    private lateinit var playBtn: Button
    private lateinit var pauseBtn: Button
    private lateinit var stopBtn: Button
    private var handler: Handler = Handler(Looper.getMainLooper())
    private var currentTrackIndex = 0
    private var pause = false

    private val trackList = listOf(
        R.raw.audio1,
        R.raw.audio2,
    )
    private val trackNames = listOf(
        "Track 1",
        "Track 2",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar = findViewById(R.id.seek_bar)
        tvPass = findViewById(R.id.tv_pass)
        tvDue = findViewById(R.id.tv_due)
        trackName = findViewById(R.id.track_name)
        playBtn = findViewById(R.id.playBtn)
        pauseBtn = findViewById(R.id.pauseBtn)
        stopBtn = findViewById(R.id.stopBtn)

        initializeMediaPlayer()
        setupSeekBar()
    }

    private fun initializeMediaPlayer() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayer = MediaPlayer.create(this, trackList[currentTrackIndex])
        trackName.text = trackNames[currentTrackIndex]
        seekBar.max = mediaPlayer.duration
        tvDue.text = formatTime(mediaPlayer.duration)
        updateSeekBar()

        mediaPlayer.setOnCompletionListener {
            nextTrack()
        }
    }

    private fun setupSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    tvPass.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                    tvPass.text = formatTime(mediaPlayer.currentPosition)
                }
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun formatTime(milliseconds: Int): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%d:%02d", minutes, secs)
    }

    fun playBtn(view: View) {
        if (pause) {
            mediaPlayer.seekTo(mediaPlayer.currentPosition)
            mediaPlayer.start()
            pause = false
            Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show()
        } else {
            initializeMediaPlayer()
            mediaPlayer.start()
            Toast.makeText(this, "Playing ${trackNames[currentTrackIndex]}", Toast.LENGTH_SHORT).show()
        }
        playBtn.isEnabled = false
        pauseBtn.isEnabled = true
        stopBtn.isEnabled = true
    }

    fun pauseBtn(view: View) {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            pause = true
            playBtn.isEnabled = true
            pauseBtn.isEnabled = false
            stopBtn.isEnabled = true
            Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopBtn(view: View) {
        if (::mediaPlayer.isInitialized && (mediaPlayer.isPlaying || pause)) {
            mediaPlayer.stop()
            mediaPlayer.release()
            pause = false
            handler.removeCallbacksAndMessages(null)
            playBtn.isEnabled = true
            pauseBtn.isEnabled = false
            stopBtn.isEnabled = false
            seekBar.progress = 0
            tvPass.text = "0:00"
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    fun nextBtn(view: View) {
        nextTrack()
    }

    fun prevBtn(view: View) {
        previousTrack()
    }

    private fun nextTrack() {
        currentTrackIndex = (currentTrackIndex + 1) % trackList.size
        initializeMediaPlayer()
        mediaPlayer.start()
        playBtn.isEnabled = false
        pauseBtn.isEnabled = true
        stopBtn.isEnabled = true
        Toast.makeText(this, "Playing ${trackNames[currentTrackIndex]}", Toast.LENGTH_SHORT).show()
    }

    private fun previousTrack() {
        currentTrackIndex = if (currentTrackIndex - 1 < 0) trackList.size - 1 else currentTrackIndex - 1
        initializeMediaPlayer()
        mediaPlayer.start()
        playBtn.isEnabled = false
        pauseBtn.isEnabled = true
        stopBtn.isEnabled = true
        Toast.makeText(this, "Playing ${trackNames[currentTrackIndex]}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        handler.removeCallbacksAndMessages(null)
    }
}