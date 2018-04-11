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

package io.github.koss.randux

import arrow.core.Either
import arrow.core.Option
import io.github.koss.randux.utils.*

/**
 * Creates a store enhancer that applies middleware to the dispatch method
 * of the Redux store. This is handy for a variety of tasks, such as expressing
 * asynchronous actions in a concise manner, or logging every action payload.
 *
 * See `redux-thunk` package as an example of the Redux middleware.
 *
 * Because middleware is potentially asynchronous, this should be the first
 * store enhancer in the composition chain.
 *
 * Note that each middleware will be given the `dispatch` and `getState` functions
 * as named arguments.
 *
 * @param [Middleware] middlewares The middleware chain to be applied.
 * @returns [StoreEnhancer] A store enhancer applying the middleware.
 */
fun applyMiddleware(vararg middlewares: Middleware): StoreEnhancer =
        { createStore: StoreCreator -> inner@ { reducer: Reducer, initialState: Option<State> ->
            val store = createStore(reducer, initialState)

            var dispatch: Dispatch = { throw IllegalStateException(
                    "Dispatching while constructing your middleware is not allowed." +
                    "Other middleware would not be applied to this dispatch."
            )}

            var chain: Array<(Dispatch) -> Dispatch> = emptyArray()

            val api = MiddlewareAPI(
                    getState = store::getState,
                    dispatch = { action: Either<AsyncAction, Action> -> dispatch(action) }
            )

            chain = middlewares.map { middleware -> middleware(api) }.toTypedArray()
            dispatch = compose(*chain)(store.dispatch)

            return@inner store override dispatch
    }}

// KOTLIN FUNCTIONS FOR COMPATIBILITY --------------------------------------------------------------

/**
 * Function mimicking the behaviour of compose.js, however is slightly more specific in terms of types
 * and function arguments to support running on the JVM
 */
fun compose(vararg funcs: (Dispatch) -> Dispatch): (Dispatch) -> Dispatch {
    if (funcs.isEmpty()) {
        return { arg -> arg }
    }

    if (funcs.size == 1) {
        return funcs[0]
    }

    return funcs.reduce {a, b -> { d: Dispatch -> a(b(d))}}
}

/**
 * Overrides the dispatch function on a store via delegation
 */
infix fun Store.override(dispatch: Dispatch): Store =
        object: Store by this {
        override val dispatch = dispatch
    }
