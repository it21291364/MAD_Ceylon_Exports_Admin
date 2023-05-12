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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {//called when the view associated with the fragment is created.

        super.onViewCreated(view, savedInstanceState)

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)
        dialog.setCancelable(false)//cannot be dismissed by pressing outside the dialog or the back button.


        binding.apply {
            imageView.setOnClickListener {//launches the gallery activity
                val intent = Intent("android.intent.action.GET_CONTENT")//get an image from the user
                intent.type = "image/*"//"image/*" to filter for image files.
                launchGalleryActivity.launch(intent)
            }
            button6.setOnClickListener {
                validateData(binding.categoryName.text.toString())
            }//passing the text entered in binding.categoryName.
        }


        if (arguments?.getSerializable("category") != null) {// if the fragment's arguments contain a serialized object with the key "category"
            category = arguments?.getSerializable("category") as CategoryModel
            Log.d("testOk", category.toString())
            binding.categoryName.setText(category!!.cat)
            //The retrieved category's name is set to binding.categoryName, and the image is loaded into binding.imageView
            Glide.with(this).load(category!!.img).into(binding.imageView)
            isEdit = true
        }

    }

    private var launchGalleryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {//indicating that the gallery activity was successful.
            imageUrl = it.data!!.data//retrieves the selected image's URI from it.data!!.data
            binding.imageView.setImageURI(imageUrl)
            isImageUpdate = true
        }
    }


    private fun validateData(categoryName: String) {//called when the button6 is clicked
        if (isEdit) {//indicating an edit operation:
            if (categoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Please provide category name", Toast.LENGTH_SHORT)
                    .show()
            } else {

                if (isImageUpdate) {//if the image has been updated
                    uploadImage(categoryName)
                } else {//image has not been updated
                    storeData(categoryName, category!!.img.toString())//passing the categoryName and the existing image URL from category!!.img
                }

            }
        } else {//If isEdit is false------>a new category creation:
            if (categoryName.isEmpty()) {
                Toast.makeText(requireContext(), "Please provide category name", Toast.LENGTH_SHORT)
                    .show()
            } else if (imageUrl == null) {
                Toast.makeText(requireContext(), "Please select image", Toast.LENGTH_SHORT).show()

            } else {
                //If the categoryName is not empty and an image has been selected, the uploadImage function is called, passing the categoryName
                uploadImage(categoryName)
            }
        }
    }

    private fun uploadImage(categoryName: String) {
        //uploading the selected image to Firebase Storage and then storing the category data,
        // including the image URL, in the database.
        dialog.show()//indicating that the image is being uploaded.

        val fileName = UUID.randomUUID().toString() + ".jpg"//ensures that each uploaded image has a unique filename.

        val refStorage = FirebaseStorage.getInstance().reference.child("category/$fileName")//The image will be stored under the "category" folder with the generated filename
        refStorage.putFile(imageUrl!!).addOnSuccessListener {//passing imageUrl!! as the file to be uploaded.
            it.storage.downloadUrl.addOnSuccessListener { image ->//If the image upload is successful
                storeData(categoryName, image.toString())//The image parameter contains the download URL of the uploaded imag
            }// passing the categoryName and the image URL obtained from the download URL.
        }.addOnFailureListener {//If the image upload fails
            dialog.dismiss()
            Toast.makeText(
                requireContext(), "Something went wrong with storage", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun storeData(categoryName: String, url: String) {
    //responsible for storing the category data (name and image URL) in the Firebase Firestore database
        val db = Firebase.firestore
        if (isEdit) {//If isEdit is true
            val data = hashMapOf<String, Any>(
                "cat" to categoryName, "img" to url, "id" to category!!.id.toString()
            )
            //update method is called on the Firestore document reference for the specific category ID.
            Log.d("testUpdate", data.toString())
            db.collection("categories").document(category!!.id.toString()).update(data)
                .addOnSuccessListener {//If the update is successful
                    dialog.dismiss()
                    binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.preview))//the image view is reset to a default image
                    binding.categoryName.text = null//category name text is cleared,
                    Toast.makeText(requireContext(), "Category Updated", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {//If the update fails
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {//If isEdit is false-------->a new category creation:

            val data = hashMapOf<String, Any>(
                "cat" to categoryName, "img" to url
            )//A data HashMap is created with the category name (categoryName) and image URL (url).

            db.collection("categories").add(data)//passed to add a new document with the specified values.
                .addOnSuccessListener {
                    //updating category id
                    val data = hashMapOf<String, Any>(
                        "cat" to categoryName, "img" to url, "id" to it.id// callback with the newly added document ID (it.id)
                    )
                    db.collection("categories").document(it.id).update(data).addOnSuccessListener {//update----------->Firestore document reference with the newly added document ID
                        dialog.dismiss()
                        binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.preview))//image view is reset to a default image,
                        binding.categoryName.text = null//category name text is cleared
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