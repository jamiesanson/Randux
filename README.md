# Randux
Randux is a minimal Redux implementation for Android. It follows the official Redux.js docs as closely as possible including, quite importantly, middleware. 

## Supporting Technologies
Redux is an architecture which pairs well with functional programming paradigms. Kotlin is a language which is flexible enough to provide relatively good support for functional programming through the usage of libraries like Arrow.kt. 

## Usage
Define your app state, with as much nesting as you want. This must be parcelable to enable state persistence. Also, `@lenses` is used for composable nested transforms. Your app doesn't need to worry about this, but Randux sure does:

### App setup

```kotlin
@lenses
@Parcelize
data class AppState(
  val homeState: HomeState = HomeState.Default,
  val profileState: ProfileState = ProfileState.Default
): Parcelable, State
```

In your application class, register the `Store`:

```kotlin
class TestApp: Application() {

  lateinit var store: Store

  override fun onCreate() {
    store = Store
      .withInitialState(AppState())
      .applyConfiguration(Configuration()) // Configure Randux however you want
      .create()
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
class FetchItemsMiddleware<FetchItems> @Inject constructor(val itemsRepo: ItemsRepository): Middleware<FetchItems> {

  override fun call(action: FetchItems, actionTrigger: (Action) -> Unit) {
    actionTrigger(Loading)
    itemsRepo.getItemsRx().subscribeBy(
      onNext = { actionTrigger(Loaded(items = it)) }
    )
  }

}
```

2. Create your local reducer; this is required:
```kotlin
fun reduce(action: Action, oldState: HomeState): HomeState {
  return when (action) {
    is Loading -> oldState.copy(loading = true)
  }
}
```

3. Rest of the owl:
```kotlin
class HomeFragment: Fragment() {

  @Inject
  lateinit var middleware: List<Middleware>
  
  @Inject
  lateinit var reducer: Reducer
  
  override fun onCreate(savedInstanceState: Bundle) {
    application.store
      .applyMiddleware(middleware)
      .addReducer(reducer)
      .observeState(::onStateChanged)
  }
  
  private fun onStateChanged(homeState: HomeState) {
  
  }
}
```

That's it!
