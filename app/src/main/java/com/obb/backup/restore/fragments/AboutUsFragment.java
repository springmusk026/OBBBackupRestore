package com.obb.backup.restore.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.obb.backup.restore.databinding.AboutUsFragmentBinding;

public class AboutUsFragment extends Fragment {

    private AboutUsFragmentBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using view binding
        binding = AboutUsFragmentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set a click listener for the contact button
        binding.contactButton.setOnClickListener(v -> {
            // Navigate to the contact us screen or take any other action
           // Navigation.findNavController(view).navigate(R.id.action_aboutUsFragment_to_contactUsFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Clear the binding when the view is destroyed
    }
}
