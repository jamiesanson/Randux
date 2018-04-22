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

import io.github.koss.randux.utils.Dispatch
import io.github.koss.randux.utils.Middleware
import io.github.koss.randux.utils.MiddlewareAPI
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class FakeApiMiddleware: Middleware {
    private var disposable: Disposable? = null

    override fun invoke(api: MiddlewareAPI): (next: Dispatch) -> Dispatch = { next -> inner@ { action ->
        return@inner when (action) {
            is LoadSomething -> {
                // If the action is LoadSomething, consume the action and dispatch some new ones
                if (disposable?.isDisposed == false) disposable?.dispose()

                api.dispatch(BeginLoad)

                disposable = Completable.complete()
                        .delay(3000, TimeUnit.MILLISECONDS)
                        .subscribe {
                            api.dispatch(FinishLoad)
                        }
            }
            else -> next(action)
    }}}
}