package io.github.koss.randux.sample.examples.thunkasync

import io.github.koss.randux.utils.Action
import io.github.koss.randux.utils.Reducer
import io.github.koss.randux.utils.State

class SimpleAsyncReducer: Reducer {
    override fun invoke(currentState: State, incomingAction: Action): State =
            when (incomingAction) {
                BeginLoad -> Loading
                FinishLoad -> Loaded
                else -> currentState
            }
}