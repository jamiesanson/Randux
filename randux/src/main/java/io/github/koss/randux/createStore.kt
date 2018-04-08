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

import arrow.core.*
import io.github.koss.randux.utils.*
import java.util.*

/**
 * Creates a Redux store that holds the state tree.
 * The only way to change the data in the store is to call `dispatch()` on it.
 *
 * There should only be a single store in your app. To specify how different
 * parts of the state tree respond to actions, you may combine several reducers
 * into a single reducer function by using `combineReducers`.
 *
 * @param {Function} reducer A function that returns the next state tree, given
 * the current state tree and the action to handle.
 *
 * @param {any} [preloadedState] The initial state. You may optionally specify it
 * to hydrate the state from the server in universal apps, or to restore a
 * previously serialized user session.
 * If you use `combineReducers` to produce the root reducer function, this must be
 * an object with the same shape as `combineReducers` keys.
 *
 * @param {Function} [enhancer] The store enhancer. You may optionally specify it
 * to enhance the store with third-party capabilities such as middleware,
 * time travel, persistence, etc. The only store enhancer that ships with Redux
 * is `applyMiddleware()`.
 *
 * @returns {Store} A Redux store that lets you read the state, dispatch actions
 * and subscribe to changes.
 */
fun createStore(reducer: Reducer, preloadedState: Option<State>, enhancer: Option<StoreEnhancer> = None): Store {
    // Apply enhancer is it exists
    if (enhancer.isDefined()) {
        val creator = { _reducer: Reducer, _preloadedState: Option<State> -> createStore(_reducer, _preloadedState) }
        return enhancer.orNull()!!(creator)(reducer, preloadedState)
    }

    return object : Store {
        private var currentReducer = reducer
        private var currentState = preloadedState
        private var currentListeners = mutableListOf<() -> Unit>()
        private var nextListeners: Stack<() -> Unit> = Stack<() -> Unit>().apply {
            currentListeners.forEach { listener -> push(listener) }
        }
        private var isDispatching = false

        init {
            dispatch(Right(ActionTypes.INIT))
        }

        fun ensureCanMutateNextListeners() {
            if (currentListeners == nextListeners) {
                nextListeners = Stack<() -> Unit>().apply {
                    currentListeners.forEach { listener -> push(listener) }
                }
            }
        }

        fun _getState(): State {
            if (isDispatching) {
                throw IllegalStateException(
                        "You may not call store.getState() while the reducer is executing. " +
                                "The reducer has already received the state as an argument. " +
                                "Pass it down from the top reducer instead of reading it from the store."
                )
            }

            return currentState
        }

        /**
         * Adds a change listener. It will be called any time an action is dispatched,
         * and some part of the state tree may potentially have changed. You may then
         * call `getState()` to read the current state tree inside the callback.
         *
         * You may call `dispatch()` from a change listener, with the following
         * caveats:
         *
         * 1. The subscriptions are snapshotted just before every `dispatch()` call.
         * If you subscribe or unsubscribe while the listeners are being invoked, this
         * will not have any effect on the `dispatch()` that is currently in progress.
         * However, the next `dispatch()` call, whether nested or not, will use a more
         * recent snapshot of the subscription list.
         *
         * 2. The listener should not expect to see all state changes, as the state
         * might have been updated multiple times during a nested `dispatch()` before
         * the listener is called. It is, however, guaranteed that all subscribers
         * registered before the `dispatch()` started will be called with the latest
         * state by the time it exits.
         *
         * @param {Function} listener A callback to be invoked on every dispatch.
         * @returns {Function} A function to remove this change listener.
         */
        fun subscribe(listener: () -> Unit): () -> Unit {

            if (isDispatching) {
                throw IllegalStateException(
                        "You may not call store.subscribe() while the reducer is executing. " +
                                "If you would like to be notified after the store has been updated, subscribe from a " +
                                "component and invoke store.getState() in the callback to access the latest state. "
                )
            }

            var isSubscribed = false

            ensureCanMutateNextListeners()

            nextListeners.push(listener)

            return unsubscribe@ {
                if (!isSubscribed) {
                    return@unsubscribe
                }

                if (isDispatching) {
                    throw IllegalStateException(
                            "You may not unsubscribe from a store listener while the reducer is executing. "
                            )
                }

                isSubscribed = false

                ensureCanMutateNextListeners()

                nextListeners.remove(listener)
            }
        }

        /**
         * Dispatches an action. It is the only way to trigger a state change.
         *
         * The `reducer` function, used to create the store, will be called with the
         * current state tree and the given `action`. Its return value will
         * be considered the **next** state of the tree, and the change listeners
         * will be notified.
         *
         * The base implementation only supports plain object actions. If you want to
         * dispatch a Promise, an Observable, a thunk, or something else, you need to
         * wrap your store creating function into the corresponding middleware. For
         * example, see the documentation for the `redux-thunk` package. Even the
         * middleware will eventually dispatch plain object actions using this method.
         *
         * @param {Object} action A plain object representing “what changed”. It is
         * a good idea to keep actions serializable so you can record and replay user
         * sessions, or use the time travelling `redux-devtools`. An action must have
         * a `type` property which may not be `undefined`. It is a good idea to use
         * string constants for action types.
         *
         * @returns {Object} For convenience, the same action object you dispatched.
         *
         * Note that, if you use a custom middleware, it may wrap `dispatch()` to
         * return something else (for example, a Promise you can await).
         */
        fun dispatch(action: Either<AsyncAction, Action>): Option<Any> {
            val act = action.getOrElse {
                throw IllegalArgumentException(
                        "Actions must be an instance of Action. " +
                                "Use custom middleware for AsyncAction."
                )
            }

            if (isDispatching) {
                throw IllegalStateException(
                        "Reducers may not dispatch Actions"
                )
            }

            try {
                isDispatching = true
                currentState = Some(currentReducer(Some(currentState), act))
            } finally {
                isDispatching = false
            }

            currentListeners = nextListeners.toMutableList().also {
                it.map { listener ->
                    listener()
                }
            }

            return Some(act)
        }

        /**
         * Replaces the reducer currently used by the store to calculate the state.
         *
         * You might need this if your app implements code splitting and you want to
         * load some of the reducers dynamically. You might also need this if you
         * implement a hot reloading mechanism for Redux.
         *
         * @param {Function} nextReducer The reducer for the store to use instead.
         * @returns {void}
         */
        fun replaceReducer(nextReducer: Reducer) {
            currentReducer = nextReducer
            dispatch(Right(ActionTypes.REPLACE))
        }

        override val dispatch: Dispatch
            get() = this::dispatch
        override val getState: () -> State
            get() = this::_getState
        override val subscribe: (listener: () -> Unit) -> () -> Unit
            get() = this::subscribe
        override val replaceReducer: (reducer: Reducer) -> Unit
            get() = this::replaceReducer

    }
}