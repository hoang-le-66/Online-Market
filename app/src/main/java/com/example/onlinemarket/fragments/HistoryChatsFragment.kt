package com.example.onlinemarket.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.onlinemarket.R
import com.example.onlinemarket.adapters.AdapterHistoryChats
import com.example.onlinemarket.databinding.FragmentHistoryChatsBinding
import com.example.onlinemarket.models.ModelHistoryChats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HistoryChatsFragment : Fragment() {

    private lateinit var bindingHistoryChats: FragmentHistoryChatsBinding
    private companion object{
        private const val TAG = "CHATS_TAG"

    }

    private lateinit var firebaseAuth: FirebaseAuth
    private var myUid = ""

    private lateinit var mContext: Context
    private lateinit var historyChatsArrayList: ArrayList<ModelHistoryChats>

    private lateinit var adapterHistoryChats: AdapterHistoryChats


    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        bindingHistoryChats = FragmentHistoryChatsBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return bindingHistoryChats.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        myUid = "${firebaseAuth.uid}"
        Log.d(TAG, "onViewCreated: myUid: $myUid")

        loadHistoryChats()

        bindingHistoryChats.searchEdt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    val query = s.toString()
                    Log.d(TAG, "onTextChanged: Search Query: $query")
                    adapterHistoryChats.filter.filter(query)

                }catch (e: Exception){
                    Log.e(TAG, "onTextChanged: ", e)

                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })
    }

    private fun loadHistoryChats(){
        //init historyChatsArrayList before starting add data into it
        historyChatsArrayList = ArrayList()
        //Get the chats of logged-in user
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear historyChatsArrayList each time starting add data into it
                historyChatsArrayList.clear()
                //load chats based on chatKey
                for(ds in snapshot.children){
                    val chatKey = "${ds.key}"
                    Log.d(TAG, "onDataChange: chatKey: $chatKey")

                    if (chatKey.contains(myUid)){
                        Log.d(TAG, "onDataChange: Contains, Add to List")
                        val modelHistoryChats = ModelHistoryChats()
                        modelHistoryChats.chatKey = chatKey

                        historyChatsArrayList.add(modelHistoryChats)
                    }else{
                        Log.d(TAG, "onDataChange: Not contains, Skip")
                    }

                }

                adapterHistoryChats = AdapterHistoryChats(mContext, historyChatsArrayList)
                bindingHistoryChats.chatsRv.adapter = adapterHistoryChats
                //after loading data, sort the list based on timestamp to show latest chat
                sort()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun sort(){
        Handler().postDelayed({
            historyChatsArrayList.sortWith{model1: ModelHistoryChats, model2: ModelHistoryChats ->
                model2.timestamp.compareTo(model1.timestamp)
            }

            adapterHistoryChats.notifyDataSetChanged()
        },750)
    }
}