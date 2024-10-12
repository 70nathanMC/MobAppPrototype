package com.example.mobappprototype.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ItemSearchHistoryBinding

class SearchHistoryAdapter(
    private val searchHistory: List<String>,
    private val onSearchHistoryClick: (String) -> Unit
) : RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder>() {

    inner class SearchHistoryViewHolder(val binding: ItemSearchHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        val binding = ItemSearchHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        val query = searchHistory[position]
        holder.binding.btnSearchHistory.text = query

        holder.binding.btnSearchHistory.setOnClickListener {
            onSearchHistoryClick(query)
        }
    }

    override fun getItemCount(): Int = searchHistory.size
}