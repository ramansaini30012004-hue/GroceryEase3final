package com.example.groceryease3

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.groceryease3.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

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
                getUserPrefs().edit().putString("profile_image", it.toString()).apply()
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

        // ✅ Profile Image Click
        binding.imgProfile.setOnClickListener {
            pickImage.launch(arrayOf("image/*"))
        }

        // ✅ Name Edit (GREEN UI)
        binding.txtUserName.setOnClickListener {

            val input = EditText(requireContext())
            input.setText(binding.txtUserName.text)
            input.hint = "Enter your name"

            // ✅ STYLE
            input.setBackgroundResource(R.drawable.edittext_filled)
            input.setTextColor(resources.getColor(android.R.color.black))
            input.setPadding(40, 30, 40, 30)

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Edit Name")
                .setView(input)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()

            // ✅ BUTTON COLORS GREEN
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(android.R.color.holo_green_dark))

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(android.R.color.holo_green_dark))

            // ✅ SAVE CLICK
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = input.text.toString().trim()

                if (name.isEmpty()) {
                    input.error = "Enter name"
                    return@setOnClickListener
                }

                binding.txtUserName.text = name
                getUserPrefs().edit().putString("user_name", name).apply()

                dialog.dismiss()
            }
        }

        // ✅ CLICK LISTENERS
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

            // ✅ Firebase logout
            FirebaseAuth.getInstance().signOut()

            // ✅ Register screen open
            val intent = Intent(requireContext(), RegisterActivity::class.java)

            // 🔥 BACK STACK CLEAR (बहुत important)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }
    }

    // ✅ Titles fix (items issue fix)
    private fun setupTitles() {
        binding.rowSavedShops.txtTitle.text = "Saved Shops"
        binding.rowAddress.txtTitle.text = "Saved Address"
        binding.rowNotifications.txtTitle.text = "Notifications"
        binding.rowHelp.txtTitle.text = "Help"
        binding.rowAbout.txtTitle.text = "About"
    }

    // ✅ Load Data
    private fun loadData() {
        val prefs = getUserPrefs()

        binding.txtUserName.text = prefs.getString("user_name", "Your Name")

        val user = FirebaseAuth.getInstance().currentUser
        binding.txtUserEmail.text = user?.email ?: "No Email"

        val img = prefs.getString("profile_image", null)
        if (!img.isNullOrEmpty()) {
            binding.imgProfile.setImageURI(Uri.parse(img))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}