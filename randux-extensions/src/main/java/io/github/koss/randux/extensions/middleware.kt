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

package io.github.koss.randux.extensions

import arrow.core.Either
import arrow.core.Option
import io.github.koss.randux.utils.*
import io.reactivex.disposables.Disposable

/**
 * To remove some of the complicated currying that middleware entails, a user could use this function
 * and Kotlin delegation to simplify middleware creation
 */
fun middleware(block: (api: MiddlewareAPI, next: Dispatch, action: Either<AsyncAction, Action>) -> Option<Any>): Middleware {
    return object : Middleware {
        override fun invoke(api: MiddlewareAPI): (next: Dispatch) -> Dispatch {
            return { next ->
                inner@ { action ->
                    return@inner block(api, next, action)
                }
            }
        }
    }
}

/**
 * Creates a middleware which handles a single disposable. A disposable handling mode can be
 * set, allowing customisability of what happens when a new disposable is added
 */
fun disposableMiddleware(
        mode: DisposableMode = DisposableMode.DISPOSE_OLD,
        block: (api: MiddlewareAPI, next: Dispatch, action: Either<AsyncAction, Action>, setDisposable: (Disposable) -> Unit) -> Option<Any>): Middleware {
    var disposable: Disposable? = null

    return object : Middleware {
        override fun invoke(api: MiddlewareAPI): (next: Dispatch) -> Dispatch {
            return { next ->
                inner@ { action ->
                    return@inner block(api, next, action, { newDisposable ->
                        when (mode) {
                            DisposableMode.DISPOSE_OLD -> {
                                disposable?.dispose()
                                disposable = newDisposable
                            }
                            DisposableMode.DROP_NEW -> {
                                newDisposable.dispose()
                            }
                        }
                    })
                }
            }
        }
    }
}

enum class DisposableMode {
    DISPOSE_OLD,
    DROP_NEW
}