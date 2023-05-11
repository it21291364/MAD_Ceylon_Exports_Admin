package org.meicode.ceylonexportsadmin.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.meicode.ceylonexportsadmin.R
import org.meicode.ceylonexportsadmin.databinding.ItemProductBinding
import org.meicode.ceylonexportsadmin.model.AddProductModel


//used to display a list of CategoryModel objects in a RecyclerView
class ProductsAdapter(var context: Context, val list: ArrayList<AddProductModel>) :
    RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {
    //Context---->inflate the layout
//list--->categories to be displayed.
    inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //reference to the layout View and its binding class
        var binding = ItemProductBinding.bind(view)
    }

    // create a new ViewHolder object--->RecyclerView needs to display a new item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        )
    }//inflates the item_category_layout.xml layout file into a View object and passes it to the ViewHolder object.

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {//new item needs to be displayed in the RecyclerView
        //sets the category name and image for the CategoryModel

        holder.binding.productName.text = list[position].productName
        holder.binding.productMrp.text = list[position].productMrp
        holder.binding.productSp.text = list[position].productSp
        holder.binding.productDescription.text = list[position].productDescription

        Glide.with(context).load(list[position].productCoverImg).into(holder.binding.imageView2)

        //handle list of products
        var adapter = ProductsImagesAdapter(context,list[position].productImages)
        holder.binding.productImgRecyclerView.adapter = adapter


        holder.binding.editBtn.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("product", list[position])
            Navigation.findNavController(it)
                .navigate(R.id.action_productFragment_to_updateProductFragment, bundle)
        })
        holder.binding.deleteBtn.setOnClickListener(View.OnClickListener {
            val db = Firebase.firestore
            db.collection("products").document(list[position].productId.toString()).delete()
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