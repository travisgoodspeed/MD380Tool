package layout;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.travisgoodspeed.md380tool.MD380Exception;
import com.travisgoodspeed.md380tool.MD380Tool;
import com.travisgoodspeed.md380tool.MainActivity;
import com.travisgoodspeed.md380tool.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LogFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //! Buffer for our call log.
    public static String calllog="";
    private int oldsrc=0, olddst=0;
    //! Logs a new entry, if it's new.
    public String addLog(int src, int dst){
        
        //Is the record new?
        if(oldsrc!=src || olddst!=dst){
            //Update so we don't double-list.
            oldsrc=src; olddst=dst;
            //Add it to the text log.
            calllog=String.format("Call from %d to %d.\n",src,dst)+calllog;
            //TODO long-term logging?  Name lookups?
        }

        return calllog;
    }


    public LogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LogFragment newInstance(String param1, String param2) {
        LogFragment fragment = new LogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_log, container, false);

        Button btnCheck = (Button) v.findViewById(R.id.check);
        final TextView textInfo = (TextView) v.findViewById(R.id.info);
        btnCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //getPermissions();
                Log.e("what", "Button was pressed in the Log view.");

                //TextView textInfo = (TextView) v.findViewById(R.id.info);

                MD380Tool tool= MainActivity.tool;
                try {
                    if (tool!=null && tool.connect()) {
                        int[] log=tool.getCallLog();

                        if(textInfo!=null)
                            textInfo.setText("\n\n\n\n"+addLog(log[1],log[2]));
                        else
                            Log.e("textInfo","textInfo==null.  WTF?");

                        tool.drawText("Done!",160,50);
                    } else {
                        textInfo.setText("\n\n\n\nFailed to connect.");
                    }
                }catch(MD380Exception e){
                    Log.e("MD380",e.getMessage());
                    e.printStackTrace();
                    if(textInfo!=null)
                        textInfo.setText(e.getMessage());
                    tool.disconnect();
                }

            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
