package org.meicode.ceylonexportsadmin.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.Navigation
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.meicode.ceylonexportsadmin.R
import org.meicode.ceylonexportsadmin.adapter.CategoryAdapter
import org.meicode.ceylonexportsadmin.databinding.FragmentCategoryBinding
import org.meicode.ceylonexportsadmin.model.CategoryModel
import java.util.*
import kotlin.collections.ArrayList

class CategoryFragment : Fragment() {//displaying a list of categories.

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var dialog: Dialog
    val list = ArrayList<CategoryModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoryBinding.inflate(layoutInflater)

        //A dialog is created and set up to show a progress indicator
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)


        dialog.show()
        getData()//retrieve the category data from Firestore.

        SearchCourse()//text watcher on the search input field

        binding.floatingActionButton.setOnClickListener {//The floating action button ---->navigate to the AddCategoryFragment
            Navigation.findNavController(it)
                .navigate(R.id.action_categoryFragment_to_AddCategoryFragment)
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
                    binding.categoryRecycler.adapter = CategoryAdapter(requireContext(), list)
                } else {////Otherwise, a clone ArrayList clone is created
                    val clone = ArrayList<CategoryModel>()
                    for (element in list!!) {
                        if (element.cat!!.lowercase()//checking if each category name (element.cat) ---->the entered text (case-insensitive match).
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
                        binding.textOops!!.visibility = View.VISIBLE//Oops" message is displayed.

                    }
                    //filtered clone ArrayList to display the filtered categories
                    binding.categoryRecycler.adapter = CategoryAdapter(requireContext(), clone)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun getData() {
        Firebase.firestore.collection("categories")
            .get().addOnSuccessListener {//get function is called to retrieve the documents from the collection.
                list.clear()//ArrayList is cleared to remove any existing data.
                dialog.dismiss()//indicate that the data retrieval is complete.
                for (doc in it.documents) {//loop iterates through each document in the retrieved data.
                    val data = doc.toObject(CategoryModel::class.java)// convert the document into an instance of the CategoryModel class.
                    list.add(data!!)//The converted CategoryModel object is added to the list
                }
                binding.categoryRecycler.adapter = CategoryAdapter(requireContext(), list)
            }
    }

}