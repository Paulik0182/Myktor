package com.nayya.myktor.ui.product.orders

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentOrdersBinding
import com.nayya.myktor.utils.viewBinding

class OrdersFragment : Fragment(R.layout.fragment_orders) {

    private val binding by viewBinding<FragmentOrdersBinding>()
    private val viewModel: OrderViewModel by viewModels()
    private lateinit var adapter: OrdersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrdersAdapter(emptyList())
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            adapter.updateList(orders)
        }

        viewModel.fetchOrders()
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
        fun newInstance() = OrdersFragment()
    }
}