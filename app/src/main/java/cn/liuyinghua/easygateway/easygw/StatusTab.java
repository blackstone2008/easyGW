package cn.liuyinghua.easygateway.easygw;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

        final ImageButton internetStatus;
        internetStatus = (ImageButton) getView(). findViewById(R.id.ibInternetStatus);
        internetStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MainActivity.isConnected )
                    ((MainActivity)getActivity()).authDialog();
            }
        });
    }

    public void updateHostsList() {
        updateHostsListActive();
      //  updateHostsListInActive();
    }

    public void updateStatus(String result) {

        final String uciEnv = "uci.env.var.prod_friendly_name";
        final String rpcWan = "rpc.network.interface.@wan.ipaddr";
        final String uciSSID = "uci.wireless.wifi-iface.@wl0.ssid";
        final String uciSSID5G = "uci.wireless.wifi-iface.@wl1.ssid";
        final String rpcUpTime = "rpc.system.uptime";
        final String rpcHosts = "rpc.hosts.";

        String strProdName = ((MainActivity)getActivity()).getRetValueFromResponse(result, uciEnv);
        String strWanIP = ((MainActivity)getActivity()).getRetValueFromResponse(result, rpcWan);
        String strSSID = ((MainActivity)getActivity()).getRetValueFromResponse(result, uciSSID);
        String strSSID5G = ((MainActivity)getActivity()).getRetValueFromResponse(result, uciSSID5G);
        String strUpTime = ((MainActivity)getActivity()).getRetValueFromResponse(result, rpcUpTime);

        TextView tvProd = (TextView) ((MainActivity)getActivity()).findViewById(R.id.tvProductName);
        TextView tvWanIP = (TextView) ((MainActivity)getActivity()).findViewById(R.id.tvWanIP);
        tvWanIP.setText("WAN IP:" + strWanIP);

        if (strWanIP.length() > 0) {
            //Wan is up
            ImageButton ibWanStatus =  (ImageButton) ((MainActivity)getActivity()).findViewById(R.id.ibInternetStatus);
            ibWanStatus.setImageResource(R.mipmap.internet_connected);
        }
        else {
            ImageButton ibWanStatus =  (ImageButton) ((MainActivity)getActivity()).findViewById(R.id.ibInternetStatus);
            ibWanStatus.setImageResource(R.mipmap.internet_no_connection);
        }

        Toolbar toolbar = (Toolbar) ((MainActivity)getActivity()).findViewById(R.id.toolbar);
        toolbar.setTitle(/*strProdName + */" Connected");
        toolbar.setBackgroundColor(Color.rgb(0,200,0));

        TextView tvProductName = (TextView) ((MainActivity)getActivity()).findViewById(R.id.tvProductName);
        tvProductName.setText(strProdName);

        TextView tvSSID = (TextView) ((MainActivity)getActivity()).findViewById(R.id.tvSSID);
        tvSSID.setText("SSID(2.4G): " + strSSID);
        TextView tvSSID5G = (TextView) ((MainActivity)getActivity()).findViewById(R.id.tvSSID5G);
        tvSSID5G.setText("SSID(5G): " + strSSID5G);

        int uptime = Integer.parseInt(strUpTime);
        TextView tvUpTime = (TextView) ((MainActivity)getActivity()).findViewById(R.id.tvUpTime);
        tvUpTime.setText("UpTime:" + (uptime / 3600) + "H-" + (uptime % 3600) / 60 + "M-" + uptime % 60 + "S");
    }
    // true to display active, false to display inactive
    public void updateHostsListActive() {
        ListView lvHostList;
        lvHostList = (ListView) getView().findViewById(R.id.lvHostsList);
        String[] hostnames = {"State", "HostName", "IP", "MAC", "ConnectedTime"};
        int[] ids = {R.id.id_state_indicator, R.id.id_host_name, R.id.id_host_ip_addr,
                R.id.id_host_mac, R.id.id_host_connection_time};

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String connTime;
        int uptime;
        for (int i = 0; i < ((MainActivity) getActivity()).arHostName.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("HostName", ((MainActivity) getActivity()).arHostName.get(i));
            JSONObject jsonHost = ((MainActivity) getActivity()).jsonMapHostName.get(((MainActivity) getActivity()).arHostName.get(i));
            String state = ((MainActivity) getActivity()).getJsonParamValue(jsonHost, "State");
//            if (state.equals("0"))
  //              continue;
    //        map.put("State", R.drawable.offline);
            //only display active hsots
            if (state.equals("0")) {
                map.put("State", R.drawable.offline);
                Log.d("LIUYH", map.get("HostName") + "is offline");
            }
            else {
                map.put("State", R.drawable.online);
                Log.d("LIUYH", map.get("HostName") + "is online");
            }
            map.put("IP", ((MainActivity) getActivity()).getJsonParamValue(jsonHost, "IPAddress"));
            map.put("MAC", ((MainActivity) getActivity()).getJsonParamValue(jsonHost, "MACAddress"));


            connTime = ((MainActivity) getActivity()).getJsonParamValue(jsonHost, "ConnectedTime");
            uptime = Integer.parseInt(connTime);
            connTime = (uptime / 3600) + "h:" + (uptime % 3600) / 60 + "m:" + uptime % 60 + "s";
            map.put("ConnectedTime", connTime);

            Log.d("LIUYH", i + ".  " + map.get("HostName") +  "     :       "
                    + map.get("IP") + " : " +
                    map.get("MAC") + "   :   " + map.get("ConnectedTime"));

            list.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(getView().getContext(),
                list, R.layout.host_list,
                hostnames, ids);
        lvHostList.setAdapter(adapter);

        lvHostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(((MainActivity) getActivity()), ((MainActivity) getActivity()).arHostName.get(i),
                        Toast.LENGTH_LONG).show();
                String strHostName = ((MainActivity) getActivity()).arHostName.get(i);
                JSONObject jsonHostObj = ((MainActivity) getActivity()).jsonMapHostName.get(strHostName);
                ((MainActivity) getActivity()).showHostDialog(jsonHostObj);
            }
        });
    }

    public void updateHostsListInActive() {

        ListView lvHostList;
        lvHostList = (ListView) getView().findViewById(R.id.lvHostsList_inactive);
        String[] hostnames = {"State", "HostName", "IP", "MAC", "ConnectedTime"};
        int[] ids = {R.id.id_state_indicator, R.id.id_host_name, R.id.id_host_ip_addr,
                R.id.id_host_mac, R.id.id_host_connection_time};

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String connTime;
        int uptime;
        for (int i = 0; i < ((MainActivity) getActivity()).arHostName.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("HostName", ((MainActivity) getActivity()).arHostName.get(i));
            JSONObject jsonHost = ((MainActivity) getActivity()).jsonMapHostName.get(((MainActivity) getActivity()).arHostName.get(i));
            String state = ((MainActivity) getActivity()).getJsonParamValue(jsonHost, "State");
            if (state.equals("1"))
                continue;
            map.put("State", R.drawable.offline);
            //only display active hsots
            if (state.equals("0")) {
                map.put("State", R.drawable.offline);
            }
            else {
                map.put("State", R.drawable.online);
            }
            map.put("IP", ((MainActivity) getActivity()).getJsonParamValue(jsonHost, "IPAddress"));
            map.put("MAC", ((MainActivity) getActivity()).getJsonParamValue(jsonHost, "MACAddress"));

            connTime = ((MainActivity) getActivity()).getJsonParamValue(jsonHost, "ConnectedTime");
            uptime = Integer.parseInt(connTime);
            connTime = (uptime / 3600) + "h:" + (uptime % 3600) / 60 + "m:" + uptime % 60 + "s";
            map.put("ConnectedTime", connTime);

            list.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(getView().getContext(),
                list, R.layout.host_list,
                hostnames, ids);
        lvHostList.setAdapter(adapter);

        lvHostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(((MainActivity) getActivity()), ((MainActivity) getActivity()).arHostName.get(i),
                        Toast.LENGTH_LONG).show();
            }
        });


    }

}
