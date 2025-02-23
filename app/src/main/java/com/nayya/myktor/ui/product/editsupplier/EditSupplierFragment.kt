package com.nayya.myktor.ui.product.editsupplier

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentEditSupplierBinding
import com.nayya.myktor.domain.CounterpartyEntity
import com.nayya.myktor.ui.product.suppliers.SupplierViewModel
import com.nayya.myktor.utils.viewBinding

class EditSupplierFragment : Fragment(R.layout.fragment_edit_supplier) {

    private val binding by viewBinding<FragmentEditSupplierBinding>()

    private val supplierViewModel: SupplierViewModel by activityViewModels()

    private val viewModel: EditSupplierViewModel by lazy {
        EditSupplierViewModel(supplierViewModel)
    }

    private var supplierId: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments?.containsKey(ARG_SUPPLIER_ID) == true) {
            supplierId = arguments?.getInt(ARG_SUPPLIER_ID)
        }

        val supplierName = arguments?.getString(ARG_SUPPLIER_NAME)
        val supplierType = arguments?.getString(ARG_SUPPLIER_TYPE)

        // Устанавливаем данные, если редактируем поставщика
        binding.supplierNameEditText.setText(supplierName ?: "")
        binding.supplierTypeEditText.setText(supplierType ?: "")

        binding.saveButton.setOnClickListener {
            val name = binding.supplierNameEditText.text.toString()
            val type = binding.supplierTypeEditText.text.toString()

            if (name.isNotEmpty() && type.isNotEmpty()) {
                viewModel.saveSupplier(
                    supplierId,
                    name,
                    type,
                    onSuccess = {
                        setFragmentResult("supplier_updated", Bundle()) // Сообщаем о сохранении
                        parentFragmentManager.popBackStack()
                        Toast.makeText(requireContext(), "Поставщик сохранён", Toast.LENGTH_SHORT)
                            .show()
                                },
                    onError = { /* Показать ошибку */
                        Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
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
        private const val ARG_SUPPLIER_ID = "supplier_id"
        private const val ARG_SUPPLIER_NAME = "supplier_name"
        private const val ARG_SUPPLIER_TYPE = "supplier_type"

        fun newInstance(counterpartyEntity: CounterpartyEntity? = null): EditSupplierFragment {
            val fragment = EditSupplierFragment()
            val args = Bundle()
            counterpartyEntity?.let {
                it.id?.let { it1 -> args.putInt(ARG_SUPPLIER_ID, it1) }
                args.putString(ARG_SUPPLIER_NAME, it.name)
                args.putString(ARG_SUPPLIER_TYPE, it.type)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
