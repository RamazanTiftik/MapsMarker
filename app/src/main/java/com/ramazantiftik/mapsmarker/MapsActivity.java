package com.ramazantiftik.mapsmarker;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.fragment.app.FragmentContainerView;
import androidx.room.Room;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.ramazantiftik.mapsmarker.databinding.ActivityMapsBinding;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    LocationManager locationManager;
    LocationListener locationListener;
    ActivityResultLauncher<String> permissionLauncher;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double markerLatitude;
    Double markerLongitude;
    private CompositeDisposable compositeDisposable=new CompositeDisposable();
    Model selectedModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.saveButton.setEnabled(false);
        db= Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places")
                //.allowMainThreadQueries() -->performance loss
                .build();
        placeDao=db.placeDao();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        registerLauncher();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        Intent intent=getIntent();
        String info=intent.getStringExtra("info");

        if(info.equals("new")){
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);

            try {
                locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(@NonNull Location location) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                };

                if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Snackbar.make(binding.getRoot(), "Permission needed for maps", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //request permisson
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                            }
                        }).show();
                    } else {
                        //request permisson
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                } else {
                    //permisson granted
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, locationListener);
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastLocation != null) {
                        LatLng userLastLoction = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLoction, 15));
                    }
                    mMap.setMyLocationEnabled(true);
                }
            }
            catch (Exception ex){
                Toast.makeText(getApplicationContext(),"Please open your location!",Toast.LENGTH_LONG);
            }

        }
        else{

            mMap.clear();
            selectedModel=(Model)intent.getSerializableExtra("place");
            LatLng latLng=new LatLng(selectedModel.latitude,selectedModel.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedModel.placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            binding.placeName.setText(selectedModel.placeName);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

        }



    }

    private void registerLauncher(){
        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //permission granted
                    if(ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,10,locationListener);
                        Location lastLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(lastLocation!=null){
                            LatLng userLastLoction=new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLastLoction,15));
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    //permisson denied
                    Toast.makeText(MapsActivity.this,"Permission Needed!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        markerLongitude=latLng.longitude;
        markerLatitude=latLng.latitude;
        binding.saveButton.setEnabled(true);
    }

    public void handleResponse(){
        Intent intent=new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void save(View view){
        //threading --> Main (UI), Default (CPU Intensive), IO (Network, database)
        //placeDao.insert(model).subscribeOn(Schedulers.io()).subscribe();
        Model model=new Model(binding.placeName.getText().toString(),markerLatitude,markerLongitude);
        compositeDisposable.add(placeDao.insert(model)
                .subscribeOn(Schedulers.io()) // place of operation
                .observeOn(AndroidSchedulers.mainThread()) // shown place
                .subscribe(MapsActivity.this::handleResponse));
    }

    public void delete(View view){

        compositeDisposable.add(placeDao.delete(selectedModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handleResponse));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}