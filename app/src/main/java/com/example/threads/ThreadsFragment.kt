package com.example.threads

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.example.threads.databinding.FragmentThreadsBinding
import java.util.*
import java.util.concurrent.TimeUnit

class ThreadsFragment : Fragment() {

    private var _binding: FragmentThreadsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThreadsBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /**
         * Кнопка РАСЧЕТ. Вводим секунды, запускается расчет, выводится число в textView
         */
        binding.button.setOnClickListener {
            binding.textView.text =
                startCalculations(binding.editText.text.toString().toInt())
            binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                text = getString(R.string.in_main_thread)
                textSize =
                    resources.getDimension(R.dimen.main_container_text_size)
            })
        }
    }

    /**
     * Метод startCalculations в цикле высчитывает дату, пока она не станет меньше одной секунды.
    Грубо говоря, мы загружаем расчётами даты основной поток приложения на одну секунду. Вызываем
    метод startCalculations каждый раз, когда нажимаем на кнопку «Расчёт». Запустите приложение и
    посмотрите, как это работает. Приложение застывает на секунду, потому что вычисления
    выполняются в основном потоке и он занят только этим. Но это практически незаметно, потому что
    больше на экране ничего не происходит.
     */
    private fun startCalculations(seconds: Int): String {
        val date = Date()
        var diffInSec: Long
        do {
            val currentDate = Date()
            val diffInMs: Long = currentDate.time - date.time
            diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs)
        } while (diffInSec < seconds)
        return diffInSec.toString()
    }

    companion object {
        fun newInstance() = ThreadsFragment()
    }
}