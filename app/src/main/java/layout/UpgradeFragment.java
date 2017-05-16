package layout;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.travisgoodspeed.md380tool.MD380Exception;
import com.travisgoodspeed.md380tool.MD380Tool;
import com.travisgoodspeed.md380tool.MainActivity;
import com.travisgoodspeed.md380tool.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UpgradeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UpgradeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UpgradeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    /* This task upgrades the radio firmware to an image including in the raw resources.
     */
    private class UpgradeTask extends AsyncTask<TextView, Integer, Void> {
        Integer frame=1;
        byte firmware[];
        @Override
        protected Void doInBackground(TextView... params) {
            //TextView ti=params[0];
            publishProgress(0);

            try {
                MainActivity.tool.upgradeApplicationInit(firmware);
            }catch(MD380Exception e){
                e.printStackTrace();
                return null;
            }

            //Run until we're cancelled.
            try {
                while (!isCancelled() && !MainActivity.tool.upgradeApplicationNextStep()) {
                    //The actual work is done in onProgressUpdate() in the UI thread.
                    if((frame%10)==0)
                        publishProgress(frame);
                    frame = frame + 1;
                }
            }catch(MD380Exception e){
                e.printStackTrace();
            }

            publishProgress(-1);
            return null;
        }

        protected void onProgressUpdate(Integer... params){
            //final TextView textInfo = (TextView) view.findViewById(R.id.txt_dmesg);

            Integer frame=params[0];
            Log.d("upgrade",
                    String.format("Upgrade frame %d", params[0]));

            if(frame==0) {
                progressBar.setMax(1000);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(500);
            }else if(frame==-1) {
                progressBar.setVisibility(View.INVISIBLE);
            }else {
                progressBar.setProgress(frame);
            }
        }
        protected void onPostExecute(String result){
            progressBar.setVisibility(View.INVISIBLE);
            Log.d("upgrade","The upgrade task has completed.");
        }

    }
    UpgradeTask bgtask=null;

    public UpgradeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UpgradeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UpgradeFragment newInstance(String param1, String param2) {
        UpgradeFragment fragment = new UpgradeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    ProgressBar progressBar=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_upgrade, container, false);

        Button btnUpgrade = (Button) v.findViewById(R.id.but_upgrade);
        progressBar=(ProgressBar) v.findViewById(R.id.pbar_upgrade);
        btnUpgrade.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //Install the firmware in a background thread.
                if(bgtask==null && MainActivity.tool!=null && MainActivity.tool.isConnected()) {
                    bgtask = new UpgradeTask();

                    //Grab the firmware as a raw resource.
                    InputStream ins = getResources().openRawResource(R.raw.firmware);
                    byte firmware[]=new byte[995840];
                    try {
                        Log.d("upgrade",
                                String.format("Flashing Non-GPS firmware."));
                        ins.read(firmware);
                        bgtask.firmware=firmware;
                        bgtask.execute();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }else{
                    Log.d("dmesg","bgtask!=null at onAttach()!");
                }
            }
        });

        Button btnUpgradeGPS = (Button) v.findViewById(R.id.but_upgradegps);
        btnUpgradeGPS.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //Install the firmware in a background thread.
                if(bgtask==null && MainActivity.tool!=null && MainActivity.tool.isConnected()) {
                    bgtask = new UpgradeTask();

                    //Grab the firmware as a raw resource.
                    InputStream ins = getResources().openRawResource(R.raw.firmwaregps);
                    byte firmware[]=new byte[999936];
                    try {
                        Log.d("upgrade",
                                String.format("Flashing GPS firmware."));
                        ins.read(firmware);
                        bgtask.firmware=firmware;
                        bgtask.execute();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }else{
                    Log.d("dmesg","bgtask!=null at onAttach()!");
                }
            }
        });

        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
