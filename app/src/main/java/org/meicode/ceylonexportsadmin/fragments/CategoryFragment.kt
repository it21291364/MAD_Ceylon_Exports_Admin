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

class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var dialog: Dialog
    val list = ArrayList<CategoryModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCategoryBinding.inflate(layoutInflater)

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)


        dialog.show()
        getData()

        SearchCourse()

        binding.floatingActionButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_categoryFragment_to_AddCategoryFragment)
        }

        return binding.root
    }

    private fun SearchCourse() {
        binding.nameInput!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim { it <= ' ' }.length == 0) {
                    if (list!!.size != 0) {
                        binding.categoryRecycler!!.visibility = View.VISIBLE
                        binding.textOops!!.visibility = View.GONE
                    } else {
                        binding.categoryRecycler!!.visibility = View.GONE
                        binding.textOops!!.visibility = View.VISIBLE
                    }
                    binding.categoryRecycler.adapter = CategoryAdapter(requireContext(), list)
                } else {
                    val clone = ArrayList<CategoryModel>()
                    for (element in list!!) {
                        if (element.cat!!.lowercase()
                                .contains(s.toString().lowercase(Locale.getDefault()))
                        ) {
                            clone.add(element)
                        }
                    }
                    if (clone.size != 0) {
                        binding.categoryRecycler!!.visibility = View.VISIBLE
                        binding.textOops!!.visibility = View.GONE
                    } else {
                        binding.categoryRecycler!!.visibility = View.GONE
                        binding.textOops!!.visibility = View.VISIBLE
                    }
                    binding.categoryRecycler.adapter = CategoryAdapter(requireContext(), clone)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun getData() {
        Firebase.firestore.collection("categories")
            .get().addOnSuccessListener {
                list.clear()
                dialog.dismiss()
                for (doc in it.documents) {
                    val data = doc.toObject(CategoryModel::class.java)
                    list.add(data!!)
                }
                binding.categoryRecycler.adapter = CategoryAdapter(requireContext(), list)
            }
    }

}