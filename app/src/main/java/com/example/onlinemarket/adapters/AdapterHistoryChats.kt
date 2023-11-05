package com.example.onlinemarket.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.onlinemarket.FilterChats
import com.example.onlinemarket.R
import com.example.onlinemarket.Utils
import com.example.onlinemarket.activities.ChatActivity
import com.example.onlinemarket.databinding.RowChatsBinding
import com.example.onlinemarket.models.ModelHistoryChats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterHistoryChats : RecyclerView.Adapter<AdapterHistoryChats.HolderHistoryChats>, Filterable{

    private var context: Context
    var chatsHistoryArrayList: ArrayList<ModelHistoryChats>
    private var filterList: ArrayList<ModelHistoryChats>

    private var filter: FilterChats? = null

    private lateinit var bindingRowChat: RowChatsBinding

    private companion object{
        private const val TAG = "ADAPTER_CHATS_TAG"
    }

    private var firebaseAuth: FirebaseAuth

    private var myUid = ""

    constructor(context: Context, chatsArrayList: ArrayList<ModelHistoryChats>) {
        this.context = context
        this.chatsHistoryArrayList = chatsArrayList
        this.filterList = chatsArrayList

        firebaseAuth = FirebaseAuth.getInstance()

        myUid = "${firebaseAuth.uid}"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderHistoryChats {
        bindingRowChat = RowChatsBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderHistoryChats(bindingRowChat.root)
    }

    override fun onBindViewHolder(holder: HolderHistoryChats, position: Int) {
        val modelHistoryChats = chatsHistoryArrayList[position]

        loadLastMessage(modelHistoryChats, holder)

        holder.itemView.setOnClickListener {
            val receiptUid = modelHistoryChats.receiptUid
            if (receiptUid != null){
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("receiptUid", receiptUid)
                context.startActivity(intent)
            }

        }
    }

    private fun loadLastMessage(modelHistoryChats: ModelHistoryChats, holder: AdapterHistoryChats.HolderHistoryChats) {
        val chatKey = modelHistoryChats.chatKey
        Log.d(TAG, "loadLastMessage: chatKey: $chatKey")

        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatKey).limitToLast(1)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){

                        val fromUid = "${ds.child("fromUid").value}"
                        val message = "${ds.child("message").value}"
                        val messageId = "${ds.child("messageId").value}"
                        val messageType = "${ds.child("messageType").value}"
                        val timestamp = ds.child("timestamp").value as? Long ?: 0
                        val toUid = "${ds.child("toUid").value}"

                        val formattedDate = Utils.formatTimestampMessage(timestamp)

                        modelHistoryChats.fromUid = fromUid
                        modelHistoryChats.messageId = messageId
                        modelHistoryChats.messageType = messageType
                        modelHistoryChats.message = message
                        modelHistoryChats.timestamp = timestamp
                        modelHistoryChats.toUid = toUid

                        holder.dateTimeTv.text = "$formattedDate"

                        if (messageType == Utils.MESSAGE_TYPE_TEXT){
                            holder.lastMessageTv.text = message
                        }else{
                            holder.lastMessageTv.text = "Sends Attachment"
                        }

                    }

                    loadReceiptUserInfo(modelHistoryChats, holder)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadReceiptUserInfo(modelHistoryChats: ModelHistoryChats, holder: HolderHistoryChats) {

        val fromUid = modelHistoryChats.fromUid
        val toUid = modelHistoryChats.toUid

        var receiptUid = ""
        if (fromUid == myUid){
            receiptUid = toUid
        }else{
            receiptUid = fromUid
        }

        Log.d(TAG, "loadReceiptUserInfo: fromUid: $fromUid")
        Log.d(TAG, "loadReceiptUserInfo: toUid: $toUid")
        Log.d(TAG, "loadReceiptUserInfo: receiptUid: $receiptUid")
        //set receiptUid to current instance of ModelHistoryChats using setters
        modelHistoryChats.receiptUid = receiptUid

        val ref = FirebaseDatabase.getInstance().getReference("Users")

        ref.child(receiptUid)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageUrl").value}"

                    modelHistoryChats.name = name
                    modelHistoryChats.profileImageUrl = profileImageUrl
                    

                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.icon_person_white)
                            .into(holder.profileIv)
                    }catch (e: Exception){
                        Log.e(TAG, "onDataChange: ", e)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterChats(this, filterList)
        }
        return filter!!
    }

    override fun getItemCount(): Int {
        return chatsHistoryArrayList.size
    }

    inner class HolderHistoryChats(itemView: View) : RecyclerView.ViewHolder(itemView){
        var profileIv = bindingRowChat.profileIv
        var nameTv = bindingRowChat.nameTv
        var lastMessageTv = bindingRowChat.lastMessageTv
        var dateTimeTv = bindingRowChat.dateTimeTv
    }


}