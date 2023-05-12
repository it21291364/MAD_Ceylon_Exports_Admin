package org.meicode.ceylonexportsadmin.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.meicode.ceylonexportsadmin.R
import org.meicode.ceylonexportsadmin.adapter.CategoryAdapter
import org.meicode.ceylonexportsadmin.adapter.ProductsAdapter
import org.meicode.ceylonexportsadmin.databinding.FragmentProductBinding
import org.meicode.ceylonexportsadmin.model.AddProductModel
import org.meicode.ceylonexportsadmin.model.CategoryModel
import java.util.*
import kotlin.collections.ArrayList

class ProductFragment : Fragment() {

    private lateinit var binding: FragmentProductBinding
    private lateinit var dialog: Dialog
    val list = ArrayList<AddProductModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductBinding.inflate(layoutInflater)//The resulting binding object is assigned to the binding property of the fragment.


//The dialog is set as non-cancelable, meaning it cannot be dismissed by pressing
// the back button or clicking outside the dialog.
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)


        dialog.show()//the data retrieval is in progress.
        getData()//fetch the necessary data for the fragment.

        SearchCourse()

        binding.floatingActionButton.setOnClickListener {//When clicked, it navigates to the "addProductFragment"
            Navigation.findNavController(it)
                .navigate(R.id.action_productFragment_to_addProductFragment)
        }
        return binding.root
    }

    private fun SearchCourse() {
        binding.nameInput!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {//the entered text is checked for length.
                if (s.toString().trim { it <= ' ' }.length == 0) {//If it's empty or contains only whitespace characters
                    if (list!!.size != 0) {//the original category list is displayed
                        binding.categoryRecycler!!.visibility = View.VISIBLE
                        binding.textOops!!.visibility = View.GONE
                    } else {
                        binding.categoryRecycler!!.visibility = View.GONE
                        binding.textOops!!.visibility = View.VISIBLE
                    }
                    binding.categoryRecycler.adapter = ProductsAdapter(requireContext(), list)
                } else {////Otherwise, a clone ArrayList clone is created
                    val clone = ArrayList<AddProductModel>()
                    for (element in list!!) {
                        if (element.productName!!.lowercase()////checking if each product name (element.cat) ---->the entered text (case-insensitive match).
                                .contains(s.toString().lowercase(Locale.getDefault()))
                        ) {
                            clone.add(element)// If a match is found, the category is added to the clone ArrayList.
                        }
                    }
                    if (clone.size != 0) {// If the clone ArrayList is not empty,
                        binding.categoryRecycler!!.visibility = View.VISIBLE//RecyclerView is visible
                        binding.textOops!!.visibility = View.GONE//Oops" message is hidden.
                    } else {
                        binding.categoryRecycler!!.visibility = View.GONE//RecyclerView is hidden
                        binding.textOops!!.visibility = View.VISIBLE////Oops" message is displayed.
                    }
                    ////filtered clone ArrayList to display the filtered products
                    binding.categoryRecycler.adapter = ProductsAdapter(requireContext(), clone)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun getData() {
        Firebase.firestore.collection("products")
            .get().addOnSuccessListener {////get function is called to retrieve the documents from the collection.
                list.clear()//ArrayList is cleared to remove any existing data.
                dialog.dismiss()//indicate that the data retrieval is complete.
                for (doc in it.documents) {//loop iterates through each document in the retrieved data.
                    val data = doc.toObject(AddProductModel::class.java)// convert the document into an instance of the CategoryModel class.
                    list.add(data!!)//The converted CategoryModel object is added to the list
                }
                binding.categoryRecycler.adapter = ProductsAdapter(requireContext(), list)
            }
    }

}