package com.sensoguard.hunter.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.sensoguard.hunter.R
import com.sensoguard.hunter.classes.CryptoHandler
import com.sensoguard.hunter.global.ACTIVATION_CODE_KEY
import com.sensoguard.hunter.global.IMEI_KEY
import com.sensoguard.hunter.global.IS_LOAD_APP
import com.sensoguard.hunter.global.setStringInPreference
import org.apache.commons.lang3.StringUtils


class ActivationActivity : AppCompatActivity() {

    private var myImei: String?=null
    var tvImei: AppCompatTextView?=null
    var btnShare: AppCompatButton?=null
    var etEnterCode:AppCompatEditText?=null
    var btnSignIn:AppCompatButton?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activation)

        init()

        myImei=intent.getStringExtra(IMEI_KEY)

        tvImei?.text = myImei


    }

    fun init(){
        tvImei = findViewById(R.id.tvImei)
        btnShare = findViewById(R.id.btnShare)

        onClickShare()

        btnSignIn= findViewById(R.id.btnSignIn)

        onClickSignIn()

        etEnterCode=findViewById(R.id.etEnterCode)


        etEnterCode?.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do something, e.g. set your TextView here via .setText()
                    val imm=v?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)

                    return true
                }
                return false
            }
        })
    }

    private fun onClickSignIn() {
        btnSignIn?.setOnClickListener{

            if (!validIsEmpty(etEnterCode)) {
                return@setOnClickListener
            }

            val myActivateCode=CryptoHandler.getInstance().encrypt(myImei)
            //if the activate code that came from user is equal to activate code that calculated by IMEI ,then start the app
            val tmp=etEnterCode?.text.toString()

            val myActivateCodeWhitespace = StringUtils.deleteWhitespace(myActivateCode)

            if (myActivateCodeWhitespace == tmp) {
                setStringInPreference(applicationContext,ACTIVATION_CODE_KEY,etEnterCode?.text.toString())
                val inn = Intent(this, MainActivity::class.java)
                inn.putExtra(IS_LOAD_APP, true)
                inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(Intent(inn))
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.wrong_activate_code),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //check if the field of edit text is empty
    private fun validIsEmpty(editText: AppCompatEditText?): Boolean {
        var isValid = true

        if (editText?.text.isNullOrBlank()) {
            editText?.error = resources.getString(R.string.empty_field_error)
            isValid = false
        }

        return isValid
    }


    private fun onClickShare() {
        btnShare?.setOnClickListener{
            //ic_share the IMEI of the device
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            val shareBody = myImei
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.email_subject))
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        }
    }



}
