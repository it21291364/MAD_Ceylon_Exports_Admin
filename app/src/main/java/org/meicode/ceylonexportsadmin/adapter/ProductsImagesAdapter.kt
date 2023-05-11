package org.meicode.ceylonexportsadmin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import org.meicode.ceylonexportsadmin.databinding.ImageItemBinding

class ProductsImagesAdapter(var context: Context, val list: ArrayList<String>)//used to display a list of images in a RecyclerView
    //ArrayList-->represents the list of images that will be displayed in the RecyclerView.
    : RecyclerView.Adapter<ProductsImagesAdapter.ProductImagesViewHolder>(){

    inner class ProductImagesViewHolder(val binding: ImageItemBinding)
        : RecyclerView.ViewHolder(binding.root)//ViewHolder-->holds a reference to the layout View and its binding class.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductImagesViewHolder {//create a new ViewHolder-->RecyclerView needs to display a new item
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ProductImagesViewHolder(binding)
        //This method inflates the ImageItem.xml layout file into a View object and passes it to the ViewHolder object.
    }

    override fun onBindViewHolder(holder: ProductImagesViewHolder, position: Int) {//onBindViewHolder---->new item needs to be displayed in the RecyclerView.
        //sets the image at the specified position of the list
        Glide.with(context).load(list[position])
            .into(holder.binding.itemImg)
    }

    override fun getItemCount(): Int {//returns the number of items in the list object.
        return list.size
    }

}