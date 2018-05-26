package badtzmarupekkle.littlethings.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.timeslot.timeslotendpoint.Timeslotendpoint;
import com.littlethings.endpoint.timeslot.timeslotendpoint.Timeslotendpoint.Builder;
import com.littlethings.endpoint.timeslot.timeslotendpoint.model.TimeSlot;
import com.littlethings.endpoint.timeslot.timeslotendpoint.model.TimeSlotResponse;

import java.io.IOException;

import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.activity.BlogActivity;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.dialog.CreateEntityDialog;
import badtzmarupekkle.littlethings.dialog.CreateTimeSlotDialog;
import badtzmarupekkle.littlethings.util.SystemMessageManager;

public class ViewTimeSlotsFragment extends ViewEntitiesFragment<TimeSlot> {
    private static final String TAG_CREATE_TIME_SLOT = "createTimeSlot";

    ImageView createView;

    Timeslotendpoint endpoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter=new TimeSlotsAdapter(activity);

        Builder builder = new Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpoint = builder.build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        entitiesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        entitiesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                return false;
            }
        });

        createView = (ImageView) rootView.findViewById(R.id.create);
        createView.setBackgroundColor(AppManager.getColor());
        createView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateTimeSlotDialog ctsDialog = new CreateTimeSlotDialog();
                ctsDialog.setOnCreateEntityListener(new CreateEntityDialog.OnCreateEntityListener<TimeSlot>() {
                    @Override
                    public void onCreateEntity(TimeSlot timeSlot) {
                        adapter.insert(timeSlot);
                        adapter.notifyDataSetChanged();
                    }
                });

                ctsDialog.show(activity.getSupportFragmentManager(), TAG_CREATE_TIME_SLOT);
            }
        });

        if(AppManager.checkNetworkConnection()) {
            //new GetTimeSlotsTask().execute();
        } else {
            smManager.displayError(SystemMessageManager.ERROR_NETWORK);
        }

        return rootView;
    }

    @Override
    public View getCreateEntityView() {
        return createView;
    }

    @Override
    protected int getEntitiesLimit() {
        return -1;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_view_time_slots;
    }

    @Override
    protected GetEntitiesTask getTask(boolean refresh) {
        return null;
    }

    @Override
    protected void setRefreshing(boolean refreshing) {

    }

    @Override
    protected void updateTimestamp() {

    }

    private class GetTimeSlotsTask extends GetEntitiesTask {

        public GetTimeSlotsTask() {
            super(false);
        }

        @Override
        protected EntityResponse doInBackground(Void... nothing) {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setSecret(AppManager.getSecret());

            try {
                TimeSlotResponse response = endpoint.getDay(timeSlot).execute();
                return new EntityResponse(response.getSuccess(), response.getErrorCode(), response.getTimeSlots());
            } catch(IOException e) {
                return null;
            }
        }
    }

    private class TimeSlotsAdapter extends EntityAdapter {

        public TimeSlotsAdapter(BlogActivity activity) {
            super(activity);
        }

        @Override
        public long getItemId(int position) {
            return entities.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {

            }

            return convertView;
        }

        private class ViewHolder {
        }
    }
}
