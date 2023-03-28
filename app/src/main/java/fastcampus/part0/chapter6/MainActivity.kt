package fastcampus.part0.chapter6

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import fastcampus.part0.chapter6.databinding.ActivityMainBinding
import fastcampus.part0.chapter6.databinding.DialogCountdownSettingBinding
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var countDownSecond = 10
    private var currentDeciSecond = 0
    private var currentCountdownDeciSecond = countDownSecond * 10
    private var timer : Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.countdownTextView.setOnClickListener {
            showCountDownSettingDialog()
        }

        binding.startButton.setOnClickListener {
            start()
            binding.startButton.isVisible = false
            binding.stopButton.isVisible = false
            binding.pauseButton.isVisible = true
            binding.lapButton.isVisible = true
        }
        binding.stopButton.setOnClickListener {
            showAlertDialog()
            stop()
        }
        binding.pauseButton.setOnClickListener{
            pause()
            binding.startButton.isVisible = true
            binding.stopButton.isVisible = true
            binding.pauseButton.isVisible = false
            binding.lapButton.isVisible = false
        }
        binding.lapButton.setOnClickListener{
            lap()
        }
        initCountdownViews()
    }

    private fun initCountdownViews(){
        val seconds = currentCountdownDeciSecond / 10
        binding.countdownTextView.text = String.format("%02d", seconds)
        binding.countdownProgressBar.progress = 100
    }

    private fun start() {
        timer = timer(initialDelay = 0, period = 100){
            if(currentCountdownDeciSecond == 0){
                currentDeciSecond += 1

                val min = currentDeciSecond.div(10)/60
                val sec = currentDeciSecond.div(10)%60
                val deciSeconds = currentDeciSecond%10

                runOnUiThread{
                    binding.timeTextView.text =
                        String.format("%02d:%02d", min, sec)
                    binding.tickTextView.text = deciSeconds.toString()

                    binding.countdownGroup.isVisible = false
                }
            } else{
                currentCountdownDeciSecond -= 1
                val seconds = currentCountdownDeciSecond/10
                val progress = (currentCountdownDeciSecond / (countDownSecond * 10f)) * 100

                binding.root.post{
                    binding.countdownTextView.text = String.format("%02d", seconds)
                    binding.countdownProgressBar.progress = progress.toInt()
                }
            }
            if(currentDeciSecond == 0 && currentCountdownDeciSecond < 31 && currentCountdownDeciSecond%10 == 0){
                val toneType = if(currentCountdownDeciSecond == 0) ToneGenerator.TONE_CDMA_HIGH_L else ToneGenerator.TONE_CDMA_ANSWER
                ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME)
                    .startTone(toneType, 100)
            }
        }
    }

    private fun pause() {
        timer?.cancel()
        timer = null
    }

    private fun stop(){
        binding.startButton.isVisible = true
        binding.stopButton.isVisible = true
        binding.pauseButton.isVisible = false
        binding.lapButton.isVisible = false

        currentDeciSecond = 0
        binding.timeTextView.text = "00.00"
        binding.tickTextView.text = "0"
        binding.countdownGroup.isVisible = true
        initCountdownViews()
        binding.lapContainerLinearLayout.removeAllViews()
    }

    private fun lap(){
        if(currentDeciSecond == 0) return
        val container = binding.lapContainerLinearLayout
        TextView(this).apply{
            textSize = 20f
            gravity = Gravity.CENTER
            val minutes = currentDeciSecond.div(10) / 60
            val seconds = currentDeciSecond.div(10) % 60
            val deciSeconds = currentDeciSecond % 10
            text = container.childCount.inc().toString() + String.format( //format: 문자열의 형식을 설정하는 메서드
                "%02d:%02d %01d",
                minutes,
                seconds,
                deciSeconds
            )
            setPadding(30)
        }.let{ labTextView ->
            container.addView(labTextView, 0)
        }
    }

    private fun showCountDownSettingDialog() {
        AlertDialog.Builder(this).apply {
            val dialogBinding = DialogCountdownSettingBinding.inflate(layoutInflater)
            with(dialogBinding.countdownSecondPicker){
                maxValue = 20
                minValue = 0
                value = countDownSecond
            }
            setTitle("카운트다운 설정")
            setView(dialogBinding.root)
            setPositiveButton("확인") { _, _ ->
                countDownSecond = dialogBinding.countdownSecondPicker.value
                currentCountdownDeciSecond = countDownSecond * 10
                binding.countdownTextView.text = String.format("%02d", countDownSecond)
            }
            setNegativeButton("취소", null)
        }.show()
    }
    private fun showAlertDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("종료하시겠습니까?")
            setPositiveButton("네"){ _, _ ->
                stop()
            }
            setNegativeButton("아니요", null)
        }.show()
    }
}