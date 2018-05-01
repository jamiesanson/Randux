# Randux
[![](https://jitpack.io/v/k-oss/Randux.svg)](https://jitpack.io/#k-oss/Randux)

Randux is a minimal Redux implementation for Android. It follows the official Redux.js docs as closely as possible including, quite importantly, middleware. 

## Supporting Technologies
Redux is an architecture which pairs well with functional programming paradigms. Kotlin is a language which is flexible enough to provide relatively good support for functional programming through the usage of libraries like Arrow.kt. 

## Usage
Define your app state, with as much nesting as you want. This must be parcelable to enable state persistence. Also, `@lenses` is used for composable nested transforms. Your app doesn't need to worry about this, but Randux sure does:

### App setup

In your application class, register the `Store`:

```kotlin
class TestApp: Application() {

  lateinit var store: Store

  override fun onCreate() {
      val reducers = setOf<Reducer>()
      val middleware = setOf<Middleware>()
      val preloadedState: AppState = Starting

      store = createStore(
              reducer = combineReducers(*reducers),
              preloadedState = preloadedState,
              enhancer = applyMiddleware(*middleware)
      )
  }
}
```
### Component setup

0. Create a local state and update your global state:
```kotlin
@lenses
@Parcelize
data class HomeState(
  val loading: Boolean = true,
  val items: List<Items> = emptyList()
): Parcelable
```

1. Create some middleware for your usecases. These could be logging middleware, crash reporting, or async things like:
```kotlin
class LoggingMiddleware: Middleware {
    override fun invoke(api: MiddlewareAPI): (next: Dispatch) -> Dispatch {
        val (_, getState) = api
        return { next ->
            inner@ { action ->
                Log.d("Logger", "Dispatching action: $action")
                val result = next(action)
                Log.d("Logger", "Next state: ${getState.invoke()}")

                return@inner result
            }
        }
    }
}
```

2. Create your local reducer; this is required:
```kotlin
class SimpleAsyncReducer: Reducer {
    override fun invoke(currentState: State, incomingAction: Action): State =
            when (incomingAction) {
                BeginLoad -> Loading
                FinishLoad -> Loaded
                else -> currentState
            }
}
```

3. Rest of the owl:
```kotlin
class HomeFragment: Fragment() {
  
  override fun onCreate(savedInstanceState: Bundle) {
    application.store
      .subscribe({ onStateChanged(store.getState()) })
  }
  
  private fun onStateChanged(homeState: HomeState) {
  
  }
}
```

That's it!
