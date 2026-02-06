package com.example.ridenow.ui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.example.ridenow.R;
import com.example.ridenow.dto.model.LocationDTO;
import com.example.ridenow.dto.model.PolylinePointDTO;
import com.example.ridenow.dto.vehicle.VehicleResponseDTO;
import com.example.ridenow.util.AddressUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reusable MapView component for displaying routes with start, stop, and end markers
 * Also supports displaying vehicle markers and handling map events
 *
 * This component encapsulates all the complexity of OpenStreetMap integration and provides
 * a simple API for displaying routes throughout the app.
 *
 * USAGE EXAMPLES:
 *
 * 1. Basic route display:
 *    RouteMapView mapView = findViewById(R.id.routeMapView);
 *    mapView.displayRoute(startLocation, endLocation, null, null);
 *
 * 2. Display vehicles on map:
 *    mapView.displayVehicles(vehiclesList);
 *    mapView.setMapEventsReceiver(mapEventsReceiver);
 *
 * 3. Add driver location:
 *    mapView.addDriverLocation(latitude, longitude);
 *
 * 4. Route with stops and detailed polyline:
 *    mapView.displayRoute(startLocation, endLocation, stopsList, polylinePoints);
 *
 * 5. Customized appearance:
 *    mapView.setRouteColor(Color.RED);
 *    mapView.setRouteWidth(12.0f);
 *    mapView.setShowMarkers(false); // Hide markers, show only route line
 *    mapView.displayRoute(startLocation, endLocation, stopsList, polylinePoints);
 *
 * 6. In XML layout:
 *    <com.example.ridenow.ui.components.RouteMapView
 *        android:id="@+id/routeMapView"
 *        android:layout_width="match_parent"
 *        android:layout_height="300dp" />
 *
 * IMPORTANT: Always call lifecycle methods from your Fragment/Activity:
 *    @Override public void onResume() { super.onResume(); mapView.onResume(); }
 *    @Override public void onPause() { super.onPause(); mapView.onPause(); }
 *    @Override public void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
 *
 * Features:
 * - Automatic zoom to fit entire route
 * - Touch handling that prevents parent scroll interference
 * - Customizable route appearance (color, width)
 * - Optional markers with formatted addresses
 * - Support for complex routes with multiple stops
 * - Handles both detailed polyline data and simple point-to-point routes
 * - Vehicle display with availability status
 * - Driver location tracking
 * - Map events handling
 */
public class RouteMapView extends FrameLayout {

    private MapView mapView;
    private boolean isInitialized = false;
    private MapEventsReceiver mapEventsReceiver;

    // Vehicle tracking
    private Map<String, Marker> vehicleMarkers = new HashMap<>();
    private Marker driverLocationMarker;

    private Marker currentVehicleMarker;

    // Configuration options
    private int routeColor = Color.parseColor("#2196F3"); // Material blue
    private float routeWidth = 8.0f;
    private boolean showMarkers = true;
    private boolean enableZoomControls = false;

    public RouteMapView(Context context) {
        super(context);
        init(context);
    }

    public RouteMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RouteMapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        initializeOSMDroid(context);
        setupMapView();
    }

    private void initializeOSMDroid(Context context) {
        if (!isInitialized) {
            SharedPreferences prefs = context.getSharedPreferences("osmdroid", 0);
            Configuration.getInstance().load(context, prefs);
            isInitialized = true;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupMapView() {
        mapView = new MapView(getContext());
        mapView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Set default zoom
        IMapController mapController = mapView.getController();
        mapController.setZoom(12.0);

        // Handle touch events to prevent parent scrolling interference
        mapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    // Disable parent scrolling when touching the map
                    ViewGroup parent = (ViewGroup) v.getParent();
                    while (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                        if (parent.getParent() instanceof ViewGroup) {
                            parent = (ViewGroup) parent.getParent();
                        } else {
                            break;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Re-enable parent scrolling
                    ViewGroup parentUp = (ViewGroup) v.getParent();
                    while (parentUp != null) {
                        parentUp.requestDisallowInterceptTouchEvent(false);
                        if (parentUp.getParent() instanceof ViewGroup) {
                            parentUp = (ViewGroup) parentUp.getParent();
                        } else {
                            break;
                        }
                    }
                    break;
            }
            return false;
        });

        addView(mapView);
    }

    /**
     * Display a route on the map with start, stop, and end markers
     * @param startLocation Starting point of the route
     * @param endLocation Ending point of the route
     * @param stopLocations List of stop points along the route (can be null or empty)
     * @param polylinePoints Detailed route polyline points (can be null)
     */
    public void displayRoute(LocationDTO startLocation, LocationDTO endLocation,
                             List<LocationDTO> stopLocations, List<PolylinePointDTO> polylinePoints) {

        if (startLocation == null || endLocation == null) {
            return;
        }

        // Clear existing overlays
        mapView.getOverlays().clear();

        List<GeoPoint> allPoints = new ArrayList<>();

        // Collect all points for zoom calculation
        GeoPoint startPoint = new GeoPoint(startLocation.getLatitude(), startLocation.getLongitude());
        GeoPoint endPoint = new GeoPoint(endLocation.getLatitude(), endLocation.getLongitude());
        allPoints.add(startPoint);
        allPoints.add(endPoint);

        // Add stop points
        List<GeoPoint> stopPoints = new ArrayList<>();
        if (stopLocations != null && !stopLocations.isEmpty()) {
            for (LocationDTO stop : stopLocations) {
                GeoPoint stopPoint = new GeoPoint(stop.getLatitude(), stop.getLongitude());
                stopPoints.add(stopPoint);
                allPoints.add(stopPoint);
            }
        }

        // Draw route polyline FIRST (appears under markers)
        drawRouteLine(allPoints, polylinePoints);

        // Add markers ON TOP of route line
        if (showMarkers) {
            addRouteMarkers(startLocation, endLocation, stopLocations);
        }

        // Zoom to fit all points
        zoomToFitRoute(allPoints);

        mapView.invalidate();
    }

    private void drawRouteLine(List<GeoPoint> allPoints, List<PolylinePointDTO> polylinePoints) {
        List<GeoPoint> routePoints;

        if (polylinePoints != null && !polylinePoints.isEmpty()) {
            // Use detailed polyline points from API
            routePoints = new ArrayList<>();
            for (PolylinePointDTO point : polylinePoints) {
                routePoints.add(new GeoPoint(point.getLatitude(), point.getLongitude()));
            }
        } else {
            // Fallback to simple line connecting all points
            routePoints = new ArrayList<>(allPoints);
        }

        if (!routePoints.isEmpty()) {
            Polyline routeLine = new Polyline(mapView);
            routeLine.setPoints(routePoints);
            routeLine.getOutlinePaint().setColor(routeColor);
            routeLine.getOutlinePaint().setStrokeWidth(routeWidth);
            routeLine.getOutlinePaint().setAntiAlias(true);
            routeLine.setInfoWindow(null); // Disable info windows

            mapView.getOverlays().add(routeLine);
        }
    }

    private void addRouteMarkers(LocationDTO startLocation, LocationDTO endLocation, List<LocationDTO> stopLocations) {
        // Add start marker
        Marker startMarker = createMarker(
            new GeoPoint(startLocation.getLatitude(), startLocation.getLongitude()),
            "Start: " + AddressUtils.formatAddress(startLocation.getAddress()),
            "Starting point",
            R.drawable.marker_start
        );
        mapView.getOverlays().add(startMarker);

        // Add stop markers
        if (stopLocations != null && !stopLocations.isEmpty()) {
            for (int i = 0; i < stopLocations.size(); i++) {
                LocationDTO stop = stopLocations.get(i);
                Marker stopMarker = createMarker(
                    new GeoPoint(stop.getLatitude(), stop.getLongitude()),
                    "Stop " + (i + 1) + ": " + AddressUtils.formatAddress(stop.getAddress()),
                    "Stop #" + (i + 1),
                    R.drawable.marker_stop
                );
                mapView.getOverlays().add(stopMarker);
            }
        }

        // Add end marker
        Marker endMarker = createMarker(
            new GeoPoint(endLocation.getLatitude(), endLocation.getLongitude()),
            "End: " + AddressUtils.formatAddress(endLocation.getAddress()),
            "Destination",
            R.drawable.marker_end
        );
        mapView.getOverlays().add(endMarker);
    }

    private Marker createMarker(GeoPoint position, String title, String subDescription, int iconResource) {
        Marker marker = new Marker(mapView);
        marker.setPosition(position);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setTitle(title);
        marker.setSubDescription(subDescription);
        marker.setIcon(ContextCompat.getDrawable(getContext(), iconResource));
        return marker;
    }

    private void zoomToFitRoute(List<GeoPoint> allPoints) {
        if (allPoints.isEmpty()) {
            return;
        }

        if (allPoints.size() == 1) {
            // Single point - center on it with default zoom
            IMapController mapController = mapView.getController();
            mapController.setCenter(allPoints.get(0));
            mapController.setZoom(14.0);
            return;
        }

        // Calculate bounding box
        double minLat = Double.MAX_VALUE;
        double maxLat = Double.MIN_VALUE;
        double minLon = Double.MAX_VALUE;
        double maxLon = Double.MIN_VALUE;

        for (GeoPoint point : allPoints) {
            minLat = Math.min(minLat, point.getLatitude());
            maxLat = Math.max(maxLat, point.getLatitude());
            minLon = Math.min(minLon, point.getLongitude());
            maxLon = Math.max(maxLon, point.getLongitude());
        }

        // Add padding (10% on each side)
        double latPadding = (maxLat - minLat) * 0.1;
        double lonPadding = (maxLon - minLon) * 0.1;

        BoundingBox boundingBox = new BoundingBox(
            maxLat + latPadding,
            maxLon + lonPadding,
            minLat - latPadding,
            minLon - lonPadding
        );

        // Zoom to fit the bounding box
        mapView.post(() -> {
            mapView.zoomToBoundingBox(boundingBox, true, 50);
            mapView.invalidate();
        });
    }

    // Configuration methods
    public void setRouteColor(int color) {
        this.routeColor = color;
    }

    public void setRouteWidth(float width) {
        this.routeWidth = width;
    }

    public void setShowMarkers(boolean show) {
        this.showMarkers = show;
    }

    public void setEnableZoomControls(boolean enable) {
        this.enableZoomControls = enable;
        if (mapView != null) {
            mapView.setBuiltInZoomControls(enable);
        }
    }

    // Lifecycle methods - call these from your Fragment/Activity
    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
        }
    }

    public void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
    }

    public void onDestroy() {
        if (mapView != null) {
            mapView.onDetach();
        }
    }

    /**
     * Get the underlying MapView for advanced operations
     */
    public MapView getMapView() {
        return mapView;
    }

    /**
     * Clear all overlays from the map
     */
    public void clearMap() {
        if (mapView != null) {
            mapView.getOverlays().clear();
            vehicleMarkers.clear();
            driverLocationMarker = null;
            mapView.invalidate();
        }
    }

    /**
     * Add a temporary marker to the map (useful for showing suggestion selection)
     */
    public void addTemporaryMarker(double latitude, double longitude, String title) {
        if (mapView == null) return;
        Marker m = createMarker(new GeoPoint(latitude, longitude), title, "", R.drawable.marker_start);
        mapView.getOverlays().add(m);
        IMapController mapController = mapView.getController();
        mapController.setCenter(new GeoPoint(latitude, longitude));
        mapView.invalidate();
    }

    /**
     * Display vehicles on the map
     * @param vehicles List of vehicles to display
     */
    public void displayVehicles(List<VehicleResponseDTO> vehicles) {
        if (vehicles == null) return;

        // Clear existing vehicle markers (but keep driver marker and map events overlay)
        clearVehicleMarkers();

        for (VehicleResponseDTO vehicle : vehicles) {
            if (vehicle.getLocation() != null) {
                addVehicleMarker(vehicle);
            }
        }

        mapView.invalidate();
    }

    /**
     * Add or update a driver location marker
     * @param latitude Driver latitude
     * @param longitude Driver longitude
     */
    public void addDriverLocation(double latitude, double longitude) {
        // Remove existing driver marker if it exists
        if (driverLocationMarker != null) {
            mapView.getOverlays().remove(driverLocationMarker);
        }

        GeoPoint driverPoint = new GeoPoint(latitude, longitude);
        driverLocationMarker = new Marker(mapView);
        driverLocationMarker.setPosition(driverPoint);
        driverLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        driverLocationMarker.setTitle("Your Location");
        driverLocationMarker.setSubDescription("Driver");
        driverLocationMarker.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_driver_location));

        mapView.getOverlays().add(driverLocationMarker);
        mapView.invalidate();
    }

    /**
     * Center the map on a specific location
     * @param latitude Location latitude
     * @param longitude Location longitude
     */
    public void centerOnLocation(double latitude, double longitude) {
        IMapController mapController = mapView.getController();
        GeoPoint point = new GeoPoint(latitude, longitude);
        mapController.setCenter(point);
    }

    /**
     * Set up map events receiver for handling map interactions
     * @param receiver Map events receiver implementation
     */
    public void setMapEventsReceiver(MapEventsReceiver receiver) {
        this.mapEventsReceiver = receiver;
        if (mapView != null) {
            // Remove existing map events overlay if any
            mapView.getOverlays().removeIf(overlay -> overlay instanceof MapEventsOverlay);

            // Add new map events overlay at the beginning (under other overlays)
            MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(receiver);
            mapView.getOverlays().add(0, mapEventsOverlay);
        }
    }

    /**
     * Get the current map center
     * @return GeoPoint representing the current map center
     */
    public GeoPoint getMapCenter() {
        if (mapView != null) {
            return (GeoPoint) mapView.getMapCenter();
        }
        return null;
    }

    /**
     * Update vehicle marker for current ride tracking
     * @param vehicleLocation Current location of the vehicle
     */
    public void updateVehicleMarker(LocationDTO vehicleLocation) {
        if (vehicleLocation == null || mapView == null) return;

        GeoPoint vehiclePoint = new GeoPoint(
            vehicleLocation.getLatitude(),
            vehicleLocation.getLongitude()
        );

        // Remove existing vehicle marker if any
        if (currentVehicleMarker != null) {
            mapView.getOverlays().remove(currentVehicleMarker);
        }

        // Create new vehicle marker
        currentVehicleMarker = new Marker(mapView);
        currentVehicleMarker.setPosition(vehiclePoint);
        currentVehicleMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        currentVehicleMarker.setTitle("Your Vehicle");
        currentVehicleMarker.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_current_vehicle));

        mapView.getOverlays().add(currentVehicleMarker);
        mapView.invalidate();
    }

    /**
     * Center the map on a specific location
     * @param location Location to center on
     */
    public void centerOnLocation(LocationDTO location) {
        if (location == null || mapView == null) return;

        GeoPoint center = new GeoPoint(location.getLatitude(), location.getLongitude());
        IMapController mapController = mapView.getController();
        mapController.animateTo(center);
    }

    /**
     * Update driver marker for current location tracking
     * @param driverLocation Current location of the driver
     */
    public void updateDriverMarker(LocationDTO driverLocation) {
        if (driverLocation == null || mapView == null) return;

        GeoPoint driverPoint = new GeoPoint(
            driverLocation.getLatitude(),
            driverLocation.getLongitude()
        );

        // Remove existing driver marker if any
        if (driverLocationMarker != null) {
            mapView.getOverlays().remove(driverLocationMarker);
        }

        // Create new driver marker
        driverLocationMarker = new Marker(mapView);
        driverLocationMarker.setPosition(driverPoint);
        driverLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        driverLocationMarker.setTitle("Your Location");
        driverLocationMarker.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_driver_location));

        mapView.getOverlays().add(driverLocationMarker);
        mapView.invalidate();
    }

    private void clearVehicleMarkers() {
        for (Marker marker : vehicleMarkers.values()) {
            mapView.getOverlays().remove(marker);
        }
        vehicleMarkers.clear();
    }

    private void addVehicleMarker(VehicleResponseDTO vehicle) {
        GeoPoint vehiclePoint = new GeoPoint(
            vehicle.getLocation().getLatitude(),
            vehicle.getLocation().getLongitude()
        );

        Marker marker = new Marker(mapView);
        marker.setPosition(vehiclePoint);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setTitle("Vehicle: " + vehicle.getLicencePlate());
        marker.setSubDescription(vehicle.getAvailable() ? "Available" : "Not Available");

        // Set different icons based on availability
        if (vehicle.getAvailable()) {
            marker.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_car_available));
        } else {
            marker.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_car_unavailable));
        }

        vehicleMarkers.put(vehicle.getLicencePlate(), marker);
        mapView.getOverlays().add(marker);
    }
}
