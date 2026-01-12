package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supply7.R
import com.example.supply7.data.Card

class CardAdapter(
    private var cards: List<Card> = emptyList(),
    private val onItemClick: ((Card) -> Unit)? = null // Optional click listener
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    private var selectedPosition = 0 // Default to first card selected

    class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view
        val textNumber: TextView = view.findViewById(R.id.textCardNumber)
        val textExpiry: TextView = view.findViewById(R.id.textExpiry)
        val imgSelected: android.widget.ImageView = view.findViewById(R.id.imgSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card_row, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        holder.textNumber.text = card.cardNumber
        holder.textExpiry.text = card.expiryDate

        // Selection Logic
        val isSelected = (position == selectedPosition)
        holder.imgSelected.visibility = if (isSelected) View.VISIBLE else View.GONE
        
        // Optional: Change background border/stroke if selected?
        // holder.root.setBackgroundResource(if (isSelected) R.drawable.bg_card_selected else R.drawable.bg_input_field)

        holder.root.setOnClickListener {
            val previous = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previous)
            notifyItemChanged(selectedPosition)
            onItemClick?.invoke(card)
        }
    }

    override fun getItemCount(): Int = cards.size

    fun updateData(newCards: List<Card>) {
        cards = newCards
        notifyDataSetChanged()
    }
    
    fun getSelectedCard(): Card? {
        if (cards.isNotEmpty() && selectedPosition in cards.indices) {
            return cards[selectedPosition]
        }
        return null
    }
}
