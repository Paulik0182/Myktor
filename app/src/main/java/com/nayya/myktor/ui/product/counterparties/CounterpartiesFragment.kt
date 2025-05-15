package com.nayya.myktor.ui.product.counterparties

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentSuppliersBinding
import com.nayya.myktor.domain.counterpartyentity.CounterpartyEntity
import com.nayya.myktor.utils.viewBinding
import kotlinx.coroutines.launch

// TODO На удаление!
class CounterpartiesFragment : Fragment(R.layout.fragment_suppliers) {

    private lateinit var viewModel: CounterpartyViewModel
    private val binding by viewBinding<FragmentSuppliersBinding>()
    private lateinit var adapter: CounterpartiesAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(CounterpartyViewModel::class.java)

        adapter = CounterpartiesAdapter(
            emptyList(),
            onItemClick = { counterparty ->
                getController().openEditSupplierFragment(counterparty)
            }
        ) { supplierDel ->
            supplierDel.id?.let { viewModel.deleteCounterparty(it) } // Удаляем поставщика
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.counterparties.observe(viewLifecycleOwner) { counterparties ->
            Log.d("SUPPLIERS", "✅ ЛОГ 2 Обновление списка: ${counterparties.size} элементов")

            adapter.updateList(counterparties)
        }

        // Запускаем suspend функцию в корутине (первая загрузка данных)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchCounterparties()
        }

        setFragmentResultListener("supplier_updated") { _, _ ->
            viewModel.fetchCounterparties() // Обновляем список только если данные изменились
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
        fun newInstance() = CounterpartiesFragment()
    }
}