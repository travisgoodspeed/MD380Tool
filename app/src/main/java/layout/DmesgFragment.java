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
import android.widget.TextView;

import com.travisgoodspeed.md380tool.MD380Exception;
import com.travisgoodspeed.md380tool.MD380Tool;
import com.travisgoodspeed.md380tool.MainActivity;
import com.travisgoodspeed.md380tool.R;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DmesgFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DmesgFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DmesgFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //! Buffer for our call log.
    public static String dmesglog="";
    private int oldsrc=0, olddst=0;

    /* This task grabs the dmesg at regular intervals, in the main thread
        to avoid contention.
     */
    private class FetchDmesgTask extends AsyncTask<TextView, Integer, Void> {
        Integer frame=0;
        @Override
        protected Void doInBackground(TextView... params) {
            TextView ti=params[0];
            //Run until we're cancelled.
            while(!isCancelled()) {
                frame = frame + 1;
                //The actual work is done in onProgressUpdate() in the UI thread.
                publishProgress(frame);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        protected void onProgressUpdate(Integer... params){
            //final TextView textInfo = (TextView) view.findViewById(R.id.txt_dmesg);
            Log.d("dmesgfetcher",
                    String.format("Fetched Dmesg frame %d",params[0]));
            MD380Tool tool= MainActivity.tool;
            try {
                if (tool!=null && tool.isConnected()) {
                    if(textInfo!=null)
                        textInfo.setText(addLog(tool.getDmesg()));
                    else
                        Log.e("Dmesg","textInfo==null.  WTF?");

                    //tool.drawText("Done!",160,50);
                } else {
                    textInfo.setText("Failed to connect.");
                }
            }catch(MD380Exception e){
                Log.e("MD380",e.getMessage());
                e.printStackTrace();
                if(textInfo!=null)
                    textInfo.setText(e.getMessage());
                tool.disconnect();
            }
        }
        protected void onPostExecute(String result){
            Log.d("dmesgfetched","The dmesg log task has completed.");
        }
    }
    FetchDmesgTask bgtask=null;

    //! Logs a new entry.
    public String addLog(String newfrag){
        //Append the new log fragment.
        dmesglog=dmesglog+newfrag;

        return dmesglog;
    }

    private OnFragmentInteractionListener mListener;

    public DmesgFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DmesgFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DmesgFragment newInstance(String param1, String param2) {
        DmesgFragment fragment = new DmesgFragment();
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


    TextView textInfo;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v= inflater.inflate(R.layout.fragment_dmesg, container, false);
        textInfo = (TextView) v.findViewById(R.id.txt_dmesg);

        /*
        Button btnCheck = (Button) v.findViewById(R.id.but_dmesg);

        btnCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //getPermissions();
                Log.e("Dmesg", "Button was pressed in the Dmesg view.");
            }
        });
        */

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

        if(bgtask==null && MainActivity.tool!=null && MainActivity.tool.isConnected()) {
            bgtask = new FetchDmesgTask();
            bgtask.execute(textInfo);
        }else{
            Log.d("dmesg","bgtask!=null at onAttach()!");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if(bgtask!=null){
            bgtask.cancel(false);
            bgtask=null;
        }
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
