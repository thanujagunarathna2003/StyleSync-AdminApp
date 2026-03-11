package lk.tgcompany.stylesync_admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import lk.tgcompany.stylesync_admin.R;

public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText adminEmail, adminPassword;
    private Button btnAdminLogin;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        db = FirebaseFirestore.getInstance();

        adminEmail = findViewById(R.id.adminEmail);
        adminPassword = findViewById(R.id.adminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnAdminLogin.setOnClickListener(v -> loginAdmin());
    }

    private void loginAdmin() {
//        String email = adminEmail.getText().toString().trim();
//        String password = adminPassword.getText().toString().trim();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(this, "Please enter credentials", Toast.LENGTH_SHORT).show();
//            return;
//        }

        String emailInput = adminEmail.getText().toString().trim();
        String passwordInput = adminPassword.getText().toString().trim();

// Search for the admin document using the email field
        db.collection("Admins")
                .whereEqualTo("email", emailInput)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {

                        // Get the first document that matches the email
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String dbPassword = document.getString("password");

                        // Compare the password from Firestore with the user input
                        if (dbPassword != null && dbPassword.equals(passwordInput)) {
                            // Login successful
                            String adminId = document.getId(); // Get the document ID (e.g., UQXla3...)

                            // Update the FCM token for this specific admin
                            updateAdminToken(adminId);

                            Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Invalid Password!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Admin not found with this email!", Toast.LENGTH_SHORT).show();
                    }
                });




//        db.collection("Admins").document().get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        DocumentSnapshot document = task.getResult();
//                        if (document.exists()) {
//                            String dbPassword = document.getString("password");
//
//                            if (dbPassword != null && dbPassword.equals(password)) {
//
//                                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
//                                Intent intent = new Intent(AdminLoginActivity.this, AdminMainActivity.class);
//                                startActivity(intent);
//                                finish();
//                            } else {
//                                Toast.makeText(this, "Invalid Password!", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(this, "Admin not found with this email!", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

    private void updateAdminToken(String adminId) {
        // Generate a new FCM registration token for this device
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            // Update the 'fcmToken' field in the admin's document
            db.collection("Admins").document(adminId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM_LOG", "Admin Token updated: " + token))
                    .addOnFailureListener(e -> Log.e("FCM_LOG", "Error updating token", e));
        });
    }
}