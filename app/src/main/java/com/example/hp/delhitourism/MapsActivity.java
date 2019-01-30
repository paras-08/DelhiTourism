package com.example.hp.delhitourism;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.hp.delhitourism.Adapters.HorizontalViewAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int RequestLocation = 100;
    private GoogleMap mMap;
    ArrayList<TouristPlace> touristPlaces;
    ArrayList<Marker> markers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("bundle");
        touristPlaces = (ArrayList<TouristPlace>) args.getSerializable("tourist places");

        initialiseRecyclerView();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    void initialiseRecyclerView() {

        RecyclerView horizontalRecyclerView = findViewById(R.id.mapRecyclerView);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        horizontalRecyclerView.setLayoutManager(layoutManager);
        horizontalRecyclerView.setAdapter(new MapRecyclerViewAdapter(touristPlaces, this));

        final SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(horizontalRecyclerView);

        horizontalRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    View centerView = snapHelper.findSnapView(layoutManager);
                    if (centerView != null) {
                        int pos = layoutManager.getPosition(centerView);
                        updateMapPosition(pos);
                    }
                }
            }

            private void updateMapPosition(int pos) {
                if(mMap != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(pos).getPosition(), 14));
                    markers.get(pos).showInfoWindow();
                }
            }
        });
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        showCurrentLocationButton();

        System.out.println("bvdhbvhdbv");
        markers = new ArrayList<>();

        for(int i = 0; i < touristPlaces.size(); i++) {
            LatLng marker = touristPlaces.get(i).getCoordinates();
            System.out.println("Added Marker");
            Marker locationMarker = mMap.addMarker(new MarkerOptions().position(marker).title(touristPlaces.get(i).getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            markers.add(locationMarker);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 14));
        markers.get(0).showInfoWindow();
    }

    private void showCurrentLocationButton() {
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, RequestLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == RequestLocation) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }









    class MapRecyclerViewAdapter extends RecyclerView.Adapter<MapRecyclerViewAdapter.MapRecyclerViewHolder> {

        private ArrayList<TouristPlace> touristPlaces;
        private Context context;

        public MapRecyclerViewAdapter(ArrayList<TouristPlace> touristPlaces, Context context) {
            this.touristPlaces = touristPlaces;
            this.context = context.getApplicationContext();
        }

        @NonNull
        @Override
        public MapRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflator = LayoutInflater.from(viewGroup.getContext());
            View view = inflator.inflate(R.layout.map_card_view, viewGroup, false);
            return new MapRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MapRecyclerViewHolder holder, int i) {
            TouristPlace touristPlace = touristPlaces.get(i);
            holder.placeName.setText(touristPlace.getName());
            holder.placeRating.setNumStars(touristPlace.getStarRating());
            holder.placeCategory.setText(touristPlace.getCategory());
            holder.placeDescription.setText(touristPlace.getDescription());
            holder.placeImage.setImageBitmap(touristPlace.getImage(context));
        }

        @Override
        public int getItemCount() {
            return touristPlaces.size();
        }

        public class MapRecyclerViewHolder extends RecyclerView.ViewHolder {

            ImageView placeImage;
            TextView placeName, placeCategory, placeDescription;
            RatingBar placeRating;

            public MapRecyclerViewHolder(@NonNull View itemView) {
                super(itemView);
                placeImage = itemView.findViewById(R.id.map_place_image_view);
                placeName = itemView.findViewById(R.id.map_place_name);
                placeRating = itemView.findViewById(R.id.map_place_rating);
                placeCategory = itemView.findViewById(R.id.map_place_category);
                placeDescription = itemView.findViewById(R.id.map_place_description);
            }
        }

    }


}
