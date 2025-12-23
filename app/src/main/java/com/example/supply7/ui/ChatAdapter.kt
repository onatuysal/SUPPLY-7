package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supply7.R
import com.example.supply7.data.Message

class ChatAdapter(
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages: List<Message> = emptyList()

    companion object {
        const val VIEW_TYPE_SENDER = 1
        const val VIEW_TYPE_RECEIVER = 2
        const val VIEW_TYPE_OFFER_SENDER = 3
        const val VIEW_TYPE_OFFER_RECEIVER = 4
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.senderId == currentUserId) {
            if (message.type == "offer") VIEW_TYPE_OFFER_SENDER else VIEW_TYPE_SENDER
        } else {
            if (message.type == "offer") VIEW_TYPE_OFFER_RECEIVER else VIEW_TYPE_RECEIVER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sender, parent, false)
                SenderViewHolder(view)
            }
            VIEW_TYPE_RECEIVER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_receiver, parent, false)
                ReceiverViewHolder(view)
            }
            VIEW_TYPE_OFFER_SENDER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_offer_sender, parent, false)
                OfferSenderViewHolder(view)
            }
            VIEW_TYPE_OFFER_RECEIVER -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_offer_receiver, parent, false)
                OfferReceiverViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    var onAcceptOffer: ((Message) -> Unit)? = null
    var onDeclineOffer: ((Message) -> Unit)? = null

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SenderViewHolder -> holder.textMessage.text = message.content
            is ReceiverViewHolder -> holder.textMessage.text = message.content
            is OfferSenderViewHolder -> {
                holder.textProductTitle.text = message.productTitle ?: "Unknown Product"
                holder.textOfferAmount.text = "₺ ${message.offerAmount}"
            }
            is OfferReceiverViewHolder -> {
                holder.textProductTitle.text = message.productTitle ?: "Unknown Product"
                holder.textOfferAmount.text = "₺ ${message.offerAmount}"
                
                if (message.status != "pending") {
                    holder.layoutButtons.visibility = View.GONE
                    holder.textStatus.visibility = View.VISIBLE
                    holder.textStatus.text = if (message.status == "accepted") "Offer Accepted" else "Offer Declined"
                } else {
                    holder.layoutButtons.visibility = View.VISIBLE
                    holder.textStatus.visibility = View.GONE
                    
                    holder.btnAccept.setOnClickListener { onAcceptOffer?.invoke(message) }
                    holder.btnDecline.setOnClickListener { onDeclineOffer?.invoke(message) }
                }
            }
        }
    }

    override fun getItemCount() = messages.size

    fun updateData(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    class SenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMessage: TextView = view.findViewById(R.id.textMessage)
    }

    class ReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMessage: TextView = view.findViewById(R.id.textMessage)
    }
    
    class OfferSenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textProductTitle: TextView = view.findViewById(R.id.textProductTitle)
        val textOfferAmount: TextView = view.findViewById(R.id.textOfferAmount)
    }

    class OfferReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textProductTitle: TextView = view.findViewById(R.id.textProductTitle)
        val textOfferAmount: TextView = view.findViewById(R.id.textOfferAmount)
        val btnAccept: android.widget.Button = view.findViewById(R.id.btnAccept)
        val btnDecline: android.widget.Button = view.findViewById(R.id.btnDecline)
        val layoutButtons: android.view.ViewGroup = view.findViewById(R.id.layoutButtons)
        val textStatus: TextView = view.findViewById(R.id.textStatus)
    }
}
