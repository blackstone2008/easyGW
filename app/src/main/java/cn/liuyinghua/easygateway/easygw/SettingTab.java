package cn.liuyinghua.easygateway.easygw;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by lyh on 2017/5/22.
 */

public class SettingTab extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_setting, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Button btWiFiPower = (Button) getView().findViewById(R.id.btWiFiPower);
        btWiFiPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strCmdTxPower = MainActivity.constructSetCommandString(
                        "uci.wireless.wifi-device.@radio_2G.tx_power_adjust", "+2");
                String strCmdOverrideRegulartory = MainActivity.constructSetCommandString(
                        "uci.wireless.wifi-device.@radio_2G.tx_power_overrule_reg", "1" );

                ((MainActivity)getActivity()).connectGatewayWithCommand("set", strCmdTxPower + strCmdOverrideRegulartory,
                        new MainActivity.VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.d("LIUYH", "Result of set");
                                Log.d("LIUYH", result);
                            }
                        });

                ((MainActivity)getActivity()).connectGatewayWithCommand("apply", null, new MainActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.d("LIUYH", "Result of apply");
                        Log.d("LIUYH", result);
                    }
                });
            }
        });
    }
}
