package cn.liuyinghua.easygateway.easygw;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyh on 2017/5/22.
 */

public class ToolboxTab extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_toolbox, container, false);



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();


        Button btSpeedTest = (Button) getView().findViewById(R.id.id_speed_test);
        btSpeedTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final long startTime = System.nanoTime();
                doSpeedTest(new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        long endTime = System.nanoTime();
                        long duration = endTime - startTime;
                        Log.d("speedTest", "start=" + startTime + "  endTime= " + endTime);
                        Log.d("speedTest", "duration in sec: " + (float)duration/1000000000f);
                        double sec = (float)duration/1000000000f;
                        double speed = 8.0/sec; // 8 is bit, the file downloaded is 1M, so in total 8Mb;
                        DecimalFormat df = new DecimalFormat("#.0");

                        Log.d("speedTest", "duration of testing: " + speed);
                        TextView tvResult = (TextView)getView().findViewById(R.id.id_tv_speed_result);
                        tvResult.setText("Estimated Bandwidth: " + df.format(speed));
                    }
                });
            }
        });

        Button btLog = (Button) getView().findViewById(R.id.id_log);
        btLog.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((MainActivity)getActivity()).refreshGate way();
                final String strCmd = "InternetGatewayDevice.DeviceInfo.DeviceLog";
                String cmd = "\"" + strCmd + "\"";
                ((MainActivity)getActivity()).connectGatewayWithCommand("get", cmd, new MainActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        String strLog = ((MainActivity)getActivity()).getRetValueFromResponse(result, strCmd);
                        String[] log_array = strLog.split("\n");

                        AlertDialog.Builder logDialog =
                                new AlertDialog.Builder((MainActivity)getActivity());
                        logDialog.setTitle("Logs");
                        logDialog.setItems(log_array, null);
                        logDialog.setPositiveButton("OK", null);
                        logDialog.show();
                    }
                });


            }
        });
    }


    public void doSpeedTest(final VolleyCallback callback) {
        //final long startTime = 0;
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        String url = "http://218.26.109.107/SpeedTest/index.php?file=1m&r=0.0";
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        };

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(response);
            }
        };

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, responseListener, errorListener) {        };

        //here is the start of time;
        requestQueue.add(stringRequest);

    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }


}
