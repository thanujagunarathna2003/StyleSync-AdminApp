package lk.tgcompany.stylesync_admin.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import lk.tgcompany.stylesync_admin.R;

public class AddServiceActivity extends AppCompatActivity {

    private TextInputEditText etServiceName, etServicePrice;
    private MaterialButton btnSave;
    private ProgressBar progressBar;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        // 1. Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // 2. Bind UI Components from your XML
        etServiceName = findViewById(R.id.etServiceName);
        etServicePrice = findViewById(R.id.etServicePrice);
        btnSave = findViewById(R.id.btnAddService);
        progressBar = findViewById(R.id.progressBar);

        // 3. Set click listener for the Save button
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveServiceToFirestore();
            }
        });
    }

    private void saveServiceToFirestore() {
        String name = etServiceName.getText().toString().trim();
        String priceText = etServicePrice.getText().toString().trim();

        // 4. Basic Validation: Ensure fields are not empty
        if (name.isEmpty()) {
            etServiceName.setError("Service name is required");
            return;
        }

        if (priceText.isEmpty()) {
            etServicePrice.setError("Price is required");
            return;
        }

        // 5. UI Updates: Show progress and disable button to prevent double clicks
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        btnSave.setText("Saving...");

        // 6. Create a Map for the service data
        Map<String, Object> service = new HashMap<>();
        service.put("name", name);
        service.put("price", Double.parseDouble(priceText));

        // We set a placeholder or null for imageUrl since we skipped image upload for now
        service.put("imageUrl", "none");
        service.put("timestamp", System.currentTimeMillis());

        // 7. Save the data to Firestore "Services" collection
        db.collection("Services")
                .add(service)
                .addOnSuccessListener(documentReference -> {
                    // Success logic
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddServiceActivity.this, "Service successfully saved!", Toast.LENGTH_SHORT).show();
                    finish(); // Close this activity and go back to the previous screen
                })
                .addOnFailureListener(e -> {
                    // Failure logic
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    btnSave.setText("Confirm & Save");
                    Toast.makeText(AddServiceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}