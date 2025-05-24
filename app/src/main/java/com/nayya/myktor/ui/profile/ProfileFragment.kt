package com.nayya.myktor.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentProfileBinding
import com.nayya.myktor.ui.root.BaseFragment
import com.nayya.myktor.utils.viewBinding

class ProfileFragment : BaseFragment(R.layout.fragment_profile) {

    private val binding by viewBinding<FragmentProfileBinding>()

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory()
    }

    private lateinit var adapter: ProfileMenuAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProfileMenuAdapter { menu ->
            requireController<ProfileFragment.Controller>().onProfileMenuItemClicked(menu)
        }
        binding.rvProfileMenu.layoutManager = LinearLayoutManager(requireContext())

        binding.rvProfileMenu.adapter = adapter

        observeViewModel()
        viewModel.loadUserProfile()

        parentFragmentManager.setFragmentResultListener(
            "counterparty_updated",
            viewLifecycleOwner
        ) { _, _ ->
            viewModel.loadUserProfile() // ⬅️ Повторно загружаем актуальные данные
        }

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
            if (counterparty == null) {
                // Не авторизован или контрагент не создан
                val placeholderRes = R.drawable.ic_profile_placeholder_user_orig

                Glide.with(requireContext())
                    .load(placeholderRes)
                    .placeholder(placeholderRes)
                    .circleCrop()
                    .into(binding.ivAvatar)

                binding.tvLoginPrompt.visibility = View.VISIBLE
                binding.tvNickname.visibility = View.GONE
                binding.tvFirstName.visibility = View.GONE
                binding.tvCompanyName.visibility = View.GONE
                binding.ivRightIcon.visibility = View.GONE

                // Очистим текст — это важно, иначе могут остаться данные предыдущего пользователя
                binding.tvNickname.text = ""
                binding.tvFirstName.text = ""
                binding.tvCompanyName.text = ""

                binding.tvLoginPrompt.setOnClickListener {
                    requireController<ProfileFragment.Controller>().openLoginScreen()
                }

                return@observe
            }

            // Контрагент доступен (авторизован)
            val placeholderRes = if (counterparty.isLegalEntity)
                R.drawable.ic_profile_placeholder_firm_orig
            else
                R.drawable.ic_profile_placeholder_user_orig

            Glide.with(requireContext())
                .load(counterparty.imagePath.takeIf { !it.isNullOrBlank() } ?: placeholderRes)
                .placeholder(placeholderRes)
                .circleCrop()
                .into(binding.ivAvatar)

            binding.tvLoginPrompt.visibility = View.GONE
            binding.tvNickname.visibility = View.VISIBLE
            binding.ivRightIcon.visibility = View.VISIBLE

            val isLegal = counterparty.isLegalEntity
            binding.tvCompanyName.visibility = if (isLegal) View.VISIBLE else View.GONE
            binding.tvFirstName.visibility = if (isLegal) View.GONE else View.VISIBLE

            binding.tvNickname.text = counterparty.shortName
            binding.tvFirstName.text = counterparty.firstName
            binding.tvCompanyName.text = counterparty.companyName

            binding.ivRightIcon.setOnClickListener {
                counterparty.id?.let { counterpartyId ->
                    requireController<ProfileFragment.Controller>().openCounterpartyDetails(
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

    interface Controller : BaseFragment.Controller {
        fun openCounterpartyDetails(counterpartyId: Long)
        fun onProfileMenuItemClicked(item: ProfileMenuType)
        fun openLoginScreen()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}
