package es.usj.master.luengo.musicquiz

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import es.usj.master.luengo.musicquiz.Models.QuizItem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private var songsList: MutableList<QuizItem> = mutableListOf()
    private var roundSongsList: MutableList<QuizItem> = mutableListOf()
    private var mediaPlayer: MediaPlayer? = null
    private var correctSong: QuizItem? = null
    private var handler = Handler()

    private val PREF_SCORE = "scorePrefs"
    private val ROUND_SCORE = "roundScore"
    private val RECORD_SCORE = "recordScore"

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private var roundPoints = 0
    private var recordScore = 0
    private var timesRepeat = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initGame()
    }

    override fun onRestart() {
        super.onRestart()
        startRound()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }

    private fun loadSongs() {
        songsList.add(QuizItem(UUID.randomUUID(), "Harlem Shake", "harlemshake"))
        songsList.add(QuizItem(UUID.randomUUID(), "Love and Marriage", "loveandmarriage"))
        songsList.add(QuizItem(UUID.randomUUID(), "Norwegian Wood", "norwegianwood"))
        songsList.add(QuizItem(UUID.randomUUID(), "Roxanne", "roxanne"))
        songsList.add(QuizItem(UUID.randomUUID(), "Royals", "royals"))
        songsList.add(QuizItem(UUID.randomUUID(), "Space Oddity", "spaceoddity"))
        songsList.add(QuizItem(UUID.randomUUID(), "Sweet Dreams", "sweetdreams"))
        songsList.add(QuizItem(UUID.randomUUID(), "Telacuti", "telacuti"))
        songsList.add(QuizItem(UUID.randomUUID(), "This Is the Life", "thisisthelife"))
        songsList.add(QuizItem(UUID.randomUUID(), "Tired", "tired"))
    }

    private fun initGame() {
        prefs = applicationContext.getSharedPreferences(PREF_SCORE, Context.MODE_PRIVATE)
        editor = prefs!!.edit()

        ibReplaySong.setOnClickListener {
            correctSong?.resource?.let { it1 ->
                playSong(it1)
                timesRepeat += 1
            }
        }

        roundPoints = prefs!!.getInt(ROUND_SCORE, 0)
        recordScore = prefs!!.getInt(RECORD_SCORE, 0)
        tvScore.text = "Score: ${roundPoints} points"

        loadSongs()
        startRound()
    }

    private fun startRound() {
        roundSongsList = shuffleSongs()
        val i = Random.nextInt(0, 3)
        correctSong = roundSongsList[i]

        tvOption1.text = roundSongsList[0].name
        tvOption2.text = roundSongsList[1].name
        tvOption3.text = roundSongsList[2].name

        tvOption1.setOnClickListener {
            checkAnswer(roundSongsList[0])
        }

        tvOption2.setOnClickListener {
            checkAnswer(roundSongsList[1])
        }

        tvOption3.setOnClickListener {
            checkAnswer(roundSongsList[2])
        }

        playSong(correctSong!!.resource)
    }

    private fun checkAnswer(song: QuizItem) {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        handler.removeCallbacksAndMessages(null)


        if (song.id == correctSong!!.id) {
            val addedPoints = 100 - (timesRepeat*5)
            roundPoints += addedPoints
            tvScore.setTextColor(Color.GREEN)
            startRound()
        } else {
            if(roundPoints > recordScore) {
                recordScore = roundPoints
                editor?.putInt(RECORD_SCORE, roundPoints)
                editor?.apply()
                showRecordDialog()
            } else {
                startRound()
            }
            timesRepeat = 0
            roundPoints = 0
            tvScore.setTextColor(Color.RED)
        }

        tvScore.text = "Score: $roundPoints points"
        editor?.putInt(ROUND_SCORE, roundPoints)
        editor?.apply()

    }

    private fun shuffleSongs(): MutableList<QuizItem> {
        val copy = songsList
        copy.shuffle()
        return copy.subList(0, 3)
    }

    private fun playSong(songName: String) {

        val resID = resources.getIdentifier(songName, "raw", packageName)
        mediaPlayer = MediaPlayer.create(applicationContext, resID)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer?.setAudioAttributes(audioAttributes)
        ibReplaySong.isClickable = false
        ibReplaySong.alpha = 0.25f
        mediaPlayer?.start()

        val startInSecond = Random.nextInt(0, mediaPlayer?.duration!!-11)
        mediaPlayer?.seekTo(startInSecond)

        handler.postDelayed({
            kotlin.run {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
                ibReplaySong.isClickable = true
                ibReplaySong.alpha = 1f
            }
        }, 10000)
    }

    private fun showRecordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("NEW RECORD")
        builder.setMessage("You've reached new personal record with $roundPoints!! Congratulations!!")
        builder.setPositiveButton("Continue"){ _,_ ->
            roundPoints = 0
            tvScore.setTextColor(Color.RED)
            tvScore.text = "Score: $roundPoints points"
            editor?.putInt(ROUND_SCORE, roundPoints)
            editor?.apply()
            startRound()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(true)
        alertDialog.show()
    }
}
