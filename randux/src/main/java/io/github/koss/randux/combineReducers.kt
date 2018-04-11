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

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import io.github.koss.randux.utils.Action
import io.github.koss.randux.utils.ActionTypes
import io.github.koss.randux.utils.Reducer
import io.github.koss.randux.utils.State

/**
 * Function for ensuring each reducer supplied is of correct form
 */
fun assertReducerShape(vararg reducers: Reducer) {
    reducers.forEach { reducer ->
        val initial = object : State() {
            val _internal = Math.random()
        }

        val initialState = reducer(initial, ActionTypes.INIT)

        if (initialState != initial) {
            throw IllegalArgumentException(
                    "Reducer ${reducer::class.java.simpleName} " +
                            "returned undefined during initialization. " +
                            "If the state passed to the reducer is undefined, you must " +
                            "explicitly return the initial state. The initial state may " +
                            "not be undefined. If you don't want to set a value for this reducer, " +
                            "you can use null instead of undefined."
            )
        }

        val type = "@@randux/PROBE_UNKNOWN_ACTION_" +
                Math.random().toString()
        if (reducer(initial, { type }) != initial) {
            throw IllegalArgumentException(
                    "Reducer ${reducer::class.java.simpleName} returned undefined when probed with a random type." +
                            "Don't try to handle ${ActionTypes.INIT} or other actions in the randux " +
                            "namespace. They are considered private. Instead, you must return the " +
                            "current state for any unknown actions, unless it is undefined, " +
                            "in which case you must return the initial state, regardless of the " +
                            "action type. The initial state may not be undefined, but can be null."
            )
        }
    }
}

/**
 * Turns an object whose values are different reducer functions, into a single
 * reducer function. It will call every child reducer, and gather their results
 * into a single state object, whose keys correspond to the keys of the passed
 * reducer functions.
 */
fun combineReducers(vararg reducers: Reducer): Reducer {

    val shapeAssertionError: Option<Throwable> = try {
        assertReducerShape(*reducers)
        None
    } catch (e: Throwable) {
        Some(e)
    }

    return combination@ { previousState: State, action: Action ->
        shapeAssertionError.exists {
            throw it
        }

        var hasChanged = false
        var nextState: State = {}
        reducers.map { reducer ->
            nextState = reducer(previousState, action)
            hasChanged = hasChanged || nextState != previousState
        }

        return@combination if (hasChanged) nextState else previousState
    }

}