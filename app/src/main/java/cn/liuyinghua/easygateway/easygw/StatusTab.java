package cn.liuyinghua.easygateway.easygw;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
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


        Button btRefreshData = (Button) getView().findViewById(R.id.btRefresh);
        btRefreshData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //((MainActivity)getActivity()).refreshGateway();


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

    }

    public void updateHostsList() {
        updateHostsListActive();
        updateHostsListInActive();
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
            if (state.equals("0"))
                continue;
            map.put("State", R.drawable.offline);
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
            connTime = (uptime / 3600) + "H-" + (uptime % 3600) / 60 + "M-" + uptime % 60 + "S";
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
            connTime = (uptime / 3600) + "H-" + (uptime % 3600) / 60 + "M-" + uptime % 60 + "S";
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

    public void updateHostsList_2() {
        ListView lvHostList;
        lvHostList = (ListView) getView().findViewById(R.id.lvHostsList);
        String [] hostnames = {"HostName",  "IP",  "MAC",  "ConnectedTime"};
        int [] ids = {R.id.id_host_name, R.id.id_host_ip_addr,  R.id.id_host_mac,  R.id.id_host_connection_time};
        /*
        static Map<String, JSONObject> jsonMapHostName = new HashMap<String, JSONObject>();
        static List<String> arHostName = new ArrayList<String>();
        */
        ArrayList<HashMap<String,String>> list=null;
        list=new ArrayList<HashMap<String,String>>();
        HashMap<String,String> map=null;
        String connTime;
        int uptime;
        for(int i = 0; i < ((MainActivity)getActivity()).arHostName.size(); i++){
            map=new HashMap<String,String>();

            map.put("HostName", ((MainActivity)getActivity()).arHostName.get(i));
            JSONObject jsonHost = ((MainActivity)getActivity()).jsonMapHostName.get(((MainActivity)getActivity()).arHostName.get(i));
            map.put("IP",  ((MainActivity)getActivity()).getJsonParamValue(jsonHost, "IPAddress"));
            map.put("MAC",  ((MainActivity)getActivity()).getJsonParamValue(jsonHost, "MACAddress"));

            connTime =  ((MainActivity)getActivity()).getJsonParamValue(jsonHost, "ConnectedTime");
            uptime = Integer.parseInt(connTime);
            connTime = (uptime / 3600) + "H-" + (uptime % 3600) / 60 + "M-" + uptime % 60 + "S";
            map.put("ConnectedTime", connTime);

            Log.d("LIUYH", i + ".  " + map.get("HostName") + "     :       " + map.get("IP") + " : " +
                           map.get("MAC") + "   :   " + map.get("ConnectedTime"));
            list.add(map);
        }

        SimpleAdapter adapter=new SimpleAdapter(getView().getContext(),
                list, R.layout.host_list,
                hostnames, ids);
        lvHostList.setAdapter(adapter);

        lvHostList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(((MainActivity)getActivity()), ((MainActivity)getActivity()).arHostName.get(i),
                        Toast.LENGTH_LONG).show();
            }
        });

    }


}
