package org.meicode.ceylonexportsadmin.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.meicode.ceylonexportsadmin.R
import org.meicode.ceylonexportsadmin.databinding.FragmentSliderBinding
import java.util.UUID

class SliderFragment : Fragment() {

    private lateinit var binding: FragmentSliderBinding//(an instance of FragmentSliderBinding
    private var imageUrl : Uri? = null//store the selected image UR
    private lateinit var dialog: Dialog//a progress dialog).

    private var launchGalleryActivity = registerForActivityResult(//handle the result of the gallery activity.
        ActivityResultContracts.StartActivityForResult()
    ){
        if(it.resultCode == Activity.RESULT_OK){//if the result is successful,
            imageUrl = it.data!!.data//updated with the selected image URI,
            binding.imageView.setImageURI(imageUrl)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSliderBinding.inflate(layoutInflater)

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progress_layout)//The dialog is created and set to a custom layout progress_layout.
        dialog.setCancelable(false)

        binding.apply {
            imageView.setOnClickListener {
                val intent = Intent("android.intent.action.GET_CONTENT")//intent--> open the gallery app.
                intent.type = "image/*"
                launchGalleryActivity.launch(intent)
            }

            button5.setOnClickListener {//button5-->upload slider button
                if(imageUrl != null){//if not empty
                    uploadImage(imageUrl!!)//upload
                }else{
                    Toast.makeText(requireContext(), "Please select image", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding.root
    }

    private fun uploadImage(uri: Uri) {//called when the user selects an imag
        dialog.show()

        val fileName = UUID.randomUUID().toString()+".jpg"//generates a unique file name

        val refStorage = FirebaseStorage.getInstance().reference.child("slider/$fileName")//image file is uploaded to Firebase Storage
        refStorage.putFile(uri)
                // On successful upload, the download URL of the image is retrieved
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener{ image ->
                    storeData(image.toString())
                }
            }
            .addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(),"Something went wrong with storage",Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeData(image: String) {

        val db = Firebase.firestore

        val data = hashMapOf<String, Any>(
            "img" to image
        )

        //The data object is then set in the "slider" collection of the Firestore
        db.collection("slider").document("item").set(data)
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(requireContext(),"Slider Updated",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(),"Something went wrong",Toast.LENGTH_SHORT).show()
            }
    }
}