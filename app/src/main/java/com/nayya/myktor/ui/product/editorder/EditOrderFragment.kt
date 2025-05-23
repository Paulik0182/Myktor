package com.nayya.myktor.ui.product.editorder

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentEditOrderBinding
import com.nayya.myktor.domain.counterpartyentity.OrderEntity
import com.nayya.myktor.domain.counterpartyentity.OrderItem
import com.nayya.myktor.domain.productentity.MeasurementUnitList
import com.nayya.myktor.ui.product.orders.OrderViewModel
import com.nayya.myktor.utils.viewBinding

class EditOrderFragment : Fragment(R.layout.fragment_edit_order) {

    private val binding by viewBinding<FragmentEditOrderBinding>()
    private val orderViewModel: OrderViewModel by activityViewModels()
    private val viewModel: EditOrderViewModel by lazy {
        EditOrderViewModel(orderViewModel)
    }

    private var orderId: Long? = null
    private var order: OrderEntity? = null
    private lateinit var adapter: OrderItemsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderItemsAdapter(emptyList())
        binding.orderItemsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.orderItemsRecyclerView.adapter = adapter

        if (arguments?.containsKey(ARG_ORDER_ID) == true) {
            orderId = arguments?.getLong(ARG_ORDER_ID)
        }

        orderId?.let {
            viewModel.fetchOrderDetails(
                it,
                onSuccess = { fetchedOrder ->
                    Log.d("API", "Полученный заказ: $fetchedOrder")
                    order = fetchedOrder
                    binding.counterpartyEditText.setText(fetchedOrder.counterpartyName)
                    adapter.updateList(fetchedOrder.items)
                },
                onError = {
                    Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.addItemButton.setOnClickListener {
            val newItem = OrderItem(
                orderId = orderId ?: 0,
                productId = 2, // Заглушка
                productName = "Заглушка: Товар 2",
                quantity = 1,
                measurementUnitId = 1, // Заглушка ID единицы измерения
                measurementUnitList = MeasurementUnitList(
                    id = 1,
                    name = "Штуки",
                    abbreviation = "шт"
                ),
                measurementUnit = "Штуки",
                measurementUnitAbbreviation = "шт"
            )
            adapter.addItem(newItem)
        }

        binding.saveButton.setOnClickListener {
            val updatedOrder = order?.copy(
                counterpartyId = order!!.counterpartyId, // TODO Это мне не нравится
                items = adapter.getItems()
            )
            updatedOrder?.let {
                viewModel.saveOrder(it,
                    onSuccess = {
                        setFragmentResult("order_updated", Bundle())
                        Toast.makeText(requireContext(), "Заказ обновлен", Toast.LENGTH_SHORT)
                            .show()
                        parentFragmentManager.popBackStack()
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Ошибка обновления", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            }
        }
    }

    companion object {
        private const val ARG_ORDER_ID = "order_id"

        fun newInstance(orderId: Long? = null): EditOrderFragment {
            val fragment = EditOrderFragment()
            val args = Bundle()
            orderId?.let { args.putLong(ARG_ORDER_ID, it) }
            fragment.arguments = args
            return fragment
        }
    }
}