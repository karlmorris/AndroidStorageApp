package edu.temple.storageapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.SharedPreferences
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.addTextChangedListener

import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import java.lang.StringBuilder
import java.lang.Exception

private const val AUTO_SAVE_KEY = "auto_save"

class MainActivity : AppCompatActivity() {

    private lateinit var textBox: EditText
    private lateinit var checkBox: CheckBox

    private var autoSave = false

    private lateinit var preferences: SharedPreferences

    private val internalFilename = "my_file"
    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get preferences for this component
        preferences = getPreferences(MODE_PRIVATE)

        // Create file reference for app-specific file
        file = File(filesDir, internalFilename)

        textBox = findViewById(R.id.editText)
        checkBox = findViewById(R.id.checkBox)

        // Read last saved value from preferences, or false if no value saved
        autoSave = preferences.getBoolean(AUTO_SAVE_KEY, false)

        // Set checkbox to last value saved
        checkBox.isChecked = autoSave

        // Load data to edittext if save option was enabled
        if (autoSave && file.exists()) {
            try {
                val br = BufferedReader(FileReader(file))
                val text = StringBuilder()
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    text.append(line)
                    text.append('\n')
                }
                br.close()
                textBox.setText(text.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            autoSave = isChecked

            // Update shared preferences when toggled
            val editor = preferences.edit()
            editor.putBoolean(AUTO_SAVE_KEY, autoSave)
            editor.apply()
        }

        // Save the file after every keystroke (obviously horrible)
        // if AutoSave is enabled
        textBox.addTextChangedListener {
            if (autoSave) {
                try {
                    val outputStream = FileOutputStream(file)
                    outputStream.write(it.toString().toByteArray())
                    outputStream.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Delete file if auto save is turned off
        if (!autoSave)
            file.delete()
    }
}