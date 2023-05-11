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
        binding = FragmentProductBinding.inflate(layoutInflater)


        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)


        dialog.show()
        getData()

        SearchCourse()

        binding.floatingActionButton.setOnClickListener {
            Navigation.findNavController(it)
                .navigate(R.id.action_productFragment_to_addProductFragment)
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
                    binding.categoryRecycler.adapter = ProductsAdapter(requireContext(), list)
                } else {
                    val clone = ArrayList<AddProductModel>()
                    for (element in list!!) {
                        if (element.productName!!.lowercase()
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
                    binding.categoryRecycler.adapter = ProductsAdapter(requireContext(), clone)
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun getData() {
        Firebase.firestore.collection("products")
            .get().addOnSuccessListener {
                list.clear()
                dialog.dismiss()
                for (doc in it.documents) {
                    val data = doc.toObject(AddProductModel::class.java)
                    list.add(data!!)
                }
                binding.categoryRecycler.adapter = ProductsAdapter(requireContext(), list)
            }
    }

}