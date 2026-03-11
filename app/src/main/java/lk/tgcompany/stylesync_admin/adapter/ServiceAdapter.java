package lk.tgcompany.stylesync_admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.tgcompany.stylesync_admin.R;
import lk.tgcompany.stylesync_admin.model.ServiceModel;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {

    private List<ServiceModel> serviceList;

    // Constructor to pass the service list
    public ServiceAdapter(List<ServiceModel> serviceList) {
        this.serviceList = serviceList;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating the custom item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        // Getting service data for the specific position
        ServiceModel service = serviceList.get(position);

        // Setting data to UI components
        holder.tvName.setText(service.getName());
        holder.tvPrice.setText("LKR " + service.getPrice());
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    // ViewHolder class to hold UI references
    public static class ServiceViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvServiceName);
            tvPrice = itemView.findViewById(R.id.tvServicePrice);
        }
    }
}