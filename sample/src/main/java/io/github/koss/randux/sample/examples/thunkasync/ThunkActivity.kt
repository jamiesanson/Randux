package io.github.koss.randux.sample.examples.thunkasync

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import arrow.core.Either.Companion.right
import arrow.core.None
import arrow.core.Some
import io.github.koss.randux.applyMiddleware
import io.github.koss.randux.createStore
import io.github.koss.randux.extensions.globalStateRx
import io.github.koss.randux.sample.R
import io.github.koss.randux.sample.util.LoggingMiddleware
import io.github.koss.randux.utils.State
import io.github.koss.randux.utils.Store
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_sample_async.*
import org.jetbrains.anko.contentView
import org.jetbrains.anko.design.snackbar
import java.util.concurrent.TimeUnit

class ThunkActivity: AppCompatActivity() {
    private lateinit var store: Store

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_async)
        setTitle(R.string.thunk_async_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        store = createStore(
                reducer = SimpleAsyncReducer(),
                preloadedState = None,
                enhancer = Some(applyMiddleware(ThunkMiddleware(), LoggingMiddleware()))
        )

        disposable = store.globalStateRx
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onNewState)

        loadButton.setOnClickListener { _ ->
            store.dispatch(right(loadSomething()))
        }
    }

    private fun loadSomething(): ThunkFunction = { dispatch, _ ->
        dispatch(right(BeginLoad))

        Completable.complete()
                .delay(3000, TimeUnit.MILLISECONDS)
                .subscribe {
                    dispatch(right(FinishLoad))
                }

        None
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