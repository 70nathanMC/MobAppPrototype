package com.example.mobappprototype.Adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mobappprototype.databinding.ItemSubjectsBinding
import com.example.mobappprototype.model.ButtonData

class ButtonAdapter(
    private val buttonList: MutableList<ButtonData>,
    private val onAddClicked: () -> Unit,
    private val onRemoveClicked: (Int) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<ButtonAdapter.ButtonViewHolder>() {

    inner class ButtonViewHolder(val binding: ItemSubjectsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val binding = ItemSubjectsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ButtonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val buttonData = buttonList[position]
        holder.binding.btnSubjectButtons.text = buttonData.label

        // Handle the click events
        if (buttonData.isAddButton) {
            holder.binding.btnSubjectButtons.setOnClickListener {
                onAddClicked()
            }
        } else {
            holder.binding.btnSubjectButtons.setOnClickListener {
                showRemoveConfirmationDialog(context, buttonData.label, position)
            }
        }
    }

    override fun getItemCount(): Int = buttonList.size

    private fun showRemoveConfirmationDialog(context: Context, subjectLabel: String, position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Remove $subjectLabel?")
        builder.setMessage("Do you want to remove $subjectLabel from the list?")
        builder.setPositiveButton("Yes") { _, _ ->
            onRemoveClicked(position)
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }
}