package org.meicode.ceylonexportsadmin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.meicode.ceylonexportsadmin.databinding.AllOrderItemLayoutBinding
import org.meicode.ceylonexportsadmin.model.AllOrderModel

class AllOrderAdapater(val list: ArrayList<AllOrderModel>, val context: Context)
    : RecyclerView.Adapter<AllOrderAdapater.AllOrderViewHolder>(){

    inner class AllOrderViewHolder(val binding : AllOrderItemLayoutBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllOrderViewHolder {
        return AllOrderViewHolder(
            AllOrderItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AllOrderViewHolder, position: Int) {
        holder.binding.productTitle.text = list[position].name
        holder.binding.productPrice.text = list[position].price
        holder.binding.cancelButton.setOnClickListener {

            holder.binding.proceedButton.visibility = GONE

            updateStatus("Canceled", list[position].orderID!!)
        }
        when(list[position].status){
            "Ordered" -> {
                holder.binding.proceedButton.text = "Dispatched"

                holder.binding.proceedButton.setOnClickListener {
                    updateStatus("Dispatched", list[position].orderID!!)
                }

            }
            "Dispatched" -> {holder.binding.proceedButton.text = "Delivered"
                holder.binding.proceedButton.setOnClickListener {
                    updateStatus("Delivered", list[position].orderID!!)
                }
                }
            "Delivered" -> {
                holder.binding.cancelButton.visibility = GONE
                holder.binding.proceedButton.isEnabled = false
                holder.binding.proceedButton.text = "Already Delivered"

                           }
            "Canceled" -> {
                holder.binding.proceedButton.visibility = GONE
                holder.binding.cancelButton.isEnabled = false
            }
        }
    }

    fun updateStatus(str: String, doc: String){
        val data = hashMapOf<String, Any>()
        data["status"] = str
        Firebase.firestore.collection( "allOders").document(doc).update(data).addOnSuccessListener {
            Toast.makeText(context, "Status Updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
        }


    }
}