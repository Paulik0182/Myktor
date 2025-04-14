package com.nayya.myktor.ui.product.productview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentViewProductBinding
import com.nayya.myktor.domain.productentity.Product
import com.nayya.myktor.utils.viewBinding
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2

class ViewProductFragment : Fragment(R.layout.fragment_view_product) {

    private val binding by viewBinding<FragmentViewProductBinding>()
    private val viewModel: ViewProductViewModel by viewModels()

    private var product: Product? = null
    private var count = 0

    private var isExpanded = false
    private val maxVisibleLines = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            product = it.getSerializable(ARG_PRODUCT) as? Product
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        product?.let { prod ->
            // Название и описание
            binding.tvProductName.text = prod.name
            setupDescription(prod)

            // Цена
            val price = prod.price.toPlainString()
            val currency = prod.currencySymbol ?: prod.currencyCode ?: ""
//            val currency = prod.currencySymbol.orEmpty()
            binding.tvPrice.text = "$price $currency"

            // Категории
            displayCategoriesHierarchically(prod)

            // Склад
            binding.tvStockInfo.text = viewModel.getStockInfo(prod)

            // Коды
            setupCodes(prod)

            // Ссылки
            binding.tvLinks.text = viewModel.getLinksText(prod)

            // Изображения
            val images = viewModel.getImageUrls(prod)
            val imageAdapter = ImagePagerAdapter(images)
            binding.imageViewPager.adapter = imageAdapter

            binding.tvImageCounter.text = if (images.isNotEmpty()) "1/${images.size}" else ""

            binding.imageViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.tvImageCounter.text = "${position + 1}/${images.size}"
                }
            })

            // Кнопка "назад"
            binding.btnBack.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            // Кнопка "редактировать"
            binding.btnEdit.setOnClickListener {
                product?.id?.let { id ->
                    getController().openEditProductFragment(id)
                }
            }

            // Кнопка "сердечко"
            binding.btnFavorite.setOnClickListener {
                val isFav = it.tag as? Boolean ?: false
                binding.btnFavorite.setImageResource(
                    if (isFav) R.drawable.ic_favorite_border else R.drawable.ic_favorite_filled
                )
                it.tag = !isFav
                val animation = AnimationUtils.loadAnimation(it.context, R.anim.heart_pop)
                it.startAnimation(animation)
            }

            // Кнопка "В корзину"
            binding.btnAddToCart.setOnClickListener {
                count = 1
                updateCounter()
                Toast.makeText(requireContext(), "Заказан 1 шт.", Toast.LENGTH_SHORT).show()
            }

            // Кнопка "+"
            binding.btnPlus.setOnClickListener {
                count++
                updateCounter()
                Toast.makeText(requireContext(), "Заказан $count шт.", Toast.LENGTH_SHORT).show()
            }

            // Кнопка "-"
            binding.btnMinus.setOnClickListener {
                count--
                if (count <= 0) {
                    count = 0
                    showAddToCart()
                } else {
                    updateCounter()
                    Toast.makeText(requireContext(), "Заказан $count шт.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun setupCodes(product: Product) {
        val codesText = viewModel.getCodesText(product)
        binding.tvCodes.text = codesText

        binding.tvCodes.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.tvCodes.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (binding.tvCodes.lineCount > 1) {
                    binding.btnMoreCodes.visibility = View.VISIBLE
                }
            }
        })

        binding.btnMoreCodes.setOnClickListener {
            CodesBottomSheetDialog.newInstance(
                product.productCodes?.map { it.codeName } ?: emptyList()
            ).show(parentFragmentManager, "CodesBottomSheetDialog")
        }
    }

    private fun setupDescription(product: Product) {
        val description = product.description.orEmpty()
        binding.tvProductDescription.text = description

        binding.tvProductDescription.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.tvProductDescription.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (binding.tvProductDescription.lineCount > 1) {
                    binding.btnMoreDescription.visibility = View.VISIBLE
                }
            }
        })

        binding.btnMoreDescription.setOnClickListener {
            DescriptionBottomSheetDialog.newInstance(description)
                .show(parentFragmentManager, "DescriptionBottomSheetDialog")
        }
    }


    private fun displayCategoriesHierarchically(product: Product) {
        val container = binding.layoutCategories
        val toggleButton = binding.btnToggleCategories
        container.removeAllViews()
        toggleButton.visibility = View.GONE

        val categoryMap = product.categories.orEmpty().associateBy { it.id }
        val subByCategory = product.subcategories.orEmpty().groupBy { it.categoryId }
        val categoryIds = product.categoryIds.orEmpty()

        // Заголовок
        val tvHeader = TextView(requireContext()).apply {
            text = "Категории и подкатегории"
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(context, R.color.grey_text))
            setPadding(0, 0, 0, 8)
        }
        container.addView(tvHeader)

        // Если нет категорий вообще — покажем "Нет категорий"
        if (categoryIds.isEmpty()) {
            val tvEmpty = TextView(requireContext()).apply {
                text = "Нет категорий"
                setTextColor(ContextCompat.getColor(context, R.color.grey_text))
            }
            container.addView(tvEmpty)
            return
        }

        // Строим дерево
        categoryIds.forEach { catId ->
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
        fun openEditProductFragment(productId: Long)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    private fun updateCounter() {
        binding.btnAddToCart.visibility = View.GONE
        binding.layoutCounter.visibility = View.VISIBLE
        binding.tvCounter.text = count.toString()
    }

    private fun showAddToCart() {
        binding.layoutCounter.visibility = View.GONE
        binding.btnAddToCart.visibility = View.VISIBLE
    }

    companion object {
        private const val ARG_PRODUCT = "product"

        fun newInstance(product: Product): ViewProductFragment {
            return ViewProductFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PRODUCT, product)
                }
            }
        }
    }
}
