package com.example.groceryease3

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.groceryease3.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    // ✅ SharedPrefs per user
    private fun getUserPrefs(): SharedPreferences {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "default"
        return requireActivity().getSharedPreferences("UserPrefs_$uid", Context.MODE_PRIVATE)
    }

    // ✅ Image Picker
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                requireActivity().contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                binding.imgProfile.setImageURI(it)

                getUserPrefs().edit()
                    .putString("profile_image", it.toString())
                    .apply()

                Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()
        setupTitles()
        binding.rowAddress.root.visibility = View.GONE
        binding.rowNotifications.root.visibility = View.GONE

        // ✅ Profile Image Click
        binding.imgProfile.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        // ✅ Name Edit
        binding.txtUserName.setOnClickListener {

            val input = EditText(requireContext())
            input.setText(binding.txtUserName.text)
            input.hint = "Enter your name"

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Edit Name")
                .setView(input)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {

                val name = input.text.toString().trim()

                if (name.isEmpty()) {
                    input.error = "Enter name"
                    return@setOnClickListener
                }

                val user = FirebaseAuth.getInstance().currentUser
                val email = user?.email ?: ""

                // ✅ SAVE DATA
                val prefs = getUserPrefs().edit()
                prefs.putString("name", name)
                prefs.putString("email", email)
                prefs.apply()


                binding.txtUserName.text = name

                // 🔥 IMPORTANT: Notify HomeFragment
                requireActivity().supportFragmentManager.setFragmentResult(
                    "profile_updated",
                    Bundle()
                )

                Toast.makeText(requireContext(), "Name updated", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            }
        }

        // ✅ Click Listeners
        binding.rowAddress.root.setOnClickListener {
            startActivity(Intent(requireContext(), AddressActivity::class.java))
        }

        binding.rowSavedShops.root.setOnClickListener {
            startActivity(Intent(requireContext(), SavedShopsActivity::class.java))
        }

        binding.rowHelp.root.setOnClickListener {
            startActivity(Intent(requireContext(), HelpCenterActivity::class.java))
        }

        binding.rowAbout.root.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        // ✅ Logout
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(requireContext(), RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // ✅ Titles
    private fun setupTitles() {
        binding.rowSavedShops.txtTitle.text = "Saved Shops"
        binding.rowAddress.txtTitle.text = "Saved Address"
        binding.rowNotifications.txtTitle.text = "Notifications"
        binding.rowHelp.txtTitle.text = "Help"
        binding.rowAbout.txtTitle.text = "About"
    }

    // ✅ Load Data
    private fun loadData() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: return // Exit if no user is logged in

        // 1. Set the email immediately from Auth
        binding.txtUserEmail.text = user.email ?: "No Email"

        // 2. Get reference to the specific user's data
        val databaseRef = FirebaseDatabase.getInstance().getReference("Users_data").child(uid)

        // 3. Listen for data once
        databaseRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").value.toString()
                val base64Image = snapshot.child("imageUrl").value.toString()

                // Update UI
                binding.txtUserName.text = name

                if (base64Image.isNotEmpty()) {
                    val bitmap = decodeBase64(base64Image)
                    binding.imgProfile.setImageBitmap(bitmap)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decodeBase64(base64String: String): Bitmap? {
        return try {
            val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}