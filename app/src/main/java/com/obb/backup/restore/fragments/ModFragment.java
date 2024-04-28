package com.obb.backup.restore.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.obb.backup.restore.Interface.OnProductClickListener;
import com.obb.backup.restore.activity.DetailActivity;
import com.obb.backup.restore.adapter.ModsAdapter;
import com.obb.backup.restore.databinding.FragmentModsBinding;
import com.obb.backup.restore.model.Product;

public class ModFragment extends Fragment  implements OnProductClickListener {

    private DatabaseReference productsRef;
    private FragmentModsBinding binding;
    private ModsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentModsBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        // Initialize Firebase Database reference
        productsRef = FirebaseDatabase.getInstance().getReference("products");

        // Set up RecyclerView
        RecyclerView recyclerView = binding.productRecyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ModsAdapter(this);
        recyclerView.setAdapter(adapter);


        // Attach a listener to retrieve and display data
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear previous data
                adapter.clearProducts();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String productId = productSnapshot.getKey();
                    String name = productSnapshot.child("name").getValue(String.class);
                    String version = productSnapshot.child("version").getValue(String.class);
                    String size = productSnapshot.child("size").getValue(String.class);
                    String developer = productSnapshot.child("developer").getValue(String.class);
                    String status = productSnapshot.child("status").getValue(String.class);
                    String imageUrl = productSnapshot.child("image_url").getValue(String.class);
                    String description = productSnapshot.child("description").getValue(String.class);
                    String download_url = productSnapshot.child("download_url").getValue(String.class);
                    String icon_url = productSnapshot.child("icon_url").getValue(String.class);
                    int timer = productSnapshot.child("timer").getValue(Integer.class);

                    Product product = new Product(productId, name, version, size, developer, status, imageUrl,description,download_url,timer,icon_url);
                    adapter.addProduct(product);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return rootView;
    }

    @Override
    public void onProductClick(Product product) {
        if (getActivity() != null) {
            openDetailActivity(getActivity(), product);
        }
    }
    private void openDetailActivity(Context context, Product product) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("product", product);
        context.startActivity(intent);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
