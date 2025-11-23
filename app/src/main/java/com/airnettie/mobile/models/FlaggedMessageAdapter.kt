package com.airnettie.mobile.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airnettie.mobile.databinding.ItemFlaggedMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class FlaggedMessageAdapter(
    private val messages: List<FlaggedMessage>
) : RecyclerView.Adapter<FlaggedMessageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFlaggedMessageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFlaggedMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        val formattedTime = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.US)
            .format(Date(message.timestamp))

        val sourceIcon = when (message.source.lowercase()) {
            "features/sms" -> "📩"
            "chat" -> "💬"
            "web" -> "🌐"
            else -> "🧠"
        }

        holder.binding.messageText.text = message.text
        holder.binding.messageSeverity.text = "⚠️ Severity: ${message.severity ?: "Unknown"}"
        holder.binding.messageCategory.text = "🧭 Category: ${message.category ?: "Unknown"}"
        holder.binding.messageSource.text = "$sourceIcon Source: ${message.sourceApp.ifBlank { message.source }}"
        holder.binding.messageTimestamp.text = "🕒 $formattedTime"
        holder.binding.messageMatched.text = "🔍 Matched: ${message.matchedItems.joinToString(", ")}"

        holder.binding.messageNotes.text = if (message.notes.isBlank()) {
            "📝 No notes added."
        } else {
            "📝 ${message.notes}"
        }

        if (message.isEscalated) {
            holder.binding.messageEscalation.text = "🚨 Escalated"
            holder.binding.messageEscalation.visibility = View.VISIBLE
        } else {
            holder.binding.messageEscalation.visibility = View.GONE
        }

        holder.binding.messageDeflection.text = message.deflectionUsed?.let {
            "🛡️ Deflection: \"$it\""
        } ?: ""
    }

    override fun getItemCount(): Int = messages.size
}