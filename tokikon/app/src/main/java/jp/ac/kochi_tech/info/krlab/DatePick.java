package jp.ac.kochi_tech.info.krlab;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import java.util.Calendar;

public class DatePick extends DialogFragment implements
        DatePickerDialog.OnDateSetListener{

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH)+1; // 0から始まるので＋1
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(),
                this,  year, month, day);
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year,
                          int monthOfYear, int dayOfMonth) {
    }

}
