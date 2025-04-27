package com.nayya.myktor.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentProfileBinding
import com.nayya.myktor.utils.viewBinding

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val binding by viewBinding<FragmentProfileBinding>()

    private val viewModel by lazy {
        ProfileViewModel(DefaultCounterpartyRepository())
    }

    private lateinit var adapter: ProfileMenuAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProfileMenuAdapter { menu ->
            getController().onProfileMenuItemClicked(menu)
        }
        binding.rvProfileMenu.layoutManager = LinearLayoutManager(requireContext())

        binding.rvProfileMenu.adapter = adapter

        observeViewModel()
        // Пока ID жёстко задан
        viewModel.loadCounterparty(counterpartyId = 19L)

        binding.ivTelegram.setOnClickListener {
            openUrl("https://t.me/your_channel") // TODO заменить на свой канал
        }

        binding.ivFacebook.setOnClickListener {
            openUrl("https://facebook.com/your_page") // TODO заменить на свой канал
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun observeViewModel() {
        viewModel.counterparty.observe(viewLifecycleOwner) { counterparty ->
            val placeholderRes = if (counterparty.isLegalEntity) {
                R.drawable.ic_profile_placeholder_firm_orig
            } else {
                R.drawable.ic_profile_placeholder_user_orig
            }

            Glide.with(requireContext())
                .load(counterparty.imagePath.takeIf { !it.isNullOrBlank() } ?: placeholderRes)
                .placeholder(placeholderRes)
                .circleCrop()
                .into(binding.ivAvatar)

            binding.tvCompanyName.apply {
                visibility = if (counterparty.isLegalEntity) View.VISIBLE else View.GONE
                text = counterparty.companyName
            }

            binding.tvFirstName.apply {
                visibility = if (counterparty.isLegalEntity) View.GONE else View.VISIBLE
                text = counterparty.firstName
            }

            binding.tvNickname.text = counterparty.shortName

            binding.llTextContainer.setOnClickListener {
                counterparty.id?.let { counterpartyId ->
                    getController().openCounterpartyDetails(
                        counterpartyId
                    )
                }
            }
        }

        viewModel.menuItems.observe(viewLifecycleOwner) { menuItems ->
            Log.d("DEBUG", "Menu size: ${menuItems.size}")
            adapter.updateItems(menuItems)
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openCounterpartyDetails(counterpartyId: Long)
        fun onProfileMenuItemClicked(item: ProfileMenuType)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
