package com.nayya.myktor.ui.product.suppliers

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentSuppliersBinding
import com.nayya.myktor.domain.CounterpartyEntity
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
            emptyList(),
            onItemClick = { supplier ->
                getController().openEditSupplierFragment(supplier)
            }
        ) { supplierDel ->
            supplierDel.id?.let { viewModel.deleteSupplier(it) } // Удаляем поставщика
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.suppliers.observe(viewLifecycleOwner) { suppliers ->
            adapter.updateList(suppliers)
        }

        // Запускаем suspend функцию в корутине (первая загрузка данных)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchSuppliers()
        }

        setFragmentResultListener("supplier_updated") { _, _ ->
            viewModel.fetchSuppliers() // Обновляем список только если данные изменились
        }

        // Кнопка "Добавить поставщика"
        binding.addCounterpartyButton.setOnClickListener {
            getController().openEditSupplierFragment(null)
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openEditSupplierFragment(supplier: CounterpartyEntity?)
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