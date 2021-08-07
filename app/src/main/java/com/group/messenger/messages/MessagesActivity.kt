package com.group.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.group.messenger.R
import com.group.messenger.messages.NewMessageActivity.Companion.USER_KEY
import com.group.messenger.registeration.Sign_up_Screen
import com.group.messenger.registeration.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class MessagesActivity : AppCompatActivity() {

    lateinit var inbox_recycler_view : RecyclerView
    val adapter = GroupAdapter<GroupieViewHolder>()

    val latestMessagesMap = HashMap<String, ChatMessage>()  // for replacing messages over and over again

    companion object {
        var currentUser : User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)
        supportActionBar?.title="Inbox"
        inbox_recycler_view = findViewById(R.id.inbox_recyclerView)

        inbox_recycler_view.adapter = adapter

        inbox_recycler_view.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))



        //setupDummyRows()
        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this,ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }
        verifyUserLogIn()
        fetchCurrentUser()
        ListenForLatestMessages()



    }


    // Private Functions

    private fun refreshRecyclerViewMessages(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun ListenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
                //adapter.add(LatestMessageRow(chatMessage))
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

//    private fun setupDummyRows()
//    {
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//        adapter.add(LatestMessageRow())
//    }

    private fun fetchCurrentUser()
    {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun verifyUserLogIn()
    {
        val uid = FirebaseAuth.getInstance().uid
        if(uid==null)  // means that no user is logged in
        {
            startActivity(Intent(this, Sign_up_Screen::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId)
        {
            R.id.menu_new_message ->
            {
                //Toast.makeText(baseContext, "You clicked New Message", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, NewMessageActivity::class.java))
            }

            R.id.menu_sign_out ->
            {
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, Sign_up_Screen::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

// private class

class LatestMessageRow(val chatMessage : ChatMessage) : Item<GroupieViewHolder>(){
    var chatPartnerUser : User? = null
    override fun getLayout(): Int {
        return R.layout.inbox_rows
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.inbox_latest_message).text = chatMessage.text
        val partnerId:String
        if(chatMessage.fromId == FirebaseAuth.getInstance().uid)
        {
            partnerId= chatMessage.toId
        }
        else
        {
            partnerId = chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$partnerId")
        ref.addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.findViewById<TextView>(R.id.inbox_username).text = chatPartnerUser?.username
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(R.id.inbox_pic))
            }
        })
    }
}