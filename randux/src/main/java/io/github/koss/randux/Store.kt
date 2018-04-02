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

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData

/**
 * Class implementing a Store
 */
class Store private constructor(
        initialState: State,
        private val configuration: Configuration
){

    /**
     * Internal state variable. Can be mapped to Rx if preferable
     */
    private val state = MutableLiveData<State>()

    /**
     * Internal actions stream
     */
    private val actions = MutableLiveData<Action>()

    init {
        state.value = initialState
    }

    fun applyMiddleware(middleware: List<Middleware>) {

    }

    fun <T: State> addReducer(reducer: Reducer<T>): LiveData<T> {

    }

    /**
     * Builder implementation for the Store
     */
    companion object Builder {
        private var initialState: State? = null
        private var configuration: Configuration = object : Configuration() {}

        @JvmStatic
        fun withInitialState(state: State): Builder{
            this.initialState = state
            return this
        }

        @JvmStatic
        fun applyConfiguration(configuration: Configuration): Builder {
            this.configuration = configuration
            return this
        }

        fun build(): Store {
            return Store(
                    initialState ?: throw IllegalStateException("You must provide an initial state"),
                    configuration)
        }
    }

    /**
     * Abstract class for configuring Randux
     */
    abstract class Configuration {

        /**
         * Middleware to be applied on a global level
         */
        open val globalMiddleware: List<Middleware> = emptyList()
    }

}