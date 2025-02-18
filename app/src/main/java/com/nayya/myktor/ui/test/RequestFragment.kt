package com.nayya.myktor.ui.test

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nayya.myktor.R
import com.nayya.myktor.databinding.FragmentRequestBinding
import com.nayya.myktor.utils.viewBinding

class RequestFragment : Fragment(R.layout.fragment_request) {

    private lateinit var viewModel: RequestViewModel
    private val binding by viewBinding<FragmentRequestBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(RequestViewModel::class.java)

        // Кнопка отправки запроса
        binding.buttonSendRequest.setOnClickListener {
            val a = binding.editTextValueA.text.toString()
            val b = binding.editTextValueB.text.toString()

            // Валидация ввода
            if (a.isEmpty() || b.isEmpty()) {
                Toast.makeText(context, "Please enter both values", Toast.LENGTH_SHORT).show()
            } else {
                // Отправка данных на сервер
                viewModel.sendSumRequest(a.toDouble(), b.toDouble())
            }
        }

        // Наблюдение за результатом
        viewModel.result.observe(viewLifecycleOwner, Observer { result ->
            binding.textViewResult.text = result
        })

        // Кнопка получения IP
        binding.buttonGetIP.setOnClickListener {
            viewModel.getIP()
        }

        // Наблюдение за IP
        viewModel.ip.observe(viewLifecycleOwner, Observer { ip ->
            binding.textViewIP.text = ip
        })

        // Кнопка получения текста
        binding.buttonGetText.setOnClickListener {
            viewModel.getTextMessage()
        }

        // Сообщение
        viewModel.message.observe(viewLifecycleOwner, Observer { message ->
            binding.textViewMessage.text = message
        })
    }

    private fun getController(): Controller = activity as Controller

    interface Controller {
        //TODO
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        getController()
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            RequestFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
