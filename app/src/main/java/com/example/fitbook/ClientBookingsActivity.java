package com.example.fitbook;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ClientBookingsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private long clientId;
    private final ArrayList<Long> bookingIds = new ArrayList<>();
    private final ArrayList<BookingItem> items = new ArrayList<>();
    private ListView listView;
    private TextView tvEmpty;

    private static class BookingItem { String title; String meta; String status; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.requireRole(this, "client")) return;
        setContentView(R.layout.activity_client_bookings);
        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        clientId = prefs.getLong(DatabaseHelper.COL_USER_ID, 0);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        listView = findViewById(R.id.listView);
        tvEmpty = findViewById(R.id.tvEmpty);
        listView.setAdapter(new Adapter());
        render();
    }

    private void render() {
        items.clear();
        bookingIds.clear();
        Cursor bookings = dbHelper.getMyBookings(clientId);
        if (bookings != null && bookings.moveToFirst()) {
            do {
                BookingItem item = new BookingItem();
                long bookingId = bookings.getLong(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_ID));
                String workoutType = bookings.getString(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TYPE));
                String date = DateFormatUtils.formatRussianDate(bookings.getString(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_DATE)));
                String time = bookings.getString(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_WORKOUT_TIME));
                String trainer = bookings.getString(bookings.getColumnIndexOrThrow("trainer_name"));
                String status = bookings.getString(bookings.getColumnIndexOrThrow(DatabaseHelper.COL_BOOKING_STATUS));
                item.title = workoutType;
                item.meta = "Дата: " + date + "\nВремя: " + time + "\nТренер: " + trainer;
                item.status = status;
                bookingIds.add(bookingId);
                items.add(item);
            } while (bookings.moveToNext());
            bookings.close();
        }
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        listView.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
    }

    private void markCompleted(long bookingId) {
        boolean success = dbHelper.markBookingCompleted(bookingId);
        Toast.makeText(this, success ? "Выполнено" : "Не удалось сохранить результат", Toast.LENGTH_SHORT).show();
        if (success) render();
    }

    private class Adapter extends BaseAdapter {
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return bookingIds.get(position); }
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_client_booking, parent, false);
            }
            BookingItem item = items.get(position);
            ((TextView) convertView.findViewById(R.id.tvTitle)).setText(item.title);
            ((TextView) convertView.findViewById(R.id.tvMeta)).setText(item.meta);
            TextView tvStatusTag = convertView.findViewById(R.id.tvStatusTag);
            MaterialButton btnDone = convertView.findViewById(R.id.btnDone);
            boolean done = "completed".equals(item.status);
            boolean canFinish = "confirmed".equals(item.status);
            tvStatusTag.setVisibility(done ? View.VISIBLE : View.GONE);
            btnDone.setVisibility(canFinish ? View.VISIBLE : View.GONE);
            btnDone.setOnClickListener(v -> markCompleted(bookingIds.get(position)));
            return convertView;
        }
    }
}
