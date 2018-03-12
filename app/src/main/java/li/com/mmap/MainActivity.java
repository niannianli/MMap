package li.com.mmap;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.List;

import li.com.mmap.POJO.Example;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static li.com.mmap.DividerItemDecoration.HORIZONTAL_LIST;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnInfoWindowClickListener {

    Toolbar toolbar;
    String keyword;

    RecyclerView rv1;
    FloatingActionButton fab;

    GoogleMap mMap;

    double latitude;
    double longitude;
    private int PROXIMITY_RADIUS = 10000;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;

    List<Row> rowList;

    ClusterManager<Row> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureToolbar();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //show error dialog if Google Play Services not available
        if (!googleServicesAvailable()) {
            Log.d("onCreate", "Google Play Services not available. Ending Test case.");
            finish();
        } else {
            Log.d("onCreate", "Google Play Services available. Continuing.");
        }

        //save dumb data for test
        saveRecord("test1", new LatLng(39.8097343, -98.5556199 ), "test");
        saveRecord("test2", new LatLng(49.8097343, -98.5556199 ), "test");
        saveRecord("test3", new LatLng(59.8097343, -98.5556199 ), "test");
        saveRecord("test4", new LatLng(89.8097343, -98.5556199 ), "test");

        initMap();

        rv1 = (RecyclerView) findViewById(R.id.id_recycleView1);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        rv1.setLayoutManager(horizontalLayoutManager);

        rv1.addItemDecoration(new DividerItemDecoration(this, HORIZONTAL_LIST));
        rv1.setAdapter(new RV1Adapter(rowList, this));

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentRV2 newFragment = new FragmentRV2();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.map2, newFragment).commit();
            }
        });

    }

    private void configureToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mMap != null) {

            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    //set info_window contents to cover original marker contents
                    View v = setInfoWindowContents(marker);
                    return v;
                }
            });
        }

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        //initial location
        //goToLocationZoom(39.8097343, -98.5556199, 3);

        rowList = getDBRecords();

        if (isNetworkConnectionAvailable()) {
            build_retrofit_and_get_response("school");
        } else {
            //data already saved in db
            for (Row row : rowList) {

               // clusterMarkers();

                MarkerOptions options = new MarkerOptions()
                        //name
                        .title(row.getName())
                        //image
                        //  .icon(BitmapDescriptorFactory.fromResource(R.raw.food))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        //geo
                        .position(row.getGeo())
                        //address
                        .snippet(row.getAddress());

                //add the marker
                mMap.addMarker(options);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(row.getGeo()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        Intent intent = new Intent(this, SingleItemDetailsActivity.class);

        intent.putExtra("imageView3", R.raw.food);
        intent.putExtra("nameTextView3", marker.getTitle());
        intent.putExtra("geoTextView3", marker.getPosition().toString());
        intent.putExtra("addressTextView3", marker.getSnippet().toString());

        startActivity(intent);

    }

    private boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) return false;
        NetworkInfo.State network = info.getState();
        return (network == NetworkInfo.State.CONNECTED || network == NetworkInfo.State.CONNECTING);
    }

    private void goToLocationZoom(double lat, double lng, int zoom) {
        LatLng ll = new LatLng(lat, lng);

        MarkerOptions options = new MarkerOptions()
                //name
                .title("test")
                //image
                //  .icon(BitmapDescriptorFactory.fromResource(R.raw.food))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                //geo
                .position(new LatLng(lat, lng))
                //address
                .snippet("test");

        //add the marker
        mMap.addMarker(options);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    private void build_retrofit_and_get_response(String type) {

        String url = "https://maps.googleapis.com/maps/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitMaps service = retrofit.create(RetrofitMaps.class);

        Call<Example> call = service.getNearbyPlaces(type, latitude + "," + longitude, PROXIMITY_RADIUS);

        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {

                try {
                    mMap.clear();

                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {

                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        LatLng geo = new LatLng(lat, lng);

                        String placeName = response.body().getResults().get(i).getName();
                        String vicinity = response.body().getResults().get(i).getVicinity();
                        String name = placeName + " : " + vicinity;

                        Geocoder gc = new Geocoder(MainActivity.this);
                        String address = gc.getFromLocation(lat, lng, 1).get(0).toString();

                        MarkerOptions markerOptions = new MarkerOptions();

                        // Position of Marker on Map
                        markerOptions.position(geo);
                        // Adding Title to the Marker
                        markerOptions.title(name);
                        markerOptions.snippet(address);
                        // Adding colour to the marker
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                        // Adding Marker to the Camera.
                        Marker m = mMap.addMarker(markerOptions);

                        // move map camera
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(geo));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

                        saveRecord(name, geo, address);

                    }

                    clusterMarkers();

                } catch (Exception e) {
                    Log.d("onResponse", "There is an error");
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d("onFailure", t.toString());
            }
        });

    }

    private List<Row> getDBRecords() {

        SQLiteDatabaseManager sQLiteDatabaseManager = new SQLiteDatabaseManager(this);

        List<Row> rowList = sQLiteDatabaseManager.getData();

        return rowList;

    }

    private void saveRecord(String name, LatLng geo, String address) {

        SQLiteDatabaseManager sQLiteDatabaseManager = new SQLiteDatabaseManager(this);

        sQLiteDatabaseManager.insertData(name, Double.toString(geo.latitude), Double.toString(geo.longitude), address.toString());
    }

    private View setInfoWindowContents(Marker marker) {
        View v = getLayoutInflater().inflate(R.layout.info_window, null);

        TextView textView1 = (TextView) v.findViewById(R.id.textViewName);
        TextView textView2 = (TextView) v.findViewById(R.id.textViewGeo);
        TextView textView3 = (TextView) v.findViewById(R.id.textViewAddress);

        String textView1String = "Location Name: " + marker.getTitle();
        textView1.setText(textView1String);

        LatLng ll = marker.getPosition();

        String textView2String = "Location Geo: lat/lng: \n" + "(" + ll.latitude + ","
                + ll.longitude + ")";
        textView2.setText(textView2String);

        String textView3String = "Location Address: " + marker.getSnippet();
        textView3.setText(textView3String);

        return v;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem search_item = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) search_item.getActionView();
        searchView.setFocusable(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, "search view", Toast.LENGTH_LONG).show();
                //     Log.d("onResponse", "There is search view");
                keyword = query.toUpperCase();
                if(keyword.equals("FOOD")) {
                    build_retrofit_and_get_response("restaurant");
                    return false;
                }else if(keyword.equals("SCHOOL")) {
                    build_retrofit_and_get_response("school");
                    return false;
                }else{
                    return false;
                }
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        return false;
    }

    private void clusterMarkers() {
        mClusterManager = new ClusterManager<>(this, mMap);

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);

        for(int i=0; i<6; i++){
    //       mClusterManager.addItem(rowList.get(i));
         mClusterManager.addItem(new Row(R.raw.food, "test4", new LatLng(-26.187616, 28.079329), "test"));
            mClusterManager.addItem(new Row(R.raw.food, "test5", new LatLng(-36.187616, 28.079329), "test"));
            mClusterManager.addItem(new Row(R.raw.food, "test6", new LatLng(-46.187616, 28.079329), "test"));
        }

/*        RenderClusterInfoWindow renderer = new RenderClusterInfoWindow(this, mMap, mClusterManager);
        mClusterManager.setRenderer(renderer);

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<Row>()
        {
            @Override public boolean onClusterClick(Cluster<Row> cluster) {
                Toast.makeText(MainActivity.this, "Cluster click", Toast.LENGTH_SHORT).show();
                // if true, do not move camera
                return false;
            }
        });
        mClusterManager.setOnClusterItemClickListener(
                new ClusterManager.OnClusterItemClickListener<Row>() {
                    @Override
                    public boolean onClusterItemClick(Row row) {
                        Toast.makeText(MainActivity.this, "Cluster item click", Toast.LENGTH_SHORT).show();
                        // if true, click handling stops here and do not show info view, do not move camera
                        // you can avoid this by calling:
                        // renderer.getMarker(clusterItem).showInfoWindow();
                        return false;
                    }
                });*/

        mClusterManager.cluster();
}

    private class RenderClusterInfoWindow extends DefaultClusterRenderer<Row> {

        RenderClusterInfoWindow(Context context, GoogleMap map, ClusterManager<Row> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onClusterRendered(Cluster<Row> cluster, Marker marker) {
            super.onClusterRendered(cluster, marker);
        }

        @Override
        protected void onBeforeClusterItemRendered(Row item, MarkerOptions markerOptions) {
            markerOptions.title(item.getName());

            super.onBeforeClusterItemRendered(item, markerOptions);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d("onLocationChanged", "entered");

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");

        // Adding colour to the marker
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        // Adding Marker to the Map
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        Log.d("onLocationChanged", String.format("latitude:%.3f longitude:%.3f", latitude, longitude));

        Log.d("onLocationChanged", "Exit");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public class RV1Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        Context context;

        public RV1Adapter(List<Row> rows, Context context) {
            rowList = rows;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv1_item_details, parent, false);
            return new RV1Adapter.ViewHolder(view);

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            RV1Adapter.ViewHolder viewHolder = (RV1Adapter.ViewHolder) holder;
            Row row = rowList.get(position);

            viewHolder.nameTextView1.setText(row.getName());
            viewHolder.geoTextView1.setText(row.getGeo().toString());
            viewHolder.addressTextView1.setText(row.getAddress());
        }

        @Override
        public int getItemCount() {
            return rowList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            CardView cv1;
            TextView nameTextView1, geoTextView1, addressTextView1;

            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                cv1 = (CardView) view.findViewById(R.id.cv1);
                nameTextView1 = view.findViewById(R.id.textViewName1);
                geoTextView1 = view.findViewById(R.id.textViewGeo1);
                addressTextView1 = view.findViewById(R.id.textViewAddress1);
            }

            @Override
            public void onClick(View v) {
                int position = getAdapterPosition();
                Row row = rowList.get(position);

                MarkerOptions options = new MarkerOptions()
                        //name
                        .title(row.getName())
                        //image
                        //  .icon(BitmapDescriptorFactory.fromResource(R.raw.food))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        //geo
                        .position(row.getGeo())
                        //address
                        .snippet(row.getAddress());

                //add the marker
                mMap.addMarker(options);

                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(row.getGeo(), 11);
                mMap.moveCamera(update);
            }
        }
    }
}


