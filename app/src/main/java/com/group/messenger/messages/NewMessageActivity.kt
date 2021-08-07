package com.group.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.group.messenger.R
import com.group.messenger.registeration.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class NewMessageActivity : AppCompatActivity() {
    lateinit var recycleView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)


        supportActionBar?.title="Select User"

        recycleView = findViewById(R.id.newMessageRecycleView)

        fetchUsers()

    }
    companion object{
        val USER_KEY = "USER_KEY"
    }
    private fun fetchUsers()
    {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot)
            {
                var adapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach{
                    val user = it.getValue(User::class.java)
                    if(user != null)
                    {
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as UserItem

                    val intent:Intent = Intent(view.context,ChatLogActivity::class.java)
                    //intent.putExtra(USER_KEY,userItem.user.username)
                    intent.putExtra(USER_KEY,userItem.user)

                    startActivity(intent)
                    finish()

                }

                recycleView.adapter = adapter
            }



            override fun onCancelled(error: DatabaseError)
            {
                // code here
            }

        })
    }
}


// Private Class

class UserItem(val user: User):Item<GroupieViewHolder>()
{
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // will be called in our list for each user object later on...
        viewHolder.itemView.findViewById<TextView>(R.id.userName).text = user.username

        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.findViewById<CircleImageView>(
            R.id.userPic
        ))
    }
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}