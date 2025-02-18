package com.nayya.myktor.ui.root

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentRootBinding
import com.nayya.myktor.utils.viewBinding

class RootFragment : Fragment(R.layout.fragment_root) {

    private val binding by viewBinding<FragmentRootBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.calculationButton.setOnClickListener {
            getController().openTestCollServer()
        }

        binding.productAccountingButton.setOnClickListener {
            getController().openProductAccounting()
        }
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        fun openTestCollServer()
        fun openProductAccounting()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {
        @JvmStatic
        fun newInstance() = RootFragment()
    }
}