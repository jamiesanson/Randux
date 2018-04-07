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

package io.github.koss.randux.core

import arrow.core.Either
import arrow.core.Option

/**
 * This file is an implementation of signatures found in the official redux glossary. See here:
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md
 *
 * Arrow is used to implement Either and Option constructs.
 */

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#state
 */
typealias State = Any

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#action
 */
typealias Action = Any

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#reducer
 */
typealias Reducer = (currentState: State, incomingAction: Action) -> State


/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#dispatching-function
 */
typealias BaseDispatch = (Action) -> Action
typealias Dispatch = (Either<Action, AsyncAction>) -> Option<Any>

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#action-creator
 */
typealias ActionCreator = () -> Either<Action, AsyncAction>

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#async-action
 */
typealias AsyncAction = Any

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#middleware
 */
open class MiddlewareAPI(val dispatch: Dispatch, val getState: () -> State)
typealias Middleware = (api: MiddlewareAPI) -> ((next: Dispatch) -> Dispatch)

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#store
 */
interface Store {
    val dispatch: Dispatch
    val getState: () -> State
    val subscribe: (listener: () -> Unit) -> (() -> Unit)
    val replaceReducer: (reducer: Reducer) -> Unit
}

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#store-creator
 */
typealias StoreCreator = (reducer: Reducer, preloadedState: Option<State>) -> Store

/**
 * https://github.com/reactjs/redux/blob/master/docs/Glossary.md#store-enhancer
 */
typealias StoreEnhancer = (next: StoreCreator) -> StoreCreator