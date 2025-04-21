package com.nayya.myktor.ui.product.orders

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentOrdersBinding
import com.nayya.myktor.domain.counterpartyentity.OrderEntity
import com.nayya.myktor.utils.viewBinding
import kotlinx.coroutines.launch

class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private val binding by viewBinding<FragmentOrdersBinding>()
    private val viewModel: OrderViewModel by viewModels()
    private lateinit var adapter: OrdersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrdersAdapter(
            emptyList(),
            onItemClick = { order ->
                getController().openEditOrderFragment(order)
            }
        ) { orderDel ->
            orderDel.id?.let { viewModel.orderDetails(it) }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            adapter.updateList(orders)
        }
        // Запускаем функцию в корутине (первая загрузка данных)
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.fetchOrders()
        }

        setFragmentResultListener("order_updated") { _, _ ->
            viewModel.fetchOrders()
        }

        binding.addOrderButton.setOnClickListener {
            getController().openEditOrderFragment(null)
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openEditOrderFragment(orderEntity: OrderEntity?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {

        @JvmStatic
        fun newInstance() = OrdersFragment()
    }
}