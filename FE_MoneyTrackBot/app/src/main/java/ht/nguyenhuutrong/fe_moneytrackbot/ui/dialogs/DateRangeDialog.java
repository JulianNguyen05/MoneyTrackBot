package ht.nguyenhuutrong.fe_moneytrackbot.ui.dialogs;

import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dialog chọn khoảng thời gian (Date Range)
 * Sử dụng MaterialDatePicker
 */
public class DateRangeDialog {

    /**
     * Callback trả về kết quả ngày đã chọn
     */
    public interface OnDateSelectedListener {
        void onDateSelected(String displayText, Long startDate, Long endDate);
    }

    /**
     * Hiển thị Date Range Picker
     */
    public static void show(FragmentManager fragmentManager,
                            OnDateSelectedListener listener) {

        MaterialDatePicker<Pair<Long, Long>> datePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Chọn phạm vi thời gian")
                        .build();

        // Người dùng nhấn OK
        datePicker.addOnPositiveButtonClickListener(selection -> {

            // Định dạng hiển thị ngày (UI)
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "d 'thg' M, yyyy",
                    new Locale("vi", "VN")
            );

            String displayText =
                    sdf.format(new Date(selection.first)) +
                            "  -  " +
                            sdf.format(new Date(selection.second));

            if (listener != null) {
                listener.onDateSelected(
                        displayText,
                        selection.first,
                        selection.second
                );
            }
        });

        datePicker.show(fragmentManager, "DATE_RANGE_PICKER");
    }
}