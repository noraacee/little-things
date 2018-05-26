 package badtzmarupekkle.littlethings.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.timeslot.timeslotendpoint.Timeslotendpoint;
import com.littlethings.endpoint.timeslot.timeslotendpoint.Timeslotendpoint.Builder;
import com.littlethings.endpoint.timeslot.timeslotendpoint.model.TimeSlot;
import com.littlethings.endpoint.timeslot.timeslotendpoint.model.TimeSlotResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.util.SystemMessageManager;

public class CreateTimeSlotDialog extends CreateEntityDialog<TimeSlot> {
    private static final int COUNT_DAYS = 7;
    private static final int LENGTH_DESCRIPTION = 500;
    private static final int LENGTH_LOCATION = 50;
    private static final int LENGTH_NAME = 25;

    private static final String ERROR_MESSAGE_DAYS_SELECTED = "You must select at least one day";
    private static final String ERROR_MESSAGE_START_END = "The end time must not have passed and cannot be before the start time";
    protected static final String TIMESTAMP_PATTERN_12_HOURS = "MMMM dd - hh:mm aa";
    protected static final String TIMESTAMP_PATTERN_24_HOURS = "MMMM dd - HH:mm";
    private static final String VIEW_DESCRIPTION = "description";
    private static final String VIEW_LOCATION = "location";
    private static final String VIEW_NAME = "name";

    private int colorDeselected;
    private int colorPosition;
    private int colorsSize;

    private EditText locationView;
    private EditText nameView;
    private TextView descriptionView;

    private DateTime endDateTime;
    private DateTime startDateTime;
    private List<Boolean> days;
    private Timeslotendpoint endpoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        days = new ArrayList<>();
        for (int i = 0; i < COUNT_DAYS; i++)
            days.add(false);

        colorDeselected = getResources().getColor(R.color.grey_500);
        colorPosition = AppManager.getColorPosition();
        colorsSize = AppManager.getColorsSize();

        DateTime dateTime = new DateTime();
        endDateTime = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth() + 1, 0, 0);
        startDateTime = new DateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0);


        Builder builder = new Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpoint = builder.build();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_time_slot);

        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams layoutParams = dialogWindow.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(layoutParams);

        smManager = new SystemMessageManager(getActivity(), (TextView) dialog.findViewById(R.id.system_message));

        final TextView endInstantView = (TextView) dialog.findViewById(R.id.end_instant);
        final TextView startInstantView = (TextView) dialog.findViewById(R.id.start_instant);
        final View createLayout = dialog.findViewById(R.id.create_layout);
        final View daysLayout = dialog.findViewById(R.id.days_layout);
        final View infoLayout = dialog.findViewById(R.id.info_layout);

        locationView = (EditText) dialog.findViewById(R.id.location);
        nameView = (EditText) dialog.findViewById(R.id.name);
        descriptionView = (TextView) dialog.findViewById(R.id.description);

        descriptionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        createLayout.setBackgroundColor(AppManager.getColor());
        daysLayout.setBackgroundColor(AppManager.getSecondaryColor());
        infoLayout.setBackgroundColor(AppManager.getColor());
        infoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorPosition == colorsSize - 1)
                    colorPosition = 0;
                else
                    colorPosition++;

                createLayout.setBackgroundColor(AppManager.getColor(colorPosition));
                daysLayout.setBackgroundColor(AppManager.getSecondaryColor(colorPosition));
                infoLayout.setBackgroundColor(AppManager.getColor(colorPosition));
            }
        });

        endInstantView.setText(printDateTime(endDateTime));
        endInstantView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        startInstantView.setText(printDateTime(startDateTime));
        startInstantView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        dialog.findViewById(R.id.monday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDay((TextView) v, 0);
            }
        });

        dialog.findViewById(R.id.tuesday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDay((TextView) v, 1);
            }
        });

        dialog.findViewById(R.id.wednesday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDay((TextView) v, 2);
            }
        });

        dialog.findViewById(R.id.thursday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDay((TextView) v, 3);
            }
        });

        dialog.findViewById(R.id.friday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDay((TextView) v, 4);
            }
        });

        dialog.findViewById(R.id.saturday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDay((TextView) v, 5);
            }
        });

        dialog.findViewById(R.id.sunday).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDay((TextView) v, 6);
            }
        });


        dialog.findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateTimeSlot()) {
                    if (AppManager.checkNetworkConnection())
                        new CreateTimeSlotTask().execute();
                    else
                        smManager.displayError(SystemMessageManager.ERROR_NETWORK);
                }
            }
        });

        dialog.findViewById(R.id.decline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }

    private String printDateTime(DateTime dateTime) {
        DateTimeFormatter dtf;
        if(AppManager.is24Hour())
            dtf= DateTimeFormat.forPattern(TIMESTAMP_PATTERN_24_HOURS);
        else
            dtf=DateTimeFormat.forPattern(TIMESTAMP_PATTERN_12_HOURS);
        return dtf.print(dateTime);
    }

    private void selectDay(TextView day, int position) {
        days.set(position, !days.get(position));
        if (days.get(position))
            day.setTextColor(Color.WHITE);
        else
            day.setTextColor(colorDeselected);
    }

    private boolean validateTimeSlot() {
        boolean daySelected = false;
        for (int i = 0; i < COUNT_DAYS; i++) {
            if (days.get(i)) {
                daySelected = true;
                break;
            }
        }
        if (!daySelected) {
            smManager.displayError(ERROR_MESSAGE_DAYS_SELECTED);
            return false;
        }

        if (nameView.getText().toString().trim().length() == 0) {
            smManager.displayErrorFill(SystemMessageManager.ERROR_FILL_EMPTY, VIEW_NAME);
            return false;
        } else if (nameView.getText().toString().trim().length() > LENGTH_NAME) {
            smManager.displayErrorFill(SystemMessageManager.ERROR_FILL_EXCEED_LIMIT, VIEW_NAME, Integer.toString(LENGTH_NAME));
            return false;
        }

        if (locationView.getText().toString().trim().length() == 0 || locationView.getText().toString().trim().length() > LENGTH_LOCATION) {
            smManager.displayErrorFill(SystemMessageManager.ERROR_FILL_EMPTY, VIEW_LOCATION);
            return false;
        } else if (locationView.getText().toString().trim().length() > LENGTH_LOCATION) {
            smManager.displayErrorFill(SystemMessageManager.ERROR_FILL_EXCEED_LIMIT, VIEW_LOCATION, Integer.toString(LENGTH_LOCATION));
            return false;
        }

        if (descriptionView.getText().toString().trim().length() > LENGTH_DESCRIPTION) {
            smManager.displayErrorFill(SystemMessageManager.ERROR_FILL_EXCEED_LIMIT, VIEW_DESCRIPTION, Integer.toString(LENGTH_DESCRIPTION));
            return false;
        }

        if (endDateTime.getMillis() < System.currentTimeMillis() || startDateTime.getMillis() >= endDateTime.getMillis()) {
            smManager.displayError(ERROR_MESSAGE_START_END);
            return false;
        }

        return true;
    }

    private class CreateTimeSlotTask extends AsyncTask<Void, Void, TimeSlotResponse> {
        private TimeSlot timeSlot;

        public CreateTimeSlotTask() {
            timeSlot = new TimeSlot();
        }

        @Override
        protected void onPreExecute() {
            //TODO: show loading
        }

        @Override
        protected TimeSlotResponse doInBackground(Void... nothing) {
            timeSlot.setSecret(AppManager.getSecret());
            timeSlot.setWriter(AppManager.getWriter());

            timeSlot.setColor(AppManager.getColor(colorPosition));
            timeSlot.setEndDay(endDateTime.getDayOfMonth());
            timeSlot.setEndHour(endDateTime.getHourOfDay());
            timeSlot.setEndMinute(endDateTime.getMinuteOfHour());
            timeSlot.setEndMonth(endDateTime.getMonthOfYear());
            timeSlot.setEndYear(endDateTime.getYearOfCentury());
            timeSlot.setStartDay(startDateTime.getDayOfMonth());
            timeSlot.setStartHour(startDateTime.getHourOfDay());
            timeSlot.setStartMinute(startDateTime.getMinuteOfHour());
            timeSlot.setStartMonth(startDateTime.getMonthOfYear());
            timeSlot.setStartYear(startDateTime.getYearOfCentury());
            timeSlot.setDays(days);
            timeSlot.setName(nameView.getText().toString().trim());
            timeSlot.setLocation(locationView.getText().toString().trim());
            if (descriptionView.getText().toString().trim().length() > 0)
                timeSlot.setDescription(descriptionView.getText().toString().trim());

            try {
                return endpoint.add(timeSlot).execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(TimeSlotResponse response) {
            if (response != null)
                if (response.getSuccess()) {
                    timeSlot.setId(response.getId());
                    timeSlot.setTimestamp(System.currentTimeMillis());
                    onCreateEntity(timeSlot);
                } else {
                    smManager.displayError(response.getErrorCode());
                }
            else
                smManager.displayDefaultError();
        }
    }
}
