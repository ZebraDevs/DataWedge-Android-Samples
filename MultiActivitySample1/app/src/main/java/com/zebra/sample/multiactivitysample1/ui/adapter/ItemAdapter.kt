package com.zebra.sample.multiactivitysample1.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zebra.sample.multiactivitysample1.data.models.DWOutputData
import com.zebra.sample.multiactivitysample1.databinding.LayoutResultBinding

class ItemAdapter :
    RecyclerView.Adapter<ItemAdapter.OutputViewHolder>() {
    var latestBinding: LayoutResultBinding?=null

    private var items = arrayListOf<DWOutputData>()

    override fun onBindViewHolder(holder: OutputViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.binding.isLatest = true
        latestBinding?.isLatest = false
        latestBinding = holder.binding
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutputViewHolder {
        val binding = LayoutResultBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OutputViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(item: DWOutputData) {
        items.add(0,item)
        notifyItemInserted(0)
    }

    class OutputViewHolder(val binding: LayoutResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dwOutputData: DWOutputData) {
            binding.code = dwOutputData.data
            binding.label = dwOutputData.label
        }
    }
}
