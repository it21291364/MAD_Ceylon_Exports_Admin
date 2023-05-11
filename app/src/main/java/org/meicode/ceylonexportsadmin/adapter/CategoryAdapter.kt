package org.meicode.ceylonexportsadmin.adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.meicode.ceylonexportsadmin.R
import org.meicode.ceylonexportsadmin.databinding.ItemCategoryLayoutBinding
import org.meicode.ceylonexportsadmin.model.CategoryModel


//used to display a list of CategoryModel objects in a RecyclerView
class CategoryAdapter(var context: Context, val list: ArrayList<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    //Context---->inflate the layout
//list--->categories to be displayed.
    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //reference to the layout View and its binding class
        var binding = ItemCategoryLayoutBinding.bind(view)
    }

    // create a new ViewHolder object--->RecyclerView needs to display a new item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_category_layout, parent, false)
        )
    }//inflates the item_category_layout.xml layout file into a View object and passes it to the ViewHolder object.

    override fun onBindViewHolder(
        holder: CategoryViewHolder,
        position: Int
    ) {//new item needs to be displayed in the RecyclerView
        //sets the category name and image for the CategoryModel

        holder.binding.textView2.text = list[position].cat
        Glide.with(context).load(list[position].img)
            .into(holder.binding.imageView2)////Glide--->load the image from the img property of the CategoryModel.

        holder.binding.editBtn.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("category", list[position])
            Navigation.findNavController(it)
                .navigate(R.id.action_categoryFragment_to_AddCategoryFragment, bundle)
        })
        holder.binding.deleteBtn.setOnClickListener(View.OnClickListener {
            val db = Firebase.firestore
            db.collection("categories").document(list[position].id.toString()).delete()
            try {
                list.removeAt(position)
                notifyDataSetChanged()
            } catch (e: Exception) {

            }
        })
    }

    override fun getItemCount(): Int {
        return list.size//returns the number of items in the list object
    }//RecyclerView to know how many items it needs to display.
}