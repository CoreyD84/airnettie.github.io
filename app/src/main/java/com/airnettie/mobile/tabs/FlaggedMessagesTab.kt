package com.airnettie.mobile.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airnettie.mobile.R
import com.airnettie.mobile.models.FlaggedMessage
import com.airnettie.mobile.models.FlaggedMessageAdapter

class FlaggedMessagesTab : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyState: TextView
    private lateinit var refreshButton: Button

    private val flaggedMessages = mutableListOf<FlaggedMessage>()
    private lateinit var adapter: FlaggedMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.tab_flagged_messages, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyState = view.findViewById(R.id.emptyState)
        refreshButton = view.findViewById(R.id.refreshButton)

        adapter = FlaggedMessageAdapter(flaggedMessages)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        refreshButton.setOnClickListener {
            loadFlaggedMessages()
        }

        loadFlaggedMessages()
        return view
    }

    private fun loadFlaggedMessages() {
        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE

        recyclerView.postDelayed({
            progressBar.visibility = View.GONE
            flaggedMessages.clear()

            // Simulated flagged phrases
            val rawMessages = listOf("I'm scared", "I hate myself", "Nobody cares")

            flaggedMessages.addAll(rawMessages.map { text ->
                FlaggedMessage(
                    text = text,
                    severity = null,
                    category = null,
                    matchedItems = listOf(text),
                    source = "sms",
                    sourceApp = "Messenger",
                    timestamp = System.currentTimeMillis()
                )
            })

            if (flaggedMessages.isEmpty()) {
                emptyState.visibility = View.VISIBLE
            } else {
                adapter.notifyDataSetChanged()
                recyclerView.visibility = View.VISIBLE
            }
        }, 1000)
    }
}