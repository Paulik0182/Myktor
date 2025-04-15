package com.nayya.myktor.ui.product.productview

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nayya.myktor.R

class CodesBottomSheetDialog : BottomSheetDialogFragment() {

    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var originalCodes: List<String>

    /**
     * У стандартного BottomSheetDialogFragment есть проблемы с анимацыей. Баг - эффект двойного
     * закрытия BottomSheet.
     * Лечится переопределением стиля. Нужно отключить анимацию. При необходимости задать анимацию в ручную.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme_Compat)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_codes_bottom_sheet, container, false)

        val listView = view.findViewById<ListView>(R.id.listView)
        val search = view.findViewById<EditText>(R.id.searchView)
        val emptyView = view.findViewById<TextView>(R.id.tvEmpty)

        // Получаем оригинальный список кодов
        originalCodes = arguments?.getStringArrayList(ARG_CODES)?.toList() ?: emptyList()

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, originalCodes.toMutableList())
        listView.adapter = adapter

        // Не позволяем родителю перехватывать скролл вверх
        listView.setOnTouchListener { v, event ->
            v.parent?.requestDisallowInterceptTouchEvent(true)
            false
        }

        // Кнопка очистки (крестик)
        search.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = 2
                val drawable = search.compoundDrawables[drawableEnd]
                if (drawable != null &&
                    event.rawX >= (search.right - drawable.bounds.width() - search.paddingEnd)
                ) {
                    search.text.clear()
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }

        // Фильтрация по вводу
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString().orEmpty()
                val cleaned = input.filter { it.isDigit() }

                if (input != cleaned) {
                    search.setText(cleaned)
                    search.setSelection(cleaned.length)
                    return
                }

                val filtered = if (cleaned.isEmpty()) {
                    originalCodes
                } else {
                    originalCodes.filter { it.contains(cleaned) }
                }

                adapter.clear()
                adapter.addAll(filtered)
                adapter.notifyDataSetChanged()

                emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            }
        })

        return view
    }

    override fun onStart() {
        super.onStart()

        dialog?.let { dlg ->
            val bottomSheet = dlg.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = (resources.displayMetrics.heightPixels * 0.9).toInt()
            bottomSheet?.requestLayout()
        }
    }

    companion object {
        private const val ARG_CODES = "codes"

        fun newInstance(codes: List<String>): CodesBottomSheetDialog {
            return CodesBottomSheetDialog().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_CODES, ArrayList(codes))
                }
            }
        }
    }
}