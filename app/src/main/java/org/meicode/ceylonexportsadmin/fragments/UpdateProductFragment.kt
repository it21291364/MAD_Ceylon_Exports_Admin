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
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.meicode.ceylonexportsadmin.R
import org.meicode.ceylonexportsadmin.adapter.AddProductImageAdapter
import org.meicode.ceylonexportsadmin.adapter.ProductsImagesAdapter
import org.meicode.ceylonexportsadmin.databinding.FragmentAddProductBinding
import org.meicode.ceylonexportsadmin.databinding.FragmentUpdateProductBinding
import org.meicode.ceylonexportsadmin.model.AddProductModel
import org.meicode.ceylonexportsadmin.model.CategoryModel
import java.util.*
import kotlin.collections.ArrayList

class UpdateProductFragment : Fragment() {

    private lateinit var binding: FragmentUpdateProductBinding//bind views in the layout file to properties in the Kotlin code;
    private lateinit var list: ArrayList<Uri>//holds Uri objects that represent images selected by the user
    private lateinit var listImages: ArrayList<String>//holds String objects that represent the URLs of the images uploaded to the Firebase Storage
    private lateinit var adapter: AddProductImageAdapter//display the images selected by the user in a RecyclerView
    private var coverImage: Uri? = null//represents the cover image
    private lateinit var dialog: Dialog//display a progress bar while images are uploaded to Firebase Storage;
    private var coverImgUrl: String? = " "// URL of the cover image uploaded to Firebase Storage
    private lateinit var categoryList: ArrayList<String>//holds the names of the categories available in the database.


    var data = AddProductModel()
    private var isCoverUpdate: Boolean = false
    private var isProductImagesUpdate: Boolean = false

    private var launchGalleryActivity =
        registerForActivityResult(//launchGalleryActivity--->launch the gallery app to select the cover image
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {//f the result is successful
                coverImage = it.data!!.data
                binding.productCoverImg.setImageURI(coverImage)//coverImage URI is updated
                binding.productCoverImg.visibility = View.VISIBLE
                isCoverUpdate = true
            }
        }

    private var launchProductActivity =
        registerForActivityResult(//launchProductActivity---->launch the gallery app to select the product images.
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {// if the result is successful,
                //selected image URI is added to the list of product images, and the product images view, flags, and adapter are updated.
                val imageUrl = it.data!!.data
                list.add(imageUrl!!)
                isProductImagesUpdate = true
                binding.localProductIamgesRv.visibility = View.VISIBLE
                binding.productImagesRv.visibility = View.GONE
                adapter.notifyDataSetChanged()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUpdateProductBinding.inflate(layoutInflater)

        //initialized to store the local product images.
        list = ArrayList()
        listImages = ArrayList()

        //A dialog is created for progress indication.
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

        adapter = AddProductImageAdapter(list)//set as the adapter for the local product images recycler view.
        binding.localProductIamgesRv.adapter = adapter//set as the adapter for the local product images recycler view.


        if (arguments?.getSerializable("product") != null) {
            data = arguments?.getSerializable("product") as AddProductModel
            //retrieves the data from the arguments and populates the UI fields with the corresponding values.
            Log.d("testOk", data.toString())
            binding.productNameEdt.setText(data!!.productName)
            binding.productDescriptionEdt.setText(data!!.productDescription)
            binding.productMrpEdt.setText(data!!.productMrp)
            binding.productSpEdt.setText(data!!.productSp)
            Glide.with(this).load(data!!.productCoverImg).into(binding.productCoverImg)

            coverImgUrl = data.productCoverImg
            listImages = data.productImages


            Glide.with(this).load(coverImgUrl).into(binding.productCoverImg)

            //handle list of products
            Log.d("productLIsezi", listImages.size.toString())
            var adapter = ProductsImagesAdapter(requireActivity(), listImages)
            binding.productImagesRv.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        setProductCategory()//populate the Spinner with the available categories


        binding.submitProductBtn.setOnClickListener {
            validateData()
        }

        return binding.root
    }

    private fun validateData() {////checks if the required fields are filled and
        if (binding.productNameEdt.text.toString().isEmpty()) {
            binding.productNameEdt.requestFocus()
            binding.productNameEdt.error = "Empty"//displays an error message
        } else if (binding.productSpEdt.text.toString().isEmpty()) {
            binding.productSpEdt.requestFocus()
            binding.productSpEdt.error = "Empty"
        } else {//If both the product name and selling price fields are not empty,
            if (isCoverUpdate) {//if the cover image is updated
                uploadImage()//upload the new cover image
            } else if (isProductImagesUpdate) {//If the cover image is not updated, it checks if the product images are updated
                listImages.clear()//clears the existing list of product images
                uploadProductImage()//upload the new product images.
            } else {
                storeData()//store the data without uploading any images.
            }

        }
    }

    private fun uploadImage() {//uploads the cover image to Firebase Storage and retrieves its download URL
        dialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"//A unique filename is generated

        //The cover image will be stored under the "products" folder with the generated filename.
        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(coverImage!!)//passing coverImage!! as the file to be uploaded
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->//download URL of the uploaded cover image
                    coverImgUrl = image.toString()
                    if (isProductImagesUpdate) {// checks if the product images are updated
                        uploadProductImage()//upload the new product images.
                    } else {//not updated,
                        storeData()//store the data without uploading any product images.
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

    private var i = 0

    private fun uploadProductImage() {//uploads the product images to Firebase Storage and retrieves their download URLs.
        dialog.show()//indicate that the images are being uploaded.

        val fileName = UUID.randomUUID().toString() + ".jpg"//A unique file name is generated.

        //The Firebase Storage reference is created using the generated file name and the "products" directory
        val refStorage = FirebaseStorage.getInstance().reference.child("products/$fileName")
        refStorage.putFile(list[i])// upload the product image from the list at index i to Firebase Storage.
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    listImages.add(image!!.toString())// the download URL of the uploaded image is added to the listImages
                    if (list.size == listImages.size) {//number of product images===size of list
                        storeData()
                    } else {//If there are still remaining product images to upload
                        i += 1//move to the next image in the list,
                        uploadProductImage()//recursively called to upload the next product image.
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

        val db = Firebase.firestore.collection("products")// store the updated product data.

        val updateData = hashMapOf<String, Any>(
            //values for the fields are obtained from the respective UI elements
            "productName" to binding.productNameEdt.text.toString(),
            "productDescription" to binding.productDescriptionEdt.text.toString(),
            "productCoverImg" to coverImgUrl.toString(),
            "productCategory" to categoryList[binding.productCategoryDropdown.selectedItemPosition],
            "productId" to data.productId.toString(),
            "productMrp" to binding.productMrpEdt.text.toString(),
            "productSp" to binding.productSpEdt.text.toString(),
            "productImages" to listImages
        )

        db.document(data.productId!!).update(updateData)
            .addOnSuccessListener {//successful a message
                dialog.dismiss()
                Toast.makeText(requireContext(), "Product Updated", Toast.LENGTH_SHORT).show()
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

                val arrayAdapter = ArrayAdapter(//ArrayAdapter==data source+custom layout for the dropdown items.
                    requireContext(),
                    R.layout.dropdown_item_layout,
                    categoryList
                )//populate a dropdown list.
                binding.productCategoryDropdown.adapter = arrayAdapter
                //It sets the productCategoryDropdown view in the layout to use the arrayAdapter as its data source.

                //set selected category
                var pos = 0//keep track of the position of the selected category
                for (doc in categoryList) {//iterates through the category list
                    Log.d("foundDaa",doc)
                    Log.d("foundDaa",data.productCategory.toString())
                    if (data.productCategory==doc) {//the category of the updated product===category name
                        Log.d("foundDaa",pos.toString())
                        binding.productCategoryDropdown.setSelection(pos)// select the corresponding category in the dropdown.
                    }
                    pos += 1
                }
            }

    }
}