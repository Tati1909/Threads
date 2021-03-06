package com.example.threads

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
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

    private var counterThread = 0

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

        /**
         * Кнопка РАСЧЕТ В ОТДЕЛЬНОМ ПОТОКЕ.
         */
        binding.calcThreadBtn.setOnClickListener {

            Thread {
                /**
                 * counterThread =1
                 */
                counterThread++
                /**
                 * Расчеты производим в отдельном потоке и присваиваем результат calculatedText
                 */
                val calculatedText = startCalculations(binding.editText.text.toString().toInt())
                /**
                 * Нельзя обращаться к элементам UI(например textView) не из UI-потока(из отдельного потока).
                 * «Только оригинальный поток (основной поток), который создал эти view, может вызывать эти view»
                 * Решить эту проблему в Android можно через Runnable — интерфейс с методом run(),
                 * который выполняется в потоке GUI.
                 * Activity.runOnUiThread(Runnable) — выполняет метод run() в UI-потоке
                незамедлительно, а если поток занят, выполнение ставится в очередь.
                 */
                activity?.runOnUiThread {

                    binding.textView.text = calculatedText
                    /**
                     * Добавляется AppCompatTextView - надпись внизу: из потока 1.
                     */
                    binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                        text = String.format(getString(R.string.from_thread), counterThread)
                        textSize = resources.getDimension(R.dimen.main_container_text_size)
                    })
                }
            }.start()
        }

        /**
         * Кнопка HandlerThread.
         * Создаем handlerThread и вызываем start
         * Создаем handler и ложим в него handlerThread.looper
         */
        val handlerThread = HandlerThread(getString(R.string.my_handler_thread))
        handlerThread.start()
        val handler = Handler(handlerThread.looper)

        binding.calcThreadHandler.setOnClickListener {
            binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                /**
                 * Добавляется AppCompatTextView - надпись внизу:
                 * Находимся в потоке: My_Handler_Thread
                 */
                text = String.format(
                    getString(R.string.calculate_in_thread),
                    handlerThread.name
                )
                textSize = resources.getDimension(R.dimen.main_container_text_size)
            })
            /**
             * Handler.post(Runnable) запоминает поток, в котором создан Handler(в UI потоке),
             * после чего метод run() будет вызываться в этом потоке.
             * Т .е . считаем в отдельном потоке, а выводим строки в майн потоке.
             */
            handler.post {
                startCalculations(binding.editText.text.toString().toInt())
                /**
                 * View.post(Runnable) ставит метод run() в очередь UI-потока.
                 */
                binding.mainContainer.post {
                    binding.mainContainer.addView(AppCompatTextView(it.context).apply {
                        /**
                         * Добавляется AppCompatTextView - надпись внизу:
                         * Считаем в потоке main
                         */
                        text = String.format(
                            getString(R.string.in_thread),
                            Thread.currentThread().name
                        )
                        textSize =
                            resources.getDimension(R.dimen.main_container_text_size)
                    })
                }
            }
        }
    }

    /**
     * Метод startCalculations в цикле высчитывает дату, пока она не станет меньше одной секунды.
    Грубо говоря, мы загружаем расчётами даты основной поток приложения на одну секунду. Вызываем
    метод startCalculations каждый раз, когда нажимаем на кнопку «Расчёт».
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