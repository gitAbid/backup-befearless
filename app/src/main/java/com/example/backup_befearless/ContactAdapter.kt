package com.example.backup_befearless

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_number.view.*

class ContactAdapter(var numbers: List<ContactModel>, var itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivDelete = view.ivDelete
        val tvNumber = view.tvNumbers
        val tvName = view.tvName
    }

    override fun getItemCount(): Int {
        return numbers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ivDelete.setOnClickListener { itemClickListener.onDelete(position) }
        holder.tvNumber.text = numbers[position].number
        holder.tvName.text = numbers[position].contactName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_number,
                parent,
                false
            )
        )
    }

}