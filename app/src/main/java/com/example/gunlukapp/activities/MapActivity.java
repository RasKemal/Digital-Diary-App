package com.example.gunlukapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gunlukapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (selectedLocation == null)
            Toast.makeText(this, "Lutfen Lokasyon Seçin", Toast.LENGTH_SHORT).show();

        else {
            Intent intent = new Intent();
            intent.putExtra("latitude", selectedLocation.latitude);
            intent.putExtra("longitude", selectedLocation.longitude);
            setResult(RESULT_OK, intent);
            finish();
        }

        return true;
    }

    private void initialize() {
        setContentView(R.layout.activity_map);
        selectedLocation = null;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this::handleMapClick);

        // Default: Istanbul
        LatLng istanbul = new LatLng(41.015137, 28.979530);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(istanbul));
    }

    private void handleMapClick(LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title("Konum"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        selectedLocation = latLng;
    }
}