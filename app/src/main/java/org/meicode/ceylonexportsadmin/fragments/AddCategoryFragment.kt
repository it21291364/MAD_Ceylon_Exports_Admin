package org.meicode.ceylonexportsadmin.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.meicode.ceylonexportsadmin.R
import org.meicode.ceylonexportsadmin.databinding.FragmentAddCategoryBinding
import org.meicode.ceylonexportsadmin.model.CategoryModel
import java.util.*

class AddCategoryFragment : Fragment() {


    private lateinit var binding: FragmentAddCategoryBinding

    private var imageUrl: Uri? = null
    private var isEdit: Boolean = false
    private var isImageUpdate: Boolean = false
    private lateinit var dialog: Dialog
    var category: CategoryModel? = CategoryModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddCategoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)


        binding.apply {
            imageView.setOnClickListener {
                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type = "image/*"
                launchGalleryActivity.launch(intent)
            }
            button6.setOnClickListener {
                validateData(binding.categoryName.text.toString())
            }
        }


        if (arguments?.getSerializable("category") != null) {
            category = arguments?.getSerializable("category") as CategoryModel
            Log.d("testOk", category.toString())
            binding.categoryName.setText(category!!.cat)
            Glide.with(this).load(category!!.img).into(binding.imageView)
            isEdit = true
        }

    }

    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            imageUrl = it.data!!.data
            binding.imageView.setImageURI(imageUrl)
            isImageUpdate = true
        }
    }


    private fun validateData(categoryName: String) {
        if (isEdit) {
            if (categoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Please provide category name", Toast.LENGTH_SHORT)
                    .show()
            } else {

                if (isImageUpdate) {
                    uploadImage(categoryName)
                } else {
                    storeData(categoryName, category!!.img.toString())
                }

            }
        } else {
            if (categoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Please provide category name", Toast.LENGTH_SHORT)
                    .show()
            } else if (imageUrl == null) {
                Toast.makeText(requireContext(), "Please select image", Toast.LENGTH_SHORT).show()

            } else {
                uploadImage(categoryName)
            }
        }
    }

    private fun uploadImage(categoryName: String) {
        dialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("category/$fileName")
        refStorage.putFile(imageUrl!!).addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { image ->
                storeData(categoryName, image.toString())
            }
        }.addOnFailureListener {
            dialog.dismiss()
            Toast.makeText(
                requireContext(), "Something went wrong with storage", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun storeData(categoryName: String, url: String) {

        val db = Firebase.firestore
        if (isEdit) {
            val data = hashMapOf<String, Any>(
                "cat" to categoryName, "img" to url, "id" to category!!.id.toString()
            )
            Log.d("testUpdate", data.toString())
            db.collection("categories").document(category!!.id.toString()).update(data)
                .addOnSuccessListener {
                    dialog.dismiss()
                    binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.preview))
                    binding.categoryName.text = null
                    Toast.makeText(requireContext(), "Category Updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {

            val data = hashMapOf<String, Any>(
                "cat" to categoryName, "img" to url
            )

            db.collection("categories").add(data)
                .addOnSuccessListener {
                    //updating category id
                    val data = hashMapOf<String, Any>(
                        "cat" to categoryName, "img" to url, "id" to it.id
                    )
                    db.collection("categories").document(it.id).update(data).addOnSuccessListener {
                        dialog.dismiss()
                        binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.preview))
                        binding.categoryName.text = null
                        Toast.makeText(requireContext(), "Category Added", Toast.LENGTH_SHORT)
                            .show()
                    }
                }.addOnFailureListener {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }
        }

    }
}