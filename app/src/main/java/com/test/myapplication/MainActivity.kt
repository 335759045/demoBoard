package com.test.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.kw.lib_board.Board.BoardLayout
import com.kw.lib_board.Listener.OnTextInputShowListener
import com.kw.lib_board.boardenum.BoardAction
import com.kw.lib_board.layout.MScrollView
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy


class MainActivity : AppCompatActivity(), View.OnClickListener, MScrollView.OnScrollListener,
    OnTextInputShowListener, InputTextMsgDialog.OnTextSendListener {
    private val permissionManifest=arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val REQUEST_CODE_CHOOSE = 23
    private val editPop by lazy { InputTextMsgDialog(this, R.style.class_dialog_center) }
    private lateinit var scrollView: MScrollView
    private lateinit var boardView: BoardLayout
    private var boardParams: RelativeLayout.LayoutParams?=null//黑板布局

    private val requestPermissionLauncher =registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        val grantedSize = it.filterValues {it}.size
        if(grantedSize!=permissionManifest.size){
            Toast.makeText(this,"用户拒绝了权限可能导致部分功能无法使用", Toast.LENGTH_SHORT)
        }else {//通过权限

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        requestPermissionLauncher.launch(permissionManifest)
        scrollView=findViewById(R.id.scrollView)
        boardView=findViewById(R.id.board_view)

        findViewById<TextView>(R.id.tv1).setOnClickListener(this)
        findViewById<TextView>(R.id.tv2).setOnClickListener(this)
        findViewById<TextView>(R.id.tv3).setOnClickListener(this)
        findViewById<TextView>(R.id.tv4).setOnClickListener(this)
        findViewById<TextView>(R.id.tv5).setOnClickListener(this)
        findViewById<TextView>(R.id.tv6).setOnClickListener(this)
        findViewById<TextView>(R.id.tv7).setOnClickListener(this)
        boardView.setOnTextInputShowListener(this)
        scrollView.setOnScrollListener(this)
        editPop.setmOnTextSendListener(this)

        boardParams = boardView.layoutParams as RelativeLayout.LayoutParams //取控件黑板的布局参数
        boardParams!!.width = Util.getScreenInfo(this).widthPixels
        boardParams!!.height=Util.getScreenInfo(this).heightPixels*50//50倍黑板
        boardView.layoutParams = boardParams!!
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.tv1 -> {
                scrollView.setScroll(false)
                boardView.setAction(BoardAction.ACTION_CHOOSE)
            }
            R.id.tv2 -> {
                scrollView.setScroll(false)
                boardView.setAction(BoardAction.ACTION_DRAW)
            }
            R.id.tv3 -> {
                scrollView.setScroll(false)
                boardView.setAction(BoardAction.ACTION_DRAW_TEXT)
            }
            R.id.tv4 -> boardView.removeChooseView()
            R.id.tv5 -> {
                boardView.setAction(BoardAction.ACTION_CHOOSE_IMAGE)
                zhihu()
            }
            R.id.tv6 -> {
                scrollView.setScroll(true)
            }
            R.id.tv7 ->{
                boardView.addNewBoard()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            val bitmap = getBitmap(Matisse.obtainPathResult(data)[0])
            if (bitmap != null) {
                boardView.setImageView(bitmap)
            }
        }
    }
    private fun getBitmap(filePath: String?): Bitmap? {
        return if (filePath == null || filePath.trim { it <= ' ' }.isEmpty()) null else BitmapFactory.decodeFile(
            filePath
        )
    }
    private fun zhihu(){
        Matisse.from(this)
            .choose(MimeType.ofImage(), true)
            .countable(true)
            .capture(true)
            .maxSelectable(1)
            .captureStrategy(
                CaptureStrategy(true, "$packageName.fileprovider")
            )
            .addFilter(
                GifSizeFilter(320, 320, 5 * Filter.K * Filter.K)
            )
            .gridExpectedSize(120)
            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            .thumbnailScale(0.85f) //              .imageEngine(new GlideEngine())  // for glide-V3
            .imageEngine(Glide4Engine()) // for glide-V4
            .showSingleMediaType(true)
            .originalEnable(true)
            .maxOriginalSize(10)
            .autoHideToolbarOnSingleTap(true)
            .forResult(REQUEST_CODE_CHOOSE)
    }

    override fun onScroll(scrollT: Int) {
        boardView.setPositionMarginTop(scrollT)
    }

    override fun onShowInputTextDialog(msg: String?) {
        editPop.setContentText(msg)
        editPop.show()
    }

    override fun onTextChange(msg: String?) {
    }

    override fun onTextSend(msg: String?) {
        boardView.setTextContent(msg, 50F)
    }
}