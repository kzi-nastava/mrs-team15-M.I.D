# Home Page Implementation

## Overview
The home page displays a map with vehicle locations based on the current map center, and shows the driver's current location if logged in as a driver.

## Components

### Map Display
- Uses OpenStreetMap (OSM) through osmdroid library
- Shows user's current location on initial load or defaults to Belgrade (44.8176, 20.4633)
- Displays vehicle markers with different colors based on availability
- Shows driver's current location marker if logged in as a driver
- Implements MapEventsReceiver to detect map interactions

### Vehicle Display
- Fetches vehicles from API using VehicleService.getAllVehicles(lat, lon)
- Green car icons (🚗) for available vehicles  
- Red car icons (🚫) for unavailable vehicles
- Shows vehicle license plate and availability status in marker popup
- **Vehicle requests are sent based on map center coordinates, not user location**

### Driver Location Display
- **New Feature**: Displays driver's current location when logged in as a driver
- Uses TokenUtils to check if user role is "DRIVER"
- Shows distinctive blue location marker for the driver's position
- Marker title: "Your Location" with snippet: "Driver"
- Driver marker persists when vehicle markers are refreshed

### Map Interaction
- Long press on map triggers new vehicle request for the current map center
- Vehicles update dynamically as user navigates the map
- Map center coordinates are used for all vehicle API requests
- Driver location marker is preserved during vehicle updates

## Location Services
- Requests ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions
- Uses Android LocationManager (not Google Play Services) for positioning
- Falls back to default Belgrade location if permission denied or location unavailable
- User location is used for initial map positioning and driver marker (if driver)

## Authentication Integration
- Uses TokenUtils to check user authentication and role
- Only shows driver location marker if:
  - User is logged in (TokenUtils.isLoggedIn())
  - User role is "DRIVER" (TokenUtils.getRole())
- Driver marker updates when user location is obtained

## API Integration
- Uses ClientUtils for Retrofit configuration
- VehicleService: GET vehicles/ with lat/lon query parameters from map center
- No ride estimation functionality (form is in layout but not functional)

## Data Flow
1. App launches → Request location permissions
2. Check if user is logged in as driver → Get user location
3. Center map on user location (or default to Belgrade)
4. Load vehicles around map center → Display on map
5. If user is driver → Add driver location marker
6. User moves/interacts with map → Get new map center coordinates
7. Send vehicle request with map center coordinates → Update vehicle markers
8. Driver marker persists through vehicle updates

## Key Features
- **Map-centered vehicle requests**: All vehicle API calls use the map center coordinates
- **Driver location tracking**: Shows current driver position with distinctive marker
- **Role-based display**: Only drivers see their location marker
- **Dynamic loading**: Vehicles refresh when user navigates to new areas
- **Marker persistence**: Driver marker preserved during vehicle updates
- **Authentication aware**: Uses token validation and role checking

## Marker Types
- **Green Car Icons**: Available vehicles (48dp)
- **Red Car Icons**: Unavailable vehicles (48dp) 
- **Blue Location Pin**: Driver's current location (48dp)

## Files Modified
- HomeFragment.java - Added driver location functionality with TokenUtils integration
- fragment_home.xml - Ride estimation form on top (not functional)
- TokenUtils.java - Used for authentication and role checking
- Added ic_driver_location.xml - Blue location marker drawable
