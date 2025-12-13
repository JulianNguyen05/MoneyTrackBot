package ht.nguyenhuutrong.fe_moneytrackbot.dialogs;

import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateRangeDialog {

    // Interface để trả kết quả về cho Fragment
    public interface OnDateSelectedListener {
        void onDateSelected(String displayText, Long startDate, Long endDate);
    }

    public static void show(FragmentManager fragmentManager, OnDateSelectedListener listener) {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        builder.setTitleText("Chọn phạm vi thời gian");

        final MaterialDatePicker<Pair<Long, Long>> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("d 'thg' M, yyyy", new Locale("vi", "VN"));
            String text = sdf.format(new Date(selection.first)) + "  -  " + sdf.format(new Date(selection.second));

            if (listener != null) {
                listener.onDateSelected(text, selection.first, selection.second);
            }
        });

        datePicker.show(fragmentManager, "DATE_PICKER");
    }
}