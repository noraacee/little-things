package badtzmarupekkle.littlethings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import badtzmarupekkle.littlethings.R;

public class CreateEntityFragment extends Fragment implements OnClickListener {

    private static final String DRAWABLE_ID_KEY = "drawableId";

    private OnClickListener ocListener;

    public static CreateEntityFragment newInstance(int drawableId) {
        CreateEntityFragment ceFragment = new CreateEntityFragment();
        Bundle args = new Bundle();
        args.putInt(DRAWABLE_ID_KEY, drawableId);
        ceFragment.setArguments(args);
        return ceFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_entity, container, false);
        ImageView createView = (ImageView) rootView.findViewById(R.id.create);
        createView.setImageResource(getArguments().getInt(DRAWABLE_ID_KEY));
        createView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClick(v);
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(ocListener != null)
            ocListener.onClick(v);
    }

    public void setOnClickListener(OnClickListener ocListener) {
        this.ocListener = ocListener;
    }
}
