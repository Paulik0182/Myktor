package com.nayya.myktor.ui.product.editproduct

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.nayya.myktor.R
import com.nayya.myktor.data.RetrofitInstance
import com.nayya.myktor.databinding.FragmentEditProductBinding
import com.nayya.myktor.domain.productentity.MeasurementUnitList
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.ui.product.category.categorypicker.CategoryPickerBottomSheet
import com.nayya.myktor.ui.product.products.ProductViewModel
import com.nayya.myktor.utils.viewBinding

class EditProductFragment : Fragment(R.layout.fragment_edit_product) {

    private val binding by viewBinding<FragmentEditProductBinding>()

    private val productViewModel: ProductViewModel by activityViewModels()
    private lateinit var viewModel: EditProductViewModel

    private var productId: Long? = null
    private var isExpanded = false
    private val maxVisibleLines = 4

    private val linksAdapter = LinksAdapter(
        onLinkChanged = { index, text -> viewModel.updateLink(index, text) },
        onAddClicked = { viewModel.addLink() },
        onRemoveClicked = { index -> viewModel.removeLink(index) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getLong(ARG_PRODUCT_ID)

        viewModel = EditProductViewModel(RetrofitInstance.api, productViewModel)
        productId?.let { viewModel.loadProduct(it) }

        viewModel.fetchCategories()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvLinks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLinks.adapter = linksAdapter

        // Spinner для единиц измерения
        val defaultUnits = listOf(
            MeasurementUnitList(1, "Штуки", "шт."),
            MeasurementUnitList(2, "Килограммы", "кг"),
            MeasurementUnitList(3, "Литры", "л")
        )

        viewModel.setUnits(defaultUnits)

        viewModel.units.observe(viewLifecycleOwner) { units ->
            val unitNames = units.map { "${it.name} (${it.abbreviation})" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerUnits.adapter = adapter

            val selectedIndex = units.indexOfFirst { it.id == viewModel.selectedUnitId.value }
            if (selectedIndex >= 0) binding.spinnerUnits.setSelection(selectedIndex)

            binding.spinnerUnits.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    viewModel.selectedUnitId.value = units[position].id
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        viewModel.product.observe(viewLifecycleOwner) { product ->
            binding.productNameEditText.setText(product.name)
            binding.descriptionEditText.setText(product.description)
            binding.priceEditText.setText(product.price.toPlainString())
            displayCategoriesHierarchically(product)
        }

        viewModel.links.observe(viewLifecycleOwner) {
            linksAdapter.submitList(it.toList())
        }

        binding.clCategoryPicker.setOnClickListener {
            val dialog = CategoryPickerBottomSheet.newInstance(
                categories = viewModel.categories.value ?: emptyList(),
                selectedCategoryIds = viewModel.selectedCategoryIds,
                selectedSubcategoryIds = viewModel.selectedSubcategoryIds
            )
            dialog.setOnApplyListener { selectedCats, selectedSubs ->
                viewModel.setCategorySelection(selectedCats.toList(), selectedSubs.toList())
            }
            dialog.show(parentFragmentManager, "CategoryPickerBottomSheet")
        }

        binding.saveButton.setOnClickListener {
            val name = binding.productNameEditText.text.toString()
            val description = binding.descriptionEditText.text.toString()
            val priceText = binding.priceEditText.text.toString()

            if (name.isBlank() || description.isBlank() || priceText.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceText.toBigDecimalOrNull()
            if (price == null) {
                Toast.makeText(requireContext(), "Некорректная цена", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveProduct(
                name = name,
                description = description,
                price = price,
                onSuccess = {
                    Toast.makeText(requireContext(), "Сохранено", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                },
                onError = {
                    Toast.makeText(requireContext(), "Ошибка: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.btnAddLink.setOnClickListener {
            viewModel.addLink()
        }
    }

    private fun displayCategoriesHierarchically(product: Product) {
        val container = binding.layoutCategories
        val toggleButton = binding.btnToggleCategories
        container.removeAllViews()

        val categoryMap = product.categories.orEmpty().associateBy { it.id }
        val subByCategory = product.subcategories.orEmpty().groupBy { it.categoryId }

        // Заголовок с иконкой редактирования
        val headerLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val tvHeader = TextView(requireContext()).apply {
            text = "Категории и подкатегории"
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.grey_text))
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }

        val editIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_edit)
            setColorFilter(ContextCompat.getColor(context, R.color.grey_text))
            setOnClickListener {
                val dialog = CategoryPickerBottomSheet.newInstance(
                    categories = viewModel.categories.value ?: emptyList(),
                    selectedCategoryIds = viewModel.selectedCategoryIds,
                    selectedSubcategoryIds = viewModel.selectedSubcategoryIds
                )
                dialog.setOnApplyListener { selectedCats, selectedSubs ->
                    viewModel.setCategorySelection(selectedCats.toList(), selectedSubs.toList())
                }
                dialog.show(parentFragmentManager, "CategoryPickerBottomSheet")
            }
        }

        headerLayout.addView(tvHeader)
        headerLayout.addView(editIcon)
        container.addView(headerLayout)


        product.categoryIds.forEach { catId ->
            val category = categoryMap[catId] ?: return@forEach
            val tvCategory = TextView(requireContext()).apply {
                text = "- ${category.name}"
                setTextColor(ContextCompat.getColor(context, R.color.grey_text))
            }
            container.addView(tvCategory)

            subByCategory[catId]?.forEach { sub ->
                val tvSub = TextView(requireContext()).apply {
                    text = "\t\t${sub.name}" // визуальный отступ
                    setTextColor(ContextCompat.getColor(context, R.color.grey_text))
                }
                container.addView(tvSub)
            }
        }

        // После отрисовки — проверим, нужно ли обрезать
        container.post {
            val fullHeight = container.height
            container.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            val headerHeight = container.getChildAt(0)?.height ?: 0
            val lineHeight = container.getChildAt(1)?.height ?: 0
            val visibleHeight = headerHeight + lineHeight * maxVisibleLines

            if (fullHeight > visibleHeight) {
                container.layoutParams.height = visibleHeight
                container.requestLayout()
                toggleButton.visibility = View.VISIBLE
            }
        }

        toggleButton.setOnClickListener {
            isExpanded = !isExpanded

            val initialHeight = container.height
            val targetHeight = if (isExpanded) {
                ViewGroup.LayoutParams.WRAP_CONTENT.also {
                    container.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    container.measure(
                        View.MeasureSpec.makeMeasureSpec(container.width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.UNSPECIFIED
                    )
                }
                container.measuredHeight
            } else {
                val headerHeight = container.getChildAt(0)?.height ?: 0
                val rowHeight = container.getChildAt(1)?.height ?: 0
                headerHeight + rowHeight * maxVisibleLines
            }

            val animator = ValueAnimator.ofInt(initialHeight, targetHeight)
            animator.addUpdateListener {
                val value = it.animatedValue as Int
                container.layoutParams.height = value
                container.requestLayout()
            }
            animator.duration = 250
            animator.start()

            toggleButton.text = if (isExpanded) "Свернуть" else "Ещё"
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        fun newInstance(productId: Long? = null): EditProductFragment {
            return EditProductFragment().apply {
                arguments = Bundle().apply {
                    productId?.let { putLong(ARG_PRODUCT_ID, it) }
                }
            }
        }
    }
}
