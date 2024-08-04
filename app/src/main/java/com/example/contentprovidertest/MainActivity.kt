package com.example.contentprovidertest

import android.Manifest
import android.content.ContentUris
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<ImageViewModel>()
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Rejestracja ActivityResultLauncher do obsługi usuwania zdjęć
        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK) {
                Toast.makeText(this@MainActivity, "Photo deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Photo couldn't be deleted", Toast.LENGTH_SHORT).show()
            }
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            0
        )

        val images = mutableListOf<Image>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )
        val milisYesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis
        val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
        val selectionArgs = arrayOf(milisYesterday.toString())
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                images.add(Image(id, name, uri))
            }
            viewModel.updateImages(images)
        }

        if (images.isNotEmpty()) {
            val imageV = findViewById<ImageView>(R.id.img)
            imageV.setImageURI(images[0].uri)
            val textV = findViewById<TextView>(R.id.txt).apply {
                text = images[0].name
            }

            findViewById<Button>(R.id.btnDel).setOnClickListener {
                try {
                    val deleteRequest = MediaStore.createDeleteRequest(contentResolver, listOf(images[0].uri))
                    intentSenderLauncher.launch(IntentSenderRequest.Builder(deleteRequest).build())
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(this, "No images found", Toast.LENGTH_LONG).show()
        }
    }
}

data class Image(
    val id: Long,
    val name: String,
    val uri: Uri
)
