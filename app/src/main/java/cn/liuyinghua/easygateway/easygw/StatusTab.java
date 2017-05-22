package cn.liuyinghua.easygateway.easygw;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lyh on 2017/5/22.
 */

public class StatusTab extends Fragment {



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_status, container, false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //((MainActivity)getActivity()).connectGatewayWithCommand()

        Button btRefreshData = (Button) getView().findViewById(R.id.btRefresh);
        btRefreshData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //final String uciProdName = "uci.env.var.prod_friendly_name";
                final String uciEnv = "uci.env.var.";
                final String uciSSID = "uci.wireless.wifi-iface.@wl0.ssid";
                final String rpcUpTime = "rpc.system.uptime";
                final String rpcHosts = "rpc.hosts.";
                final String rpcWan = "rpc.network.interface.@wan.";
                //Very important for multiple commands in one rpc
                final String connector = "\"" + ",\n" + "\"";
                //连接gateway之前只考虑了一个命令，所以需要给get参数添加双引号
                String cmd = "\"" + uciSSID + connector + rpcUpTime + connector
                        + uciEnv + connector + rpcHosts + connector + rpcWan + "\",";

                ((MainActivity)getActivity()).connectGatewayWithCommand("get", cmd, new MainActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        String strProdName = MainActivity.getRetValueFromResponse(result, uciEnv, "prod_friendly_name");
                        String strHwVersion = MainActivity.getRetValueFromResponse(result, uciEnv, "hardware_version");
                        String strSSID = MainActivity.getRetValueFromResponse(result, uciSSID);
                        String strUpTime = MainActivity.getRetValueFromResponse(result, rpcUpTime);
                        String strWANIP = MainActivity.getRetValueFromResponse(result, rpcWan, "ipaddr");
                        // Log.d("LIUYH", result);
                        // Log.d("LIUYH", "wan ip is " + strWANIP);

                        int uptime = Integer.parseInt(strUpTime);
                        TextView tvProdName = (TextView) getView().findViewById(R.id.tvGatewayModel);
                        tvProdName.setText("Product Name: " + strProdName);
                        TextView tvHWVersion = (TextView) getView().findViewById(R.id.tvHWVersion);
                        tvHWVersion.setText("Hardware Version: " + strHwVersion);
                        TextView tvSSID = (TextView) getView().findViewById(R.id.tvSSID);
                        tvSSID.setText("SSID: " + strSSID);
                        TextView tvUpTime = (TextView) getView().findViewById(R.id.tvUpTime);
                        tvUpTime.setText("UpTime:" + (uptime / 3600) + "hours " + (uptime % 3600) / 60 + "minutes " + uptime % 60 + "seconds");
                        TextView tvWanIP = (TextView) getView().findViewById(R.id.tvWanIP);
                        tvWanIP.setText("WAN IP: " + strWANIP);

                        ((MainActivity)getActivity()).getHostFromResponse(result);

                    }
                });
            }
        });

        Button btHost = (Button) getView().findViewById(R.id.btHostInfo);
        btHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showClickMessage("test");
                ((MainActivity)getActivity()).showListDialog();
            }
        });

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
