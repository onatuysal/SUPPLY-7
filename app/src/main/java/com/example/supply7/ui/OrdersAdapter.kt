package com.example.supply7.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.supply7.R
import com.example.supply7.data.Order
import java.text.SimpleDateFormat // Consider java.time later if API 26+
import java.util.Date
import java.util.Locale

class OrdersAdapter(private val onItemClick: (Order) -> Unit) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    private var orders: List<Order> = emptyList()

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textOrderId: TextView = view.findViewById(R.id.textOrderId)
        val textDate: TextView = view.findViewById(R.id.textDate)
        val textTotal: TextView = view.findViewById(R.id.textTotal)
        val textStatus: TextView = view.findViewById(R.id.textStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.textOrderId.text = "Order #${order.id.takeLast(6)}"
        holder.textTotal.text = "₺${order.totalAmount}"
        holder.textStatus.text = order.status
        
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        holder.textDate.text = sdf.format(Date(order.timestamp))
        
        // Status Color
        if (order.status == "CONFIRMED" || order.status == "DELIVERED") {
            holder.textStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
             holder.textStatus.setTextColor(android.graphics.Color.parseColor("#FF9800"))
        }

        holder.itemView.setOnClickListener { onItemClick(order) }
    }

    override fun getItemCount() = orders.size

    fun updateData(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
