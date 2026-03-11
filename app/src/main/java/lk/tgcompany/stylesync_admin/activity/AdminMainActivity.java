package lk.tgcompany.stylesync_admin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lk.tgcompany.stylesync_admin.R;

public class AdminMainActivity extends AppCompatActivity {

    private TextView txtTotalEarnings, txtTotalBookings, txtTodayBookings;
    private TextView lblDone, lblPending, lblRescheduled;
    private ProgressBar pbDone, pbPending, pbRescheduled;
    private FirebaseFirestore db;
    private MaterialCardView cardViewBookings, cardManageServices, cardTodayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        db = FirebaseFirestore.getInstance();

        // 1. Bind UI Components
        txtTotalEarnings = findViewById(R.id.txtTotalEarnings);
        txtTotalBookings = findViewById(R.id.txtTotalBookings);
        txtTodayBookings = findViewById(R.id.txtTodayBookings);

        lblDone = findViewById(R.id.lblDone);
        lblPending = findViewById(R.id.lblPending);
        lblRescheduled = findViewById(R.id.lblRescheduled);

        pbDone = findViewById(R.id.pbDone);
        pbPending = findViewById(R.id.pbPending);
        pbRescheduled = findViewById(R.id.pbRescheduled);

        cardViewBookings = findViewById(R.id.cardViewBookings);
        cardManageServices = findViewById(R.id.cardManageServices);
        cardTodayList = findViewById(R.id.cardTodayList); // Button to view only today's list

        //view only today booking
        cardTodayList.setOnClickListener(v -> {
            // Navigate specifically to the new TodayBookingsActivity
            Intent intent = new Intent(this, TodayBookingsActivity.class);
            startActivity(intent);
        });

        // Logout Button Logic
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, AdminLoginActivity.class));
            finish();
        });

        // View Full History (No Filter)
        cardViewBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllBookingsActivity.class);
            intent.putExtra("FILTER_TODAY", false); // Show all bookings
            startActivity(intent);
        });

        // Manage Salon Services
        cardManageServices.setOnClickListener(v -> {
            // Placeholder for ManageServicesActivity
             startActivity(new Intent(this, AddServiceActivity.class));
        });

        calculateStats();
    }

    private void calculateStats() {
        // Get Current Date (Format: d/M/yyyy)
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        db.collection("Bookings").get().addOnSuccessListener(queryDocumentSnapshots -> {
            double income = 0;
            int total = 0, todayCount = 0, done = 0, pending = 0, res = 0;

            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                total++;
                String bookingDate = doc.getString("date");
                String status = doc.getString("status");
                Double price = doc.getDouble("price");

                // Calculate appointments for today
                if (today.equals(bookingDate)) {
                    todayCount++;
                }

                // Categorize based on Status
                if (status != null) {
                    if (status.equalsIgnoreCase("Done") || status.equalsIgnoreCase("Completed")) {
                        done++;
                        // Only add to income if booking is Completed/Done
                        if (price != null) income += price;
                    } else if (status.equalsIgnoreCase("Rescheduled")) {
                        res++;
                    } else {
                        pending++;
                    }
                }
            }

            // Update UI with calculated data
            txtTotalEarnings.setText(String.format(Locale.getDefault(), "Rs. %.2f", income));
            txtTotalBookings.setText(String.valueOf(total));
            txtTodayBookings.setText(String.valueOf(todayCount));

            // Setup Progress Bar values
            if (total > 0) {
                pbDone.setMax(total); pbDone.setProgress(done);
                pbPending.setMax(total); pbPending.setProgress(pending);
                pbRescheduled.setMax(total); pbRescheduled.setProgress(res);

                lblDone.setText("Completed: " + done);
                lblPending.setText("Pending: " + pending);
                lblRescheduled.setText("Rescheduled: " + res);
            } else {
                // Reset bars if no data found
                pbDone.setProgress(0);
                pbPending.setProgress(0);
                pbRescheduled.setProgress(0);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dashboard data when returning from other activities
        calculateStats();
    }
}