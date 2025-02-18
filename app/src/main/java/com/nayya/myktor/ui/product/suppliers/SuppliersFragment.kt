package com.nayya.myktor.ui.product.suppliers

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentSuppliersBinding
import com.nayya.myktor.ui.product.editsupplier.EditSupplierFragment
import com.nayya.myktor.utils.viewBinding
import kotlinx.coroutines.launch

class SuppliersFragment : Fragment(R.layout.fragment_suppliers) {

    private lateinit var viewModel: SupplierViewModel
    private val binding by viewBinding<FragmentSuppliersBinding>()
    private lateinit var adapter: SuppliersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SupplierViewModel::class.java)

        adapter = SuppliersAdapter(
            emptyList()) {supplier ->
            viewModel.deleteSupplier(supplier.id) // Удаляем поставщика
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.suppliers.observe(viewLifecycleOwner) { suppliers ->
            adapter.updateList(suppliers)
        }

//        viewModel.fetchSuppliers()
        // Запускаем suspend функцию в корутине
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchSuppliers()
        }

        // Кнопка "Добавить поставщика"
        binding.addCounterpartyButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, EditSupplierFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        // TODO
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {

        @JvmStatic
        fun newInstance() = SuppliersFragment()
    }
}