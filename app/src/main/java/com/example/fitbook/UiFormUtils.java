package com.example.fitbook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class UiFormUtils {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private UiFormUtils() {
    }

    public static void attachDatePicker(@NonNull FragmentActivity activity, @NonNull TextView field) {
        field.setFocusable(false);
        field.setClickable(true);
        field.setCursorVisible(false);
        field.setOnClickListener(v -> {
            long selectedDate = 0L;
            String current = field.getText() == null ? "" : field.getText().toString().trim();
            if (!current.isEmpty()) {
                try {
                    Calendar parsed = Calendar.getInstance();
                    parsed.setTime(DATE_FORMAT.parse(current));
                    selectedDate = parsed.getTimeInMillis();
                } catch (Exception ignored) {
                    selectedDate = 0L;
                }
            }
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Выберите дату")
                    .setSelection(selectedDate == 0L ? MaterialDatePicker.todayInUtcMilliseconds() : selectedDate)
                    .build();
            picker.addOnPositiveButtonClickListener(selection -> {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(selection);
                field.setText(DATE_FORMAT.format(calendar.getTime()));
            });
            picker.show(activity.getSupportFragmentManager(), "fitbook_date_picker");
        });
    }

    public static void attachQuarterHourTimePicker(@NonNull Context context, @NonNull MaterialAutoCompleteTextView field) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.item_dropdown_dark, buildQuarterHourTimes());
        adapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        field.setAdapter(adapter);
        field.setTextColor(context.getColor(R.color.fitbook_text_primary));
        field.setHintTextColor(context.getColor(R.color.fitbook_text_hint));
        field.setInputType(android.text.InputType.TYPE_NULL);
        field.setKeyListener(null);
        field.setOnClickListener(v -> field.showDropDown());
        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                field.showDropDown();
            }
        });
    }

    public static void attachDarkSpinner(@NonNull Context context, @NonNull Spinner spinner, @NonNull List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.item_dropdown_dark, new ArrayList<>(items));
        adapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        spinner.setAdapter(adapter);
    }

    public static void attachDarkDropdown(@NonNull Context context, @NonNull MaterialAutoCompleteTextView field, @NonNull List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.item_dropdown_dark, new ArrayList<>(items));
        adapter.setDropDownViewResource(R.layout.item_dropdown_dark_dropdown);
        field.setAdapter(adapter);
    }

    @NonNull
    private static List<String> buildQuarterHourTimes() {
        ArrayList<String> times = new ArrayList<>(57);
        for (int hour = 8; hour <= 22; hour++) {
            for (int minute = 0; minute < 60; minute += 15) {
                if (hour == 22 && minute > 0) {
                    break;
                }
                times.add(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            }
        }
        return times;
    }
}
