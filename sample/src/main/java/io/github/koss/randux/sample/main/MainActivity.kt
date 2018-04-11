/*
 * MIT License
 *
 * Copyright (c) 2018 K-OSS Development
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.koss.randux.sample.main

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import arrow.core.Either
import io.github.koss.randux.sample.R
import io.github.koss.randux.sample.SampleApp

import io.github.koss.randux.utils.State
import io.github.koss.randux.utils.Store
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.design.snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var unsubscribe: () -> Unit

    private lateinit var store: Store

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = (application as SampleApp).store
        unsubscribe = store.subscribe({ onStateChanged(store.getState()) })

        loadButton.setOnClickListener { _ ->
            store.dispatch(Either.Left(LoadSomething))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unsubscribe()
    }

    private fun onStateChanged(state: State) {
        when (state) {
            Empty -> {}
            Loading -> { snackbar(contentView!!, "Loading") }
            Loaded -> { snackbar(contentView!!, "Loaded") }
        }
    }
}
