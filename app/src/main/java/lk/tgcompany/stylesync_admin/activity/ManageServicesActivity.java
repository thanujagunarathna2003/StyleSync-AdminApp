package lk.tgcompany.stylesync_admin.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.tgcompany.stylesync_admin.R;
import lk.tgcompany.stylesync_admin.adapter.ServiceAdapter;
import lk.tgcompany.stylesync_admin.model.ServiceModel;

public class ManageServicesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ServiceAdapter adapter;
    private List<ServiceModel> serviceList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_services);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind UI components
        recyclerView = findViewById(R.id.recyclerViewServices);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        serviceList = new ArrayList<>();
        adapter = new ServiceAdapter(serviceList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load data
        loadServices();
    }

    private void loadServices() {
        progressBar.setVisibility(View.VISIBLE);

        // Simple fetch without orderBy to avoid Index errors for now
        db.collection("Services")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    serviceList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Convert document to ServiceModel
                            ServiceModel service = document.toObject(ServiceModel.class);

                            // Manually setting document ID as serviceId if needed
                            service.setServiceId(document.getId());

                            serviceList.add(service);
                        } catch (Exception e) {
                            Log.e("FirestoreError", "Mapping Error: " + e.getMessage());
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (serviceList.isEmpty()) {
                        Toast.makeText(this, "No services found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Fetch Error: ", e);
                });
    }
}