package com.lungyi.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.lungyi.myapplication.databinding.ActivityMainBinding

private const val CAMERA_REQUEST_CODE = 101
val mydatas: MutableList<ScanData> = mutableListOf()

class MainActivity : AppCompatActivity() {
    private lateinit var bind: ActivityMainBinding
    private lateinit var codeScanner: CodeScanner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        bind = ActivityMainBinding.inflate(layoutInflater)
        bind.rvResult.layoutManager = LinearLayoutManager(this)
        bind.rvResult.adapter = ResultAdapter(context = this, mydatas)

        //super.onCreate(savedInstanceState)
        setContentView(bind.root)
        setupPermissions()
        codeScanner()
        setButton()
    }

    private fun setButton() {

        bind.clearAll.setOnClickListener() {
            clearAll()
        }
        bind.btn.setOnClickListener()
        {
            addResult()

        }
        bind.btnDel.setOnClickListener() {
            delLastOne()
        }

    }

    private fun clearAll() {
        mydatas.clear()
        bind.tvRepeat.text = ""
        bind.rvResult.layoutManager = LinearLayoutManager(this)
        bind.rvResult.adapter = ResultAdapter(context = this, mydatas)
    }

    private fun addResult() {

        //新增到list 中

        val result = bind.tvResult.text.toString()

        if (mydatas.isEmpty()) {
            mydatas.add(ScanData(1, result))

            bind.tvRepeat.text = ""
            bind.rvResult.layoutManager = LinearLayoutManager(this)
            bind.rvResult.adapter = ResultAdapter(context = this, mydatas)
        } else {
            for (item in mydatas) {
                if (result.equals(item.content)) {
                    //find repeated
                    //Toast.makeText(this, "Repeat Found in ${item.serNum}", Toast.LENGTH_SHORT).show()                        .show()

                    bind.tvRepeat.text = "第${mydatas.size + 1}筆 和 第${item.serNum}筆的資料相同"
                } else {
                    bind.tvRepeat.text = ""
                }

            }

            mydatas.add(ScanData(mydatas.size + 1, result))
            bind.rvResult.layoutManager = LinearLayoutManager(this)
            bind.rvResult.adapter = ResultAdapter(context = this, mydatas)
            bind.rvResult.scrollToPosition(mydatas.size - 1)
        }

    }

    private fun delLastOne() {
        Log.d("daniel", "mydata size =" + mydatas.size)
        if (!mydatas.isEmpty()) {
            mydatas.removeLast()
        }
        Log.d("daniel", "mydata size =" + mydatas.size)
        bind.rvResult.layoutManager = LinearLayoutManager(this)
        bind.rvResult.adapter = ResultAdapter(context = this, mydatas)
        bind.tvRepeat.text = ""
        bind.rvResult.scrollToPosition(mydatas.size - 1)
    }

    private fun codeScanner() {
        //showCamera = true
        // bind = ActivityMainBinding.inflate(layoutInflater)
        codeScanner = CodeScanner(this, bind.scanView)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false
            decodeCallback = DecodeCallback {
                runOnUiThread {
                    bind.tvResult.text = it.text
                    //addResult()
                }
            }
            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main", "Camera initialization error:${it.message}")

                }
            }
        }
        bind.scanView.setOnClickListener {
            codeScanner.startPreview()
        }


    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()

    }

    private fun setupPermissions() {
        val permission =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.d("daniel", "before ask camera permission")
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
            //Log.d("daniel", "after ask camera permission")
        } else {
            //Log.d("daniel", "no need to ask camera permission")
        }
    }

    private fun makeRequest() {
        Log.d("dainel", "make request")
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )

    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(

        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "your need the camera permission to be able to use this app!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}


class ResultAdapter(val context: Context, val scanResult: MutableList<ScanData>) :
    RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

    //Creat a new view - EXPENSIVE
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout, parent, false)
        return ViewHolder(view)
    }

    //Bind the data at position into the view holder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result: ScanData = scanResult[position]
        holder.bind(result)
    }

    // HOW MANY  DATASETS ARE IN THS LIST
    override fun getItemCount() = scanResult.size

    inner class ViewHolder(itemViw: View) : RecyclerView.ViewHolder(itemViw) {
        var tvNum = itemViw.findViewById<TextView>(R.id.tvNum)
        var tvResult = itemViw.findViewById<TextView>(R.id.tvResult)
        fun bind(result: ScanData) {
            tvNum.text = result.serNum.toString()
            tvResult.text = result.content
        }
    }
}