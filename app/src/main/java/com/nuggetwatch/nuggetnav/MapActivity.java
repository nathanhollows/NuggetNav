package com.nuggetwatch.nuggetnav;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.transition.TransitionManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gte;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.toNumber;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private String storeName;
    private String storeNicename;
    private String storeJson;
    private double storeLat;
    private double storeLng;
    private GeoJsonSource source;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Style loadedStyle;
    private String locations = null;
    private boolean locationsAdded = false;
    private boolean centered = false;
    private ConstraintLayout container;
    private ConstraintSet constraintSet;
    private boolean popupVisible = false;
    private GeoJsonSource selectedSource;
    private double offset = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        // Begin fetching locations as early as possible
        // TODO: Fix error not loading on first run
        fetchLocations();

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        LinearLayout popup = findViewById(R.id.popup);

        Toolbar mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);

        FloatingActionButton locator = findViewById(R.id.centerButton);
        locator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                centerOnUser();
            }
        });

        Button see_reviews = findViewById(R.id.see_reviews);
        see_reviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), StoreActivity.class);
                intent.putExtra("name", storeName);
                intent.putExtra("nicename", storeNicename);
                intent.putExtra("json", storeJson);
                view.getContext().startActivity(intent);
            }
        });

        popup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(
                                storeLat + getOffset(),
                                storeLng
                        )) // Sets the new camera position
                        .zoom(17) // Sets the zoom
                        .build(); // Creates a CameraPosition from the builder

                mapboxMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(position), 1000);

                centered = false;
                updateCenterButton();

            }
        });

        container = findViewById(R.id.container);
        constraintSet = new ConstraintSet();
        constraintSet.clone(container);
        constraintSet.clear(R.id.popup, ConstraintSet.BOTTOM);
        constraintSet.connect(R.id.popup, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0);
        constraintSet.applyTo(container);

        Button navigateButton = findViewById(R.id.navigateButton);
        navigateButton.setVisibility(View.GONE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadedStyle != null && !locationsAdded && locations != null) {
                    addClusteredGeoJsonSource(loadedStyle);
                }
            }
        }, 5000);

        FloatingActionButton fuck = findViewById(R.id.nearestButton);
        fuck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(findViewById(R.id.container), "Can you not?", Snackbar.LENGTH_LONG).show();
            }
        });

    }


    // If permissions are given for location then enable location component
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals("android.permission.ACCESS_FINE_LOCATION") && grantResults[i] == 0) {
                if (loadedStyle != null) {
                    enableLocationComponent(loadedStyle);
                    if (!locationsAdded && locations != null) {
                        addClusteredGeoJsonSource(loadedStyle);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (popupVisible) {
            TransitionManager.beginDelayedTransition(container);
            constraintSet.clear(R.id.popup, ConstraintSet.BOTTOM);
            constraintSet.connect(R.id.popup, ConstraintSet.TOP, R.id.container, ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(container);

            popupVisible = false;

            refreshSelected(null);

        } else {
            super.onBackPressed();
        }
    }

    // https://stackoverflow.com/questions/40142331/how-to-request-location-permission-at-runtime
    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation without blocking this thread
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.message_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            enableLocationComponent(loadedStyle);
        }
    }

    public void centerOnUser() {
        if (mapboxMap.getLocationComponent().isLocationComponentActivated()
                && mapboxMap.getLocationComponent().isLocationComponentEnabled()
                && mapboxMap.getLocationComponent().getLastKnownLocation() != null) {

            // Zoom to user location
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(
                            mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude() + getOffset(),
                            mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude()
                    )) // Sets the new camera position
                    .zoom(Math.max(13, mapboxMap.getCameraPosition().zoom)) // Sets the zoom
                    .tilt(0)
                    .bearing(0)
                    .build(); // Creates a CameraPosition from the builder

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 1000);

            centered = true;
            updateCenterButton();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.styles.
        // int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    // Add the mapView lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void fetchLocations() {

        int cacheSize = 10 * 1024 * 1024; // 10MB

        File httpCacheDirectory = new File(this.getCacheDir(), "http-cache");
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor())
                .cache(cache)
                .build();

        String url = "https://nuggetwatch.co.nz/locations";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    final String myResponse = response.body().string();

                    MapActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!locationsAdded) {
                                locations = myResponse;
                            }
                        }
                    });
                }
            }
        });
    }
    private void addClusteredGeoJsonSource(@NonNull Style loadedMapStyle) {

        if (locationsAdded) {
            return;
        }
        locationsAdded = true;

        TransitionManager.beginDelayedTransition(mapView);

        // Add a new source from the GeoJSON data and set the 'cluster' option to true.
        try {
            source = new GeoJsonSource("locations",
                    locations,
                    new GeoJsonOptions()
                            .withCluster(true)
                            .withClusterMaxZoom(14)
                            .withClusterRadius(50)
            );
            loadedMapStyle.addSource(source);
        } catch (Exception ignored) {
            Toast.makeText(this, "Locations couldn't be added :(", Toast.LENGTH_LONG).show();
        }


        // Use the locations GeoJSON source to create three layers: One layer for each cluster category.
        // Each point range gets a different fill color.
        int[][] layers = new int[][] {
                new int[] {1, ContextCompat.getColor(this, R.color.colorAccent)},
                new int[] {0, ContextCompat.getColor(this, R.color.colorAccent)}
        };

        //Creating a marker layer for single data points
        SymbolLayer unclustered = new SymbolLayer("unclustered-points", "locations");

        unclustered.setProperties(
                iconImage("marker"),
                iconAllowOverlap(true),
                iconSize(0.9f)
        );
        loadedMapStyle.addLayer(unclustered);

        for (int i = 0; i < layers.length; i++) {
        //Add clusters' circles
            CircleLayer circles = new CircleLayer("cluster-" + i, "locations");
            circles.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(20f)
            );

            Expression pointCount = toNumber(get("point_count"));

            // Add a filter to the cluster layer that hides the circles based on "point_count"
            circles.setFilter(
                    i == 0
                            ? all(has("point_count"),
                            gte(pointCount, literal(layers[i][0]))
                    ) : all(has("point_count"),
                            gte(pointCount, literal(layers[i][0])),
                            lt(pointCount, literal(layers[i - 1][0]))
                    )
            );
            loadedMapStyle.addLayer(circles);
        }

        //Add the count labels
        SymbolLayer count = new SymbolLayer("count", "locations");
        count.setProperties(
                textField(Expression.toString(get("point_count"))),
                textSize(12f),
                textColor(Color.parseColor("#213140")),
                textIgnorePlacement(true),
                textAllowOverlap(true)
        );
        loadedMapStyle.addLayer(count);

        // Create the selected marker layer
        setupSelectedLayer();

    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Get an instance of the component
        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        // Activate with options
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);

        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING);

        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.NORMAL);

        // Zoom to user location
        assert mapboxMap.getLocationComponent().getLastKnownLocation() != null;
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(
                        mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude() + getOffset(),
                        mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude()
                )) // Sets the new camera position
                .zoom(13) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 1000);

        centered = true;
        updateCenterButton();

        Button navigateButton = findViewById(R.id.navigateButton);
        navigateButton.setVisibility(View.VISIBLE);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this, CompassActivity.class);
                intent.putExtra("latitude", String.valueOf(storeLat));
                intent.putExtra("longitude", String.valueOf(storeLng));
                intent.putExtra("name", storeName);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        // Convert LatLng coordinates to screen pixel and only query the rendered features.
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);

        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "unclustered-points");

        // Get the first feature within the list if one exist
        if (features.size() > 0) {
            Feature feature = features.get(0);

            // Ensure the feature has properties defined
            if (feature.properties() != null) {

                Point marker = null;
                if (feature.geometry() != null) {
                    marker = Point.fromJson(feature.geometry().toJson());
                }

                if (feature.properties().has("cluster_id")) {

                    int zoom = source.getClusterExpansionZoom(feature);

                    double difference = (mapboxMap.getCameraPosition().zoom + (17 - mapboxMap.getCameraPosition().zoom) / 4) - mapboxMap.getCameraPosition().zoom;
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(
                                    marker.latitude() + getOffset(),
                                    marker.longitude()
                            )) // Sets the new camera position
                            .zoom(zoom + 0.1) // Sets the zoom
                            .build(); // Creates a CameraPosition from the builder

                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), (int) (300 + 200 * difference));
                } else {

                    double difference = (mapboxMap.getCameraPosition().zoom + (17 - mapboxMap.getCameraPosition().zoom) / 4) - mapboxMap.getCameraPosition().zoom;

                    popupVisible = true;

                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(
                                    marker.latitude() + getOffset(),
                                    marker.longitude()
                            )) // Sets the new camera position
                            .zoom(17) // Sets the zoom
                            .build(); // Creates a CameraPosition from the builder

                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), (int) (300 + 200 * difference));

                    TextView name = findViewById(R.id.name);
                    storeName = feature.properties().get("chain").toString().replace("\"", "");
                    storeNicename = feature.properties().get("nicename").toString().replace("\"", "");
                    storeJson = feature.toJson();
                    storeLat = marker.latitude();
                    storeLng = marker.longitude();
                    name.setText(storeName);

                    if (mapboxMap.getLocationComponent().isLocationComponentActivated()) {
                        float distanceTo;
                        String unit = "km";

                        if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
                            Location nuggetLoc = new Location("");
                            nuggetLoc.setLatitude(marker.latitude());
                            nuggetLoc.setLongitude(marker.longitude());

                            Location currentLoc = new Location("");
                            currentLoc.setLatitude(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude());
                            currentLoc.setLongitude(mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());

                            distanceTo = nuggetLoc.distanceTo(currentLoc) / 1000;

                            if (distanceTo < 1) {
                                unit = "m";
                                distanceTo = Math.round(distanceTo * 1000);
                            }

                            TextView distance = findViewById(R.id.distance);
                            distance.setText(String.format(
                                    this.getResources().getString(R.string.map_distance),
                                    distanceTo, unit));
                        }
                    } else {
                        TextView distance = findViewById(R.id.distance);
                        distance.setVisibility(View.GONE);
                    }

                    TransitionManager.beginDelayedTransition(container);

                    refreshSelected(feature.geometry());

                    constraintSet.clear(R.id.popup, ConstraintSet.TOP);
                    constraintSet.connect(R.id.popup, ConstraintSet.BOTTOM, R.id.container, ConstraintSet.BOTTOM, 0);
                    constraintSet.applyTo(container);

                    RatingBar rating = findViewById(R.id.ratingBar);
                    rating.setRating(feature.properties().get("rating").getAsInt());
                    TextView count = findViewById(R.id.count);
                    count.setText(String.format(this.getResources().getString(
                            R.string.map_rating_count),
                            feature.properties().get("rating_count").getAsInt()));
                }
            }
        } else {
            TransitionManager.beginDelayedTransition(container);

            refreshSelected(null);

            constraintSet.clear(R.id.popup, ConstraintSet.BOTTOM);
            constraintSet.connect(R.id.popup, ConstraintSet.TOP, R.id.container, ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(container);

            popupVisible = false;

        }

        return true;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap map) {

        mapboxMap = map;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/nathanhollows/ck03c12ce0ymt1cpgsg8vv5rw"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                style.addImage(
                        "marker",
                        BitmapFactory.decodeResource(getResources(), R.drawable.marker)
                );

                style.addImage(
                        "marker_selected",
                        BitmapFactory.decodeResource(getResources(), R.drawable.marker_selected)
                );

                // Check for location permissions
                // Should it succeed the location component will be enabled
                loadedStyle = style;
                checkLocationPermission();

                if (locations != null && !locationsAdded) {
                    addClusteredGeoJsonSource(loadedStyle);
                }

            }
        });

        mapboxMap.addOnMapClickListener(MapActivity.this);

        mapboxMap.addOnMoveListener(new MapboxMap.OnMoveListener() {
            @Override
            public void onMoveBegin(@NonNull MoveGestureDetector detector) {
                centered = false;
                updateCenterButton();

            }

            @Override
            public void onMove(@NonNull MoveGestureDetector detector) {

            }

            @Override
            public void onMoveEnd(@NonNull MoveGestureDetector detector) {
            }
        });

        mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    public void updateCenterButton() {
        FloatingActionButton centerButton = findViewById(R.id.centerButton);

        if (centered) {
            centerButton.setImageResource(R.drawable.ic_my_location_black_24dp);
        } else {
            centerButton.setImageResource(R.drawable.ic_location_searching_white_24dp);
        }
    }

    private void setupSelectedLayer() {
        // Create a source for the selected store
        selectedSource = new GeoJsonSource("selected");
        loadedStyle.addSource(selectedSource);
        // Create a marker layer for the selected store
        SymbolLayer selected = new SymbolLayer("selected", "selected");
        selected.setProperties(
                iconImage("marker_selected"),
                iconAllowOverlap(false),
                iconSize(0.9f)
        );
        loadedStyle.addLayer(selected);
    }

    private void refreshSelected(Geometry store) {
        Layer selected = loadedStyle.getLayer("selected");
        if (selected == null) {
            return;
        }
        if (store != null) {
            selectedSource.setGeoJson(store);
            selected.setProperties(
                    visibility(Property.VISIBLE));
        } else {
            selected.setProperties(
                    visibility(Property.NONE));
        }

    }

    private double getOffset() {
        if (offset == -1) {
            // Calculate the view offset from the popup
            offset = (mapView.getHeight() - findViewById(R.id.popup).getHeight()) - mapView.getHeight() / 2f;
            offset = - (offset * 0.0457 / 1000 / 6378) * (180 / Math.PI);
        }

        if (popupVisible) {
            return offset;
        } else {
            return 0;
        }
    }
}