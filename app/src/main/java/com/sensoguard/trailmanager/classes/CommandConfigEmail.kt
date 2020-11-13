package com.sensoguard.trailmanager.classes

import android.app.Dialog
import android.content.Context
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import com.sensoguard.trailmanager.R

class CommandConfigEmail {

    var email: String = ""
    var password: String = ""
    var port: String = ""
    var server: String = ""
    var commands = ArrayList<String>()


    fun populateCommands(): ArrayList<String> {
        commands.add("#s#$email#$password#$port#sphone#$server###1#")
        commands.add("#s#$email#$password#$port#internet.golantelecom.net.il#$server###1#")
        commands.add("#s#$email#$password#$port#uinternet#$server#orange#orange#1#")
        commands.add("#s#$email#$password#$port#sphone.pelephone.net.il#$server#pcl@3g#pcl#1#")
        commands.add("#s#$email#$password#$port#net.hotm#$server###1#")
        commands.add("#s#$email#$password#$port#netazi#$server###1#")
        commands.add("#s#$email#$password#$port#internet.rl#$server#rl@3g#rl#1#")
        commands.add("#s#$email#$password#$port#data.youphone.co.il#$server###1#")
        commands.add("#s#$email#$password#$port#uwap.orange.co.il#$server###1#")
        commands.add("#s#$email#$password#$port#we#$server###1#")
        return commands
    }

    //show dialog with email details
    fun showPhoneNumbersDialog(context: Context?) {

        if (context != null) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_dialog_configuration_email_send)

            dialog.setCancelable(true)


            val etMailAddress = dialog.findViewById<AppCompatEditText>(R.id.etMailAddress)
            val etPassword = dialog.findViewById<AppCompatEditText>(R.id.etPassword)
            val etMailServer = dialog.findViewById<AppCompatEditText>(R.id.etMailServer)
            val etMailServerPort = dialog.findViewById<AppCompatEditText>(R.id.etMailServerPort)
            val btnSendCommand = dialog.findViewById<AppCompatButton>(R.id.btnSendCommand)

            //add automatically when set address as gmail
            etMailAddress?.setOnFocusChangeListener { view, hasFocus ->

                if (!hasFocus) {
                    if ((view as EditText).text.toString().contains("@gmail.com")) {
                        //fill automatically
                        etMailServer?.setError("", null)
                        etMailServer?.setText("smtp.gmail.com")
                        etMailServerPort?.setError("", null)
                        etMailServerPort?.setText("465")
                    }
                }
            }

            btnSendCommand.setOnClickListener {

                if (validIsEmpty(etMailServer!!, context)
                    and validIsEmpty(etMailServerPort!!, context)
                    and validIsEmpty(etMailAddress!!, context)
                    and validIsEmpty(etPassword!!, context)
                ) {
                    email = etMailAddress.text.toString()
                    password = etPassword.text.toString()
                    port = etMailServerPort.text.toString()
                    server = etMailServer.text.toString()

                    populateCommands()
                    dialog.dismiss()
                }

            }
            val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancel)
            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()

        }
    }

    //check if the fields is empty
    private fun validIsEmpty(editText: EditText, context: Context): Boolean {
        var isValid = true

        if (editText.text.isNullOrBlank()) {
            editText.error =
                context.resources.getString(R.string.empty_field_error)
            isValid = false
        }

        return isValid
    }
}