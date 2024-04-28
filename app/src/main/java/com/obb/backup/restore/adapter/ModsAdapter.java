package com.obb.backup.restore.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.obb.backup.restore.Interface.OnProductClickListener;
import com.obb.backup.restore.R;
import com.obb.backup.restore.model.Product;
import com.obb.backup.restore.databinding.ItemProductBinding;
import java.util.ArrayList;
import java.util.List;

public class ModsAdapter extends RecyclerView.Adapter<ModsAdapter.ProductViewHolder> {

    private List<Product> productList = new ArrayList<>();
    private ItemProductBinding binding;
    private OnProductClickListener onProductClickListener;

    public ModsAdapter(OnProductClickListener onProductClickListener) {
        this.onProductClickListener = onProductClickListener;
    }

    public void addProduct(Product product) {
        productList.add(product);
        notifyItemInserted(productList.size() - 1);
    }

    public void clearProducts() {
        productList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding, onProductClickListener, productList);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private ItemProductBinding binding;
        private OnProductClickListener onProductClickListener;
        private List<Product> productList;

        ProductViewHolder(ItemProductBinding binding, OnProductClickListener onProductClickListener, List<Product> productList) {
            super(binding.getRoot());
            this.binding = binding;
            this.onProductClickListener = onProductClickListener;
            this.productList = productList;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Product product = productList.get(position);
                    if (onProductClickListener != null) {
                        onProductClickListener.onProductClick(product);
                    }
                }
            });
        }

        void bind(Product product) {
            binding.productNameTextView.setText(product.getName());
            binding.productVersionTextView.setText(product.getVersion());
            binding.productSizeTextView.setText(product.getSize());
            binding.productDeveloperTextView.setText(product.getDeveloper());
            binding.productStatusTextView.setText(product.getStatus());

            Glide.with(itemView)
                    .load(product.geticon_url())
                    .optionalCenterCrop()
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.error_image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.productImage);

        }
    }
}
