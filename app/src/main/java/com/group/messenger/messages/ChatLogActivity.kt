package com.group.messenger.messages

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.group.messenger.R
import com.group.messenger.registeration.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class ChatLogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<GroupieViewHolder>()
    lateinit var recyclerview_chatLog : RecyclerView
    lateinit var send:Button
    lateinit var messageBody:EditText

    var toUser : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        recyclerview_chatLog = findViewById(R.id.chatLogRecyclerView)


        recyclerview_chatLog.adapter = adapter
        recyclerview_chatLog = findViewById(R.id.chatLogRecyclerView)
        send = findViewById(R.id.chatLogSendButton)
        messageBody = findViewById(R.id.chatLogMessageBox)

        //val username = intent.getStringExtra(NewMessageActivity.USER_KEY) // receiving companion object

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

        if (toUser != null) {
            supportActionBar?.title= toUser!!.username
        }

        //showDummyData()

        listenForMessages()

        send.setOnClickListener({
            performSendMessageOperation()
        })

    }// End of On Create Function

    // Private Functions

//    private fun showDummyData()
//    {
//        adapter.add(ChatFromItem("Hi"))
//        adapter.add(ChatToItem("Assalam Alikum"))
//        adapter.add(ChatFromItem("Walikum Assalam"))
//        adapter.add(ChatToItem("Kaifa haaluka?"))
//        adapter.add(ChatFromItem("Mai theek Alhamduillah. Tum sunao?"))
//        adapter.add(ChatToItem("Mai b theek ALHAMDUILLAH"))
//        adapter.add(ChatFromItem("wyd?"))
//        adapter.add(ChatToItem("Project kr raha"))
//        adapter.add(ChatFromItem("MAD ya WAD?"))
//        adapter.add(ChatToItem("mad"))
//        adapter.add(ChatFromItem("Kitna ho gaya?"))
//        adapter.add(ChatToItem("50% hua hai"))
//    }

    //private fun performSendMessageOperation(view : Context)
    private fun performSendMessageOperation()
    {
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val message = messageBody.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val toId = user?.uid
        if (fromId==null) return
        if (toId==null) return
        //val reference = FirebaseDatabase.getInstance().getReference("/messages").push() // All users get the same message
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()  // user to user message
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()  // user to user message
        val chatMessage = ChatMessage(reference.key!!,message,fromId!!,toId,System.currentTimeMillis()/1000)
        //reference.setValue("Test Message")
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                //Toast.makeText(view, "Message Saved in Database, ${reference.key}", Toast.LENGTH_SHORT).show()
                messageBody.text.clear()
                recyclerview_chatLog.scrollToPosition(adapter.itemCount-1)
            }
        toReference.setValue(chatMessage)

        val inboxMessageReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        inboxMessageReference.setValue(chatMessage)

        val inboxMessageToReference = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        inboxMessageToReference.setValue(chatMessage)
    }

    private fun listenForMessages()
    {
        val toId = FirebaseAuth.getInstance().uid
        val fromId = toUser?.uid
        //val reference = FirebaseDatabase.getInstance().getReference("/messages")
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        reference.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                //val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

                if(chatMessage!=null && toUser != null)
                {
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid)  // Means that the message is from our device (The Logged-In User)
                    {
                        val currentUser = MessagesActivity.currentUser ?: return   // force unwrapping
                        //adapter.add(ChatToItem(chatMessage.text, toUser))
                        adapter.add(ChatToItem(chatMessage.text, currentUser))
                    }
                    else
                    {
                        adapter.add(ChatFromItem(chatMessage.text, toUser!!))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }
}

// Private Classes

class ChatFromItem(val text:String,val user:User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

       // viewHolder.itemView.findViewById<TextView>(R.id.chatBoxMessage).text = "From Message..."
        viewHolder.itemView.findViewById<TextView>(R.id.chatBoxMessage).text = text

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.chatBoxProfilePic))
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

class ChatToItem(val text:String, val user:User) : Item<GroupieViewHolder>(){
//class ChatToItem(val text:String) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        //viewHolder.itemView.findViewById<TextView>(R.id.chatBoxMessageTo).text="To Message..."
        viewHolder.itemView.findViewById<TextView>(R.id.chatBoxMessageTo).text=text
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.chatBoxProfilePicTo))

        // Loading the Profile Picture
        //Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.chatBoxProfilePicTo))

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

class ChatMessage(val id : String, val text : String, val fromId : String, val toId : String , val timestamp : Long)
{
    constructor() : this("","","","",-1 )
}
