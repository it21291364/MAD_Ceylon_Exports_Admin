package org.meicode.ceylonexportsadmin.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.meicode.ceylonexportsadmin.databinding.ImageItemBinding

class AddProductImageAdapter(val list: ArrayList<Uri>)//used to display a list of images in a RecyclerView
    //ArrayList-->represents the list of images that will be displayed in the RecyclerView.
    : RecyclerView.Adapter<AddProductImageAdapter.AddProductImageViewHolder>(){

    inner class AddProductImageViewHolder(val binding: ImageItemBinding)
        : RecyclerView.ViewHolder(binding.root)//ViewHolder-->holds a reference to the layout View and its binding class.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddProductImageViewHolder {//create a new ViewHolder-->RecyclerView needs to display a new item
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AddProductImageViewHolder(binding)
        //This method inflates the ImageItem.xml layout file into a View object and passes it to the ViewHolder object.
    }

    override fun onBindViewHolder(holder: AddProductImageViewHolder, position: Int) {//onBindViewHolder---->new item needs to be displayed in the RecyclerView.
        holder.binding.itemImg.setImageURI(list[position])//sets the image at the specified position of the list
    }

    override fun getItemCount(): Int {//returns the number of items in the list object.
        return list.size
    }

}