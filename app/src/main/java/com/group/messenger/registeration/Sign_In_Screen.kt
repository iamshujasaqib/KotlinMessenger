package com.group.messenger.registeration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.group.messenger.R
import com.group.messenger.messages.MessagesActivity

class Sign_In_Screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_screen)

        var et_email: EditText = findViewById(R.id.etsigninEmail)
        var et_password: EditText = findViewById(R.id.etsigninPassword)
        var signIn : Button = findViewById(R.id.signinButton)
        var register : TextView = findViewById(R.id.backtoregister)

        signIn.setOnClickListener({
            //Toast.makeText(this, "you clicked sign in button", Toast.LENGTH_SHORT).show()

            var email = et_email.text.toString()
            var password = et_password.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener({
                    if (!it.isSuccessful) return@addOnCompleteListener
                    // else if
                    //Log.d("Main","Successfully created user with uid: ${it.result?.user?.uid}")
                    //Toast.makeText(this, "Sign-in Successfull", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MessagesActivity::class.java))
                    finish()
                })

                .addOnFailureListener({
                    Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                })

        })

        // go back to registration screen
        register.setOnClickListener({
            finish()
        })
    }
}