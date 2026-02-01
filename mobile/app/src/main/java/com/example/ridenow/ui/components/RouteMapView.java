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
import com.example.ridenow.dto.model.Location;
import com.example.ridenow.dto.model.PolylinePoint;
import com.example.ridenow.util.AddressUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable MapView component for displaying routes with start, stop, and end markers
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
 * 2. Route with stops and detailed polyline:
 *    mapView.displayRoute(startLocation, endLocation, stopsList, polylinePoints);
 *
 * 3. Customized appearance:
 *    mapView.setRouteColor(Color.RED);
 *    mapView.setRouteWidth(12.0f);
 *    mapView.setShowMarkers(false); // Hide markers, show only route line
 *    mapView.displayRoute(startLocation, endLocation, stopsList, polylinePoints);
 *
 * 4. In XML layout:
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
 */
public class RouteMapView extends FrameLayout {

    private MapView mapView;
    private boolean isInitialized = false;

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
    public void displayRoute(Location startLocation, Location endLocation,
                           List<Location> stopLocations, List<PolylinePoint> polylinePoints) {

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
            for (Location stop : stopLocations) {
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

    private void drawRouteLine(List<GeoPoint> allPoints, List<PolylinePoint> polylinePoints) {
        List<GeoPoint> routePoints;

        if (polylinePoints != null && !polylinePoints.isEmpty()) {
            // Use detailed polyline points from API
            routePoints = new ArrayList<>();
            for (PolylinePoint point : polylinePoints) {
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

    private void addRouteMarkers(Location startLocation, Location endLocation, List<Location> stopLocations) {
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
                Location stop = stopLocations.get(i);
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
            mapView.invalidate();
        }
    }
}
