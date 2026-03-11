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

import lk.tgcompany.stylesync_admin.R;
import lk.tgcompany.stylesync_admin.adapter.AdminBookingAdapter;
import lk.tgcompany.stylesync_admin.model.BookingModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AllBookingsActivity extends AppCompatActivity {

    private RecyclerView rvAllBookings;
    private FirebaseFirestore db;
    private List<BookingModel> bookingList;
    private AdminBookingAdapter adapter;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_bookings);

        db = FirebaseFirestore.getInstance();
        rvAllBookings = findViewById(R.id.rvAllBookings);
        rvAllBookings.setLayoutManager(new LinearLayoutManager(this));

        loadBookings();
    }

    private void loadBookings() {
        db.collection("Bookings").get().addOnSuccessListener(queryDocumentSnapshots -> {
            bookingList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                BookingModel model = doc.toObject(BookingModel.class);
                if (model != null) {
                    model.setDocumentId(doc.getId());
                    bookingList.add(model);
                }
            }

            // 1. Create a date format to parse your DB strings
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

            // 2. Sort the list by Date and then by Time
            Collections.sort(bookingList, new Comparator<BookingModel>() {
                @Override
                public int compare(BookingModel b1, BookingModel b2) {
                    try {
                        // Combine date and time to create a full timestamp
                        Date d1 = format.parse(b1.getDate() + " " + (b1.getTime() != null ? b1.getTime() : "00:00"));
                        Date d2 = format.parse(b2.getDate() + " " + (b2.getTime() != null ? b2.getTime() : "00:00"));

                        // Sort in Ascending order (Upcoming dates and early times first)
                        if (d1 != null && d2 != null) {
                            return d1.compareTo(d2);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });

            // 3. Clean expired bookings and update UI
            checkAndCleanExpiredBookings();
            setupAdapter();

        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error loading bookings", e);
        });
    }

    private void checkAndCleanExpiredBookings() {
        // 1. Get current Date and Time
        Date currentDate = new Date();

        // 2. Define the date format matching DB
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (BookingModel booking : bookingList) {
            try {
                if (booking.getDate() != null && booking.getTime() != null) {
                    // Combine Date and Time strings from DB
                    String dateTimeStr = booking.getDate() + " " + booking.getTime();
                    Date bookingStartDate = fullDateFormat.parse(dateTimeStr);

                    if (bookingStartDate != null) {
                        //Add 1 hour to the start time to get the "expiry" point
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(bookingStartDate);
                        cal.add(Calendar.HOUR_OF_DAY, 1);
                        Date expiryDate = cal.getTime();

                        // If current time is past the expiry date and status is "Paid & Confirmed"
                        if (currentDate.after(expiryDate) && "Paid & Confirmed".equals(booking.getStatus())) {

                            String docId = booking.getDocumentId();

                            // Update Firestore
                            db.collection("Bookings").document(docId)
                                    .update("status", "Done")
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Validation", "Booking " + docId + " marked as Done auto.");

                                        // Send Notification to user
//                                        sendNotificationToUser(booking.getUserId(), "Thank you for your attendance!");
                                    });

                            // Update local list object to reflect UI changes
                            booking.setStatus("Done");
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Validation", "Expiry check error for: " + booking.getDocumentId(), e);
            }
        }
    }

    private void setupAdapter() {
        adapter = new AdminBookingAdapter(bookingList, this, model -> {
            String[] options = {"Mark as Done", "Reschedule", "Delete"};
            new AlertDialog.Builder(this)
                    .setTitle("Manage Appointment")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) updateStatus(model.getDocumentId(), "Done");
                        else if (which == 1) showDatePicker(model);
                        else if (which == 2) deleteBooking(model.getDocumentId());
                    }).show();
        });
        rvAllBookings.setAdapter(adapter);
    }

    private void showDatePicker(BookingModel model) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {

            String newDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, (month + 1), year);

            showStartTimePicker(model, newDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showStartTimePicker(BookingModel model, String newDate) {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String start = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            showEndTimePicker(model, newDate, start);
        }, 10, 0, true).show();
    }

    private void showEndTimePicker(BookingModel model, String newDate, String start) {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            String end = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            validateAndReschedule(model.getDocumentId(), newDate, start, end);
        }, 11, 0, true).show();
    }

//    private void showDatePicker(BookingModel model) {
//        Calendar c = Calendar.getInstance();
//        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
//            String newDate = dayOfMonth + "/" + (month + 1) + "/" + year;
//            showStartTimePicker(model, newDate);
//        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
//    }
//
//    private void showStartTimePicker(BookingModel model, String newDate) {
//        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
//            String start = String.format("%02d:%02d", hourOfDay, minute);
//            showEndTimePicker(model, newDate, start);
//        }, 10, 0, true).show();
//    }
//
//    private void showEndTimePicker(BookingModel model, String newDate, String start) {
//        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
//            String end = String.format("%02d:%02d", hourOfDay, minute);
//            validateAndReschedule(model.getDocumentId(), newDate, start, end);
//        }, 11, 0, true).show();
//    }

    // --- MAIN VALIDATION LOGIC ---
    private void validateAndReschedule(String docId, String date, String newStart, String newEnd) {
        try {
            Date newStartTime = timeFormat.parse(newStart);
            Date newEndTime = timeFormat.parse(newEnd);

            // 1. Get all bookings for that specific date
            db.collection("Bookings").whereEqualTo("date", date).get()
                    .addOnSuccessListener(query -> {
                        boolean isOverlapping = false;

                        for (QueryDocumentSnapshot doc : query) {
                            // Current checking document eka api update karana eka nemei nam witarak check karanawa
                            if (doc.getId().equals(docId)) continue;

                            String existingTimeRange = doc.getString("time");
                            if (existingTimeRange != null && existingTimeRange.contains(" - ")) {
                                try {
                                    String[] parts = existingTimeRange.split(" - ");
                                    Date existingStart = timeFormat.parse(parts[0]);
                                    Date existingEnd = timeFormat.parse(parts[1]);

                                    // Check overlap logic: (StartA < EndB) and (EndA > StartB)
                                    if (newStartTime.before(existingEnd) && newEndTime.after(existingStart)) {
                                        isOverlapping = true;
                                        break;
                                    }
                                } catch (ParseException e) { e.printStackTrace(); }
                            }
                        }

                        if (isOverlapping) {
                            Toast.makeText(this, "Time Conflict! Another booking exists in this range.", Toast.LENGTH_LONG).show();
                        } else {
                            // All clear! Update Firestore
                            String finalTimeRange = newStart + " - " + newEnd;
                            db.collection("Bookings").document(docId)
                                    .update("date", date, "time", finalTimeRange, "status", "Rescheduled")
                                    .addOnSuccessListener(aVoid -> {
                                        loadBookings();
                                        Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    });

        } catch (ParseException e) {
            Toast.makeText(this, "Invalid Time Format", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatus(String docId, String status) {
        db.collection("Bookings").document(docId).update("status", status).addOnSuccessListener(aVoid -> loadBookings());
    }

    private void deleteBooking(String docId) {
        db.collection("Bookings").document(docId).delete().addOnSuccessListener(aVoid -> loadBookings());
    }

}