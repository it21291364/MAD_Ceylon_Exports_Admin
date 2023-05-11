package org.meicode.ceylonexportsadmin.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.meicode.ceylonexportsadmin.R
import org.meicode.ceylonexportsadmin.adapter.AddProductImageAdapter
import org.meicode.ceylonexportsadmin.databinding.FragmentAddProductBinding
import org.meicode.ceylonexportsadmin.model.AddProductModel
import org.meicode.ceylonexportsadmin.model.CategoryModel
import java.util.*
import kotlin.collections.ArrayList

//create a fragment that allows the user to add a new product to a database.
class AddProductFragment : Fragment() {
    private lateinit var binding: FragmentAddProductBinding//bind views in the layout file to properties in the Kotlin code;
    private lateinit var list: ArrayList<Uri>//holds Uri objects that represent images selected by the user
    private lateinit var listImages: ArrayList<String>//holds String objects that represent the URLs of the images uploaded to the Firebase Storage
    private lateinit var adapter: AddProductImageAdapter//display the images selected by the user in a RecyclerView
    private var coverImage: Uri? = null//represents the cover image
    private lateinit var dialog: Dialog//display a progress bar while images are uploaded to Firebase Storage;
    private var coverImgUrl: String? = " "// URL of the cover image uploaded to Firebase Storage
    private lateinit var categoryList: ArrayList<String>//holds the names of the categories available in the database.


    private var launchGalleryActivity =
        registerForActivityResult(//launchGalleryActivity--->launch the gallery app to select the cover image
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                coverImage = it.data!!.data
                binding.productCoverImg.setImageURI(coverImage)
                binding.productCoverImg.visibility = VISIBLE
            }
        }

    private var launchProductActivity =
        registerForActivityResult(//launchProductActivity---->launch the gallery app to select the product images.
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val imageUrl = it.data!!.data
                list.add(imageUrl!!)
                adapter.notifyDataSetChanged()
            }
        }


    override fun onCreateView(//layout file for the fragment and sets up event listeners for the UI components
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddProductBinding.inflate(layoutInflater)

        list = ArrayList()
        listImages = ArrayList()

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)

        binding.selectCoverImg.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGalleryActivity.launch(intent)
        }

        binding.productImgBtn.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchProductActivity.launch(intent)
        }


        setProductCategory()//populate the Spinner with the available categories

        adapter = AddProductImageAdapter(list)
        binding.productImgRecyclerView.adapter = adapter

        binding.submitProductBtn.setOnClickListener {
            validateData()
        }

        return binding.root
    }

    private fun validateData() {////checks if the required fields are filled and
        if (binding.productNameEdt.text.toString().isEmpty()) {
            binding.productNameEdt.requestFocus()
            binding.productNameEdt.error = "Empty"
        } else if (binding.productSpEdt.text.toString().isEmpty()) {
            binding.productSpEdt.requestFocus()
            binding.productSpEdt.error = "Empty"
        } else if (coverImage == null) {//// if the user has selected a cover image and
            Toast.makeText(requireContext(), "Please select a cover image", Toast.LENGTH_SHORT)
                .show()
        } else if (list.size < 1) {//// at least one product image
            Toast.makeText(requireContext(), "Please select product images", Toast.LENGTH_SHORT)
                .show()
        } else {
            uploadImage()//uploads the cover image to Firebase Storage and retrieves its download URL
        }
    }

    private fun uploadImage() {//uploads the cover image to Firebase Storage and retrieves its download URL
        dialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(coverImage!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    coverImgUrl = image.toString()
                    uploadProductImage()//upload the product images.
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Something went wrong with storage",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private var i = 0

    private fun uploadProductImage() {//uploads the product images to Firebase Storage and retrieves their download URLs.
        dialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(list[i])
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    listImages.add(image!!.toString())
                    if (list.size == listImages.size) {
                        storeData()
                    } else {
                        i += 1
                        uploadProductImage()
                    }
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Something went wrong with storage",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun storeData() {//store the product data in the Firebase Firestore database.

        val db = Firebase.firestore.collection("products")
        val key = db.document().id

        val data = AddProductModel()
        data.productName = binding.productNameEdt.text.toString()
        data.productDescription = binding.productDescriptionEdt.text.toString()
        data.productCoverImg = coverImgUrl.toString()
        data.productCategory = categoryList[binding.productCategoryDropdown.selectedItemPosition]
        data.productId = key
        data.productMrp = binding.productMrpEdt.text.toString()
        data.productSp = binding.productSpEdt.text.toString()
        data.productImages = listImages

        db.document(key).set(data).addOnSuccessListener {//successful a message
            dialog.dismiss()
            Toast.makeText(requireContext(), "Product Added", Toast.LENGTH_SHORT).show()
            binding.productNameEdt.text = null

        }

            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Somehing went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setProductCategory() {//sets the product category----populates a dropdown list

        categoryList = ArrayList()//creates a new empty ArrayList
        Firebase.firestore.collection("categories").get()
            .addOnSuccessListener {//retrieves data from the "categories" collection
                categoryList.clear()//remove any existing data
                for (doc in it.documents) {//iterates through the documents
                    val data = doc.toObject(CategoryModel::class.java)
                    categoryList.add(data!!.cat!!)//adding the category name to ArrayList.
                }
                categoryList.add(0, "Select Category")

                val arrayAdapter = ArrayAdapter(
                    requireContext(),
                    R.layout.dropdown_item_layout,
                    categoryList
                )//populate a dropdown list.
                binding.productCategoryDropdown.adapter = arrayAdapter
                //It sets the productCategoryDropdown view in the layout to use the arrayAdapter as its data source.
            }

    }
}