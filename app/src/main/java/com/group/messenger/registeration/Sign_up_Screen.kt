package com.group.messenger.registeration

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.group.messenger.R
import com.group.messenger.messages.MessagesActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.parcel.Parcelize
import java.util.*

class Sign_up_Screen : AppCompatActivity() {
    lateinit var  uploadImage : ImageButton
    lateinit var et_username : EditText
    lateinit var  imageCircularUpload : CircleImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_screen)

       uploadImage = findViewById(R.id.uploadImageButton)
        et_username = findViewById(R.id.etUsername)
        imageCircularUpload=findViewById(R.id.circularImage)

        var et_email:EditText = findViewById(R.id.etEmail)
        var et_password:EditText = findViewById(R.id.etPassword)
        var signIn : TextView = findViewById(R.id.signintv)
        var signUp : Button = findViewById(R.id.signupButton)

        // 1- Image Listener

        uploadImage.setOnClickListener({
            //Toast.makeText(this, "you clicked image button", Toast.LENGTH_SHORT).show()

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        })

        // 2- Sign-in

        signIn.setOnClickListener({
            startActivity(Intent(this, Sign_In_Screen::class.java))
        })


        // 3- Sign-Up / Register

        signUp.setOnClickListener({    // Register Button

            var email:String = et_email.text.toString()
            var password:String = et_password.text.toString()

            signUpUser(email,password)
        })
    }


    // Private Functions

    private fun signUpUser(email:String , password:String)
    {
        if(email.isEmpty() || password.isEmpty())  return

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener({
                if (!it.isSuccessful) return@addOnCompleteListener
                // else if
                //Log.d("Main","Successfully created user with uid: ${it.result?.user?.uid}")
                Toast.makeText(this, "Registration Successfull", Toast.LENGTH_SHORT).show()
                uploadImageToFirebaseStorage()

                startActivity(Intent(this, MessagesActivity::class.java))
                finish()
            })

            .addOnFailureListener({
                Toast.makeText(this, "Invalid Email string", Toast.LENGTH_SHORT).show()
            })
    }

    private fun uploadImageToFirebaseStorage()
    {
        if(selectedPhotoUri==null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                //Toast.makeText(baseContext, "Image uploaded successfully", Toast.LENGTH_SHORT).show()

                ref.downloadUrl.addOnSuccessListener {
                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener({

            })
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String)
    {
        var uid = FirebaseAuth.getInstance().uid?:""
        var ref = FirebaseDatabase.getInstance().getReference("users/$uid")

        var user = User(uid,et_username.text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {

                /*
                val intent = Intent(this,MessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                */
                startActivity(Intent(this, MessagesActivity::class.java))
                finish()
            }
            .addOnFailureListener({
                Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
            })
    }

    // Saved Variable

    var selectedPhotoUri :Uri? = null


    // Override Functions

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==0 && resultCode== Activity.RESULT_OK && data!=null)
        {
            // It will represent the location of image stored in the device
            //var uri = data?.data
            selectedPhotoUri = data?.data
            // We can use this "uri" to get access to the image as a bitmap

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)

            imageCircularUpload.setImageBitmap(bitmap)
            uploadImage.alpha=0f

//            var bitmapDrawable = BitmapDrawable(bitmap)
//            uploadImage.setBackgroundDrawable(bitmapDrawable)
        }
    }
}

// Private Class
@Parcelize
class User(var uid:String, var username:String, var profileImageUrl:String) : Parcelable
{
    constructor() : this("","","")
}
