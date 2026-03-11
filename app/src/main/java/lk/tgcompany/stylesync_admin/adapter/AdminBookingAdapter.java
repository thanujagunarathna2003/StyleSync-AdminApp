package lk.tgcompany.stylesync_admin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import lk.tgcompany.stylesync_admin.R;
import lk.tgcompany.stylesync_admin.model.BookingModel;
import java.util.List;

public class AdminBookingAdapter extends RecyclerView.Adapter<AdminBookingAdapter.ViewHolder> {

    private List<BookingModel> list;
    private Context context;
    private OnActionListener listener;

    public interface OnActionListener { void onAction(BookingModel model); }

    public AdminBookingAdapter(List<BookingModel> list, Context context, OnActionListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_admin_booking, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingModel model = list.get(position);

        holder.txtBookingId.setText("#ID: " + model.getDocumentId().substring(0, 6).toUpperCase());
        holder.txtServiceName.setText(model.getServiceName());
        holder.txtDateTimeSlot.setText(model.getDate() + " | " + model.getTime());
        holder.txtPrice.setText("Rs." + model.getPrice());

        String st = model.getStatus() != null ? model.getStatus() : "Pending";
        holder.txtStatus.setText(st.toUpperCase());

        if ("Done".equals(st) || "Completed".equals(st)) holder.txtStatus.setTextColor(Color.GREEN);
        else if ("Rescheduled".equals(st)) holder.txtStatus.setTextColor(Color.BLUE);
        else holder.txtStatus.setTextColor(Color.RED);

        holder.itemView.setOnLongClickListener(v -> {
            listener.onAction(model);
            return true;
        });
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtBookingId, txtCustomerName, txtServiceName, txtDateTimeSlot, txtPrice, txtStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBookingId = itemView.findViewById(R.id.txtBookingId);
            txtServiceName = itemView.findViewById(R.id.txtServiceName);
            txtDateTimeSlot = itemView.findViewById(R.id.txtDateTimeSlot);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }
}