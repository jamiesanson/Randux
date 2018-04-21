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

package io.github.koss.randux.sample.examples.simpleasync

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import arrow.core.Some
import io.github.koss.randux.applyMiddleware
import io.github.koss.randux.createStore
import io.github.koss.randux.extensions.dispatchAsync
import io.github.koss.randux.extensions.globalStateRx
import io.github.koss.randux.sample.R
import io.github.koss.randux.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_sample_async.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.design.snackbar

/**
 * Simple asynchronous application of Randux. Uses a single store with no state nesting.
 *
 * Explanation:
 *  - When the button is pressed, an [AsyncAction] is dispatched to the store
 *  - The stores dispatch function is wrapped by [FakeApiMiddleware], meaning the middleware is able
 *    to intercept the action
 *  - The [FakeApiMiddleware] dispatches regular actions to the dispatch function, i.e [BeginLoad]
 *  - The reducer is then called with the regular action producing a state change, i.e [BeginLoad] -> [Loading]
 *  - The reduced state is emitted to the subscribers, i.e the onNewState function
 */
class SimpleAsyncActivity: AppCompatActivity() {

    private lateinit var store: Store

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_async)
        setTitle(R.string.simple_async_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        store = createStore(
                reducer = SimpleAsyncReducer(),
                preloadedState = Some(Empty),
                enhancer = Some(applyMiddleware(FakeApiMiddleware()))
        )

        disposable = store.globalStateRx
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onNewState)

        loadButton.setOnClickListener { _ ->
            store.dispatchAsync(LoadSomething)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun onNewState(state: State) {
        when (state) {
            Empty -> {}
            Loading -> { snackbar(contentView!!, "Loading") }
            Loaded -> { snackbar(contentView!!, "Loaded") }
        }
    }
}