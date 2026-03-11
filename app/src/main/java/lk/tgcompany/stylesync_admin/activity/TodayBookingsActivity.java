package lk.tgcompany.stylesync_admin.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.tgcompany.stylesync_admin.R;
import lk.tgcompany.stylesync_admin.adapter.AdminBookingAdapter;
import lk.tgcompany.stylesync_admin.model.BookingModel;

public class TodayBookingsActivity extends AppCompatActivity {

    private RecyclerView rvTodayBookings;
    private FirebaseFirestore db;
    private List<BookingModel> todayList;
    private AdminBookingAdapter adapter;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today_bookings);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Bind RecyclerView and set layout manager
        rvTodayBookings = findViewById(R.id.rvTodayBookings);
        rvTodayBookings.setLayoutManager(new LinearLayoutManager(this));

        loadTodayBookings();
    }

    private void loadTodayBookings() {

        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());

        db.collection("Bookings")
                .whereEqualTo("date", currentDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todayList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        BookingModel model = doc.toObject(BookingModel.class);
                        if (model != null) {

                            model.setDocumentId(doc.getId());
                            todayList.add(model);
                        }
                    }


                    Collections.sort(todayList, (b1, b2) -> {
                        String t1 = (b1.getTime() != null) ? b1.getTime() : "00:00";
                        String t2 = (b2.getTime() != null) ? b2.getTime() : "00:00";
                        return t1.compareTo(t2);
                    });

                    // UI update
                    if (todayList.isEmpty()) {
                        Toast.makeText(this, "Today is: " + currentDate + " - No bookings!", Toast.LENGTH_SHORT).show();
                    }

                    setupAdapter();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error: " + e.getMessage());
                    Toast.makeText(this, "Check connection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

//    private void loadTodayBookings() {
//        // Get current system date (Format: dd/MM/yyyy)
//        String currentDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
//
//        db.collection("Bookings")
//                .whereEqualTo("date", currentDate)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    todayList = new ArrayList<>();
//                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
//                        BookingModel model = doc.toObject(BookingModel.class);
//                        if (model != null) {
//                            model.setDocumentId(doc.getId());
//                            todayList.add(model);
//                        }
//                    }
//
//                    // 1. Sorting the list by time (Earliest time at the top)
//                    Collections.sort(todayList, new Comparator<BookingModel>() {
//                        @Override
//                        public int compare(BookingModel b1, BookingModel b2) {
//                            // Handle null time values to avoid crashes
//                            String time1 = (b1.getTime() != null) ? b1.getTime() : "00:00";
//                            String time2 = (b2.getTime() != null) ? b2.getTime() : "00:00";
//
//                            // Ascending order: 08:00 will come before 10:00
//                            return time1.compareTo(time2);
//                        }
//                    });
//
//                    // 3. Setup RecyclerView Adapter
//                    if (todayList.isEmpty()) {
//                        Toast.makeText(this, "No appointments for today!", Toast.LENGTH_SHORT).show();
//                    }
//                    setupAdapter();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    private void setupAdapter() {
        adapter = new AdminBookingAdapter(todayList, this, model -> {
            String[] options = {"Mark as Done", "Reschedule", "Delete"};
            new AlertDialog.Builder(this)
                    .setTitle("Manage Appointment")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) updateStatus(model.getDocumentId(), "Done", model.getUserId());
                        else if (which == 1) showDatePicker(model);
                        else if (which == 2) deleteBooking(model.getDocumentId());
                    }).show();
        });
        rvTodayBookings.setAdapter(adapter);
    }

// --- RESCHEDULE FLOW ---

    private void showDatePicker(BookingModel model) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String newDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            showStartTimePicker(model, newDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showStartTimePicker(BookingModel model, String newDate) {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String start = String.format("%02d:%02d", hourOfDay, minute);
            showEndTimePicker(model, newDate, start);
        }, 10, 0, true).show();
    }

    private void showEndTimePicker(BookingModel model, String newDate, String start) {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String end = String.format("%02d:%02d", hourOfDay, minute);
            validateAndReschedule(model, newDate, start, end);
        }, 11, 0, true).show();
    }

    // --- MAIN VALIDATION & CONFLICT CHECK ---
    private void validateAndReschedule(BookingModel model, String date, String newStart, String newEnd) {
        try {
            Date newStartTime = timeFormat.parse(newStart);
            Date newEndTime = timeFormat.parse(newEnd);

            db.collection("Bookings").whereEqualTo("date", date).get()
                    .addOnSuccessListener(query -> {
                        boolean isOverlapping = false;

                        for (QueryDocumentSnapshot doc : query) {
                            if (doc.getId().equals(model.getDocumentId())) continue;

                            String existingTimeRange = doc.getString("time");
                            if (existingTimeRange != null && existingTimeRange.contains(" - ")) {
                                try {
                                    String[] parts = existingTimeRange.split(" - ");
                                    Date existingStart = timeFormat.parse(parts[0]);
                                    Date existingEnd = timeFormat.parse(parts[1]);

                                    // Overlap logic: (StartA < EndB) and (EndA > StartB)
                                    if (newStartTime.before(existingEnd) && newEndTime.after(existingStart)) {
                                        isOverlapping = true;
                                        break;
                                    }
                                } catch (ParseException e) { e.printStackTrace(); }
                            }
                        }

                        if (isOverlapping) {
                            Toast.makeText(this, "Time Conflict! Another booking exists.", Toast.LENGTH_LONG).show();
                        } else {
                            String finalTimeRange = newStart + " - " + newEnd;
                            db.collection("Bookings").document(model.getDocumentId())
                                    .update("date", date, "time", finalTimeRange, "status", "Rescheduled")
                                    .addOnSuccessListener(aVoid -> {
                                        sendNotification(model.getUserId(), "Rescheduled to " + date + " (" + finalTimeRange + ")");
                                        loadTodayBookings(); // Refresh UI
                                        Toast.makeText(this, "Rescheduled Successfully!", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid Format", Toast.LENGTH_SHORT).show();
        }
    }

// --- HELPER METHODS ---

    private void updateStatus(String docId, String status, String userId) {
        db.collection("Bookings").document(docId).update("status", status)
                .addOnSuccessListener(aVoid -> {
                    if (status.equals("Done")) {
                        sendNotification(userId, "Thank you for your attendance!");
                    }
                    loadTodayBookings();
                });
    }

    private void deleteBooking(String docId) {
        db.collection("Bookings").document(docId).delete().addOnSuccessListener(aVoid -> loadTodayBookings());
    }

    private void sendNotification(String userId, String message) {
        if (userId == null) return;

        // Create a unique timestamp for ordering
        long timestamp = System.currentTimeMillis();

        // Also optional: Create a readable date for the user to see easily
        String readableDate = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(new Date());

        Map<String, Object> notif = new HashMap<>();
        notif.put("message", message);
        notif.put("timestamp", timestamp);
        notif.put("date", readableDate); // This helps to show date in User App
        notif.put("status", "unread");  // To show a "New" badge in User App

        db.collection("Users").document(userId)
                .collection("Notifications")
                .add(notif)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Notification", "Notification sent to User: " + userId);
                })
                .addOnFailureListener(e -> {
                    Log.e("Notification", "Error sending notification", e);
                });
    }
}