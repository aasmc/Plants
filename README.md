# Plants educational app

This app is based on the official Android Developers Codelab 
"Learn advanced coroutines with Kotlin Flow and LiveData" https://developer.android.com/codelabs/advanced-kotlin-coroutines

## Functionality 

When the app first runs, a list of cards appears, each displaying the name and image of a specific plant.

Each Plant has a growZoneNumber, an attribute that represents the region where the plant is most likely to thrive.
Users can tap the filter icon to toggle between showing all plants and plants for a 
specific grow zone, which is hardcoded to zone 9. Press the filter button a few times to see this in action.

## Architecture overview

This app uses Architecture Components to separate the UI code in MainActivity and PlantListFragment 
from the application logic in PlantListViewModel. PlantRepository provides a bridge between the ViewModel 
and PlantDao, which accesses the Room database to return a list of Plant objects. The UI then takes this 
list of plants and displays them in RecyclerView grid layout.
