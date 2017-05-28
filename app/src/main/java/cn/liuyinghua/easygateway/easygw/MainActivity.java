package cn.liuyinghua.easygateway.easygw;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import android.content.DialogInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    static ArrayList<String> respStringList = new ArrayList<String>();
    static Map<String, JSONObject> jsonMapHostName = new HashMap<String, JSONObject>();
    static Map<String, String> strHostName = new HashMap<String, String>();
    static List<String> arHostName = new ArrayList<String>();

    // default token in header, it is used as key for authentication a session
    static String strTokenInHeader = "";
    // is gateway can be connected or not, use the Token defined above
    static boolean isConnected = false;
    static final String URL = "http://192.168.1.1/api";

    private StatusTab statusTab;
    private SettingTab settingTab;
    private ToolboxTab toolboxTab;

    private LinearLayout mTabStatus;
    private LinearLayout mTabSetting;
    private LinearLayout mTabToolbox;

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        fragmentManager = getFragmentManager();
        setTabSelection(0);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (isConnected == false)
            toolbar.setBackgroundColor(Color.GRAY);
        toolbar.setTitle("Gateway not connected");
        setSupportActionBar(toolbar);



/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
    }

    private void initViews()
    {
        mTabStatus = (LinearLayout) findViewById(R.id.id_tab_bottom_status);
        mTabSetting = (LinearLayout) findViewById(R.id.id_tab_bottom_setting);
        mTabToolbox = (LinearLayout) findViewById(R.id.id_tab_bottom_toolbox);

        mTabStatus.setOnClickListener(this);
        mTabSetting.setOnClickListener(this);
        mTabToolbox.setOnClickListener(this);

    }

    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.id_tab_bottom_status:
                setTabSelection(0);
                break;
            case R.id.id_tab_bottom_setting:
                setTabSelection(1);
                break;
            case R.id.id_tab_bottom_toolbox:
                setTabSelection(2);
                break;

            default:
                break;
        }
    }

    private void setTabSelection(int index)
    {
        // 重置按钮
        resetBtn();
        // 开启一个Fragment事务
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况
        hideFragments(transaction);
        switch (index)
        {
            case 0:
                // 当点击了消息tab时，改变控件的图片和文字颜色
                ((ImageButton) mTabStatus.findViewById(R.id.btn_tab_bottom_status))
                        .setImageResource(R.mipmap.tab_status_pressed);
                if (statusTab == null)
                {
                    // 如果MessageFragment为空，则创建一个并添加到界面上
                    statusTab = new StatusTab();
                    transaction.add(R.id.id_content, statusTab);
                } else
                {
                    // 如果MessageFragment不为空，则直接将它显示出来
                    transaction.show(statusTab);
                }
                break;
            case 1:
                // 当点击了消息tab时，改变控件的图片和文字颜色
                ((ImageButton) mTabSetting.findViewById(R.id.btn_tab_bottom_setting))
                        .setImageResource(R.mipmap.tab_settings_pressed);
                if (settingTab == null)
                {
                    // 如果MessageFragment为空，则创建一个并添加到界面上
                    settingTab = new SettingTab();
                    transaction.add(R.id.id_content, settingTab);
                } else
                {
                    // 如果MessageFragment不为空，则直接将它显示出来
                    transaction.show(settingTab);
                }
                break;
            case 2:
                // 当点击了动态tab时，改变控件的图片和文字颜色
                ((ImageButton) mTabToolbox.findViewById(R.id.btn_tab_bottom_toolbox))
                        .setImageResource(R.mipmap.tab_toobox_pressed);
                if (toolboxTab == null)
                {
                    // 如果NewsFragment为空，则创建一个并添加到界面上
                    toolboxTab = new ToolboxTab();
                    transaction.add(R.id.id_content, toolboxTab);
                } else
                {
                    // 如果NewsFragment不为空，则直接将它显示出来
                    transaction.show(toolboxTab);
                }
                break;
        }
        transaction.commit();
    }

    /**
     * 清除掉所有的选中状态。
     */
    private void resetBtn()
    {
        ((ImageButton) mTabStatus.findViewById(R.id.btn_tab_bottom_status))
                .setImageResource(R.mipmap.tab_status_normal);
        ((ImageButton) mTabSetting.findViewById(R.id.btn_tab_bottom_setting))
                .setImageResource(R.mipmap.tab_settings_normal);
        ((ImageButton) mTabToolbox.findViewById(R.id.btn_tab_bottom_toolbox))
                .setImageResource(R.mipmap.tab_toobox_normal);

    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     *
     * @param transaction
     *            用于对Fragment执行操作的事务
     */
    @SuppressLint("NewApi")
    private void hideFragments(FragmentTransaction transaction)
    {
        if (statusTab != null)
        {
            transaction.hide(statusTab);
        }
        if (settingTab != null)
        {
            transaction.hide(settingTab);
        }
        if (toolboxTab != null)
        {
            transaction.hide(toolboxTab);
        }
    }



    public static String constructSetCommandString(String strParam, String strValue) {
        return "\"" + strParam + "\"" + ":" + "\"" + strValue + "\",\n";
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this,
                    "SETTNIG",
                    Toast.LENGTH_SHORT).show();
            authDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //paramPath is the full path of the parameter
    //For example, env.var.prod_friendly_name=
    public String getRetValue(String jsonStr, String paramPath) {

        int posParam = paramPath.lastIndexOf(".");
        System.out.println(posParam);
        String path = paramPath.substring(0, posParam + 1);
        System.out.println("path = " + path);
        String value = paramPath.substring(posParam + 1, paramPath.length());
        System.out.println("param = " + value);

        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject responseJson = json.getJSONObject("response");
            JSONObject pathJson = responseJson.getJSONObject(path);
            JSONObject paramJson = pathJson.getJSONObject(value);
            String retStr = paramJson.getString("value");
            return retStr;
        } catch (JSONException e) {
            Log.e("EasyGateway", "Failed to crete json object " + e.getMessage());
        }
        return null;
    }


    // 如果paramPath是一个参数的名字（比如uci.env.var.prod_friendly_name，其特点为结束字符不包含"." ），其返回格式是上一级path，然后包括Json object 参数，里边的value/type是骑值
    /*
        "uci.env.var.":{
      "prod_friendly_name":{
        "value":"MediaAccess TGiiNet-2",
        "type":"string"
         }
        },
    如果paramPath是一个父节点，则其返回值是父节点为json的名称；与上边只返回一个参数不同，这里返回的所有的参数
     */
    public static String getRetValueFromResponse(String jsonStr, String paramPath) {

        //参数全路径，指明的是叶子节点

        int posParam = paramPath.lastIndexOf(".");
        String path = paramPath.substring(0, posParam + 1);
        String value = paramPath.substring(posParam + 1, paramPath.length());

        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject responseJson = json.getJSONObject("response");
            JSONObject pathJson = responseJson.getJSONObject(path);
            JSONObject paramJson = pathJson.getJSONObject(value);
            String retStr = paramJson.getString("value");
            return retStr;
        } catch (JSONException e) {
            Log.e("LYH", "Failed to crete json object in getRetValueFromJSon " + e.getMessage());
        }
        return null;
    }

    //jsonStr is the response Json string
    //paramPath is the parent node ended with "."
    //return value is the JSON object for the parent object
    public static String getRetValueFromResponse(String jsonStr, String paramPath, String paraName) {

        //参数全路径，指明的是叶子节点
        if (paramPath.charAt(paramPath.length() - 1) != '.') {
            Log.e("LYH", "param path and name should be provided");
            return null;
        }
        //path是以"."结尾的path，意思是某个父节点的名字
        String path = paramPath;
        String value = paraName;

        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject responseJson = json.getJSONObject("response");
            JSONObject pathJson = responseJson.getJSONObject(path);
            JSONObject paramJson = pathJson.getJSONObject(value);
            String retStr = paramJson.getString("value");
            return retStr;
        } catch (JSONException e) {
            Log.e("LYH", "Failed to crete json object in getRetValueFromJSon " + e.getMessage());
        }
        return null;
    }

    public static JSONObject getJsonFromResponse(String jsonStr, String paramPath) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject responseJson = json.getJSONObject("response");
            JSONObject pathJson = responseJson.getJSONObject(paramPath);
            return pathJson;
        } catch (JSONException e) {
            Log.e("EasyGateway", "Failed to crete json object in getJsonFromResponse " + e.getMessage());
        }
        return null;
    }

    /*
    Get format:
      {
        "command":"get",
        "data":[
            "uci.wireless.wifidevice.@radio_2G.channel",
            "uci.wireless.wifidevice.@radio_2G.standard",
            "rpc.wireless.ssid."
          ]
      }

      Set format
      {
        "command":"set",
        "data":{
          "uci.wireless.wifi-device.@radio_2G.channel":"1",
            "uci.wireless.wifi-device.@radio_2G.standard":"bg"
         }
      }

      Apply format:
      {
        "command":"apply",
        "data":true    => NO ", be careful
      }

     Path is created outside of this function
     if more than 1 command is used, the quotation mark should be added before calling;
     */


    public String connectGatewayWithCommand(String strCommand, final String strPath, final VolleyCallback callback) {

        String retStr;

        final long startTime = System.nanoTime();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String body;
        if (strCommand.equals("get") || strCommand.equals("set")) {
             body = new String("{\n" +
                    "  \"command\":\"" + strCommand + "\",\n" +
                    "  \"data\":[\n" +
                    strPath + "\n" +
                    // "    \"" + strPath + "\",\n" +
                    "  ]\n" +
                    "}\n");
        }
        else if (strCommand.equals("apply")) {

             body = new String("{\n" +
                    "  \"command\":\"apply\",\n" +
                    "  \"data\":true\n" +
                    "}\n");
        }
        else {
            return null;
        }

        Log.d("liuyh", "new body");
        Log.d("liuyh", body);

        final String requestBody = body;

        SsX509TrustManager.allowAllSSL();

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.onSuccess(response);
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY", error.toString());
            }
        };

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, responseListener, errorListener) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                // token is the password for authentication
                params.put("X-tch-token", strTokenInHeader);
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    if (requestBody != null) {
                        String body = new String(requestBody.getBytes());
                        // Log.d("LIUYH", "get body by getBody ==> " + body);
                    }
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (Exception uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };

        requestQueue.add(stringRequest);

        return null;
    }

    // json is the json object that includes parameters, due to the fact
    // that the parameters includes 2 memeber:
    //  1. value => real value of the parameter
    //  2. type => type of the parameter
    // (firstly used when getting paramters value from hosts, so many parameters:(
    public String getJsonParamValue(JSONObject jsonObj, String strParam) {
        String strVal = null;
        try {
            JSONObject jsonSubOjb = jsonObj.getJSONObject(strParam);
            strVal = jsonSubOjb.getString("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return strVal;
    }

    //result is the returned response
    public String getHostFromResponse(String result) {
        //rpc.hosts.HostNumberOfEntries => max hosts
        //rpc.hosts.host.21.  The problem is that the index is not consisent, how to handle
        String strHostNum = getRetValueFromResponse(result, "rpc.hosts.HostNumberOfEntries");
        int hostNum = Integer.parseInt(strHostNum);
        int activeHost = 0;
        String strHostInfo = null;
        strHostName.clear();
        arHostName.clear();
        jsonMapHostName.clear();

        //Assume we have max  255 hosts
        //The problem here is that the host.i is not sequential, so we need construct every possible
        //host rpc string and try to find it in the result string (which is a big json string)
        for (int i = 0; i < 255; i++) {
            String rpcHostPath = String.format("rpc.hosts.host.%d.", i);
            // If the rpc path is in the repsonse message, then handle it
            if (result.contains(rpcHostPath) != true)
                continue;

            // Log.d("HOST", "Find host " + rpcHostPath + "in the result string");
            JSONObject jsonObj = getJsonFromResponse(result, rpcHostPath);
            if (jsonObj != null) {
                /*
                String strState = null;
                strState = getJsonParamValue(jsonObj, "State");
                if (strState.equals("0"))
                    continue;
                */
                //State = 1, this is an active host
                String strHostFriendlyName = getJsonParamValue(jsonObj, "FriendlyName");
                String strIP = getJsonParamValue(jsonObj, "IPAddress");
                strHostInfo += strHostFriendlyName + "     :     " + strIP + "\n";
                //for displaying in diaglog
                strHostName.put(strHostFriendlyName, rpcHostPath);
                jsonMapHostName.put(strHostFriendlyName, jsonObj);
                arHostName.add(strHostFriendlyName);
                if (activeHost++ >= hostNum)
                    break;
            }
        }
        TextView tvHosts = (TextView) findViewById(R.id.tvHostInfo);
        if (strHostInfo != null)
            tvHosts.setText(strHostInfo);
        else
            tvHosts.setText("NO host info collected");
        return strHostInfo;
    }



    private void showClickMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void showListDialog() {
        //List<String> arHostName => how to convert it to a String
        int size = arHostName.size();
        //final String[] items = (String[]) arHostName.toArray(new String[size]);

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(size + "Active Hosts Information");
        listDialog.setItems(arHostName.toArray(new String[size]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                // ...To-do
                //jsonMapHostName.put(strHostFriendlyName, jsonObj);  => here we can get the jsonObject of host and display all information.
                //Toast.makeText(MainActivity.this,"你点击了" + items[which],Toast.LENGTH_SHORT).show();

                JSONObject jsonHost = jsonMapHostName.get(arHostName.get(which));
                showHostDialog(jsonHost);
            }
        });
        listDialog.show();
    }

    public void refreshGateway() {
        //final String uciProdName = "uci.env.var.prod_friendly_name";
        final String uciEnv = "uci.env.var.";
        final String uciSSID = "uci.wireless.wifi-iface.@wl0.ssid";
        final String uciSSID5G = "uci.wireless.wifi-iface.@wl1.ssid";
        final String rpcUpTime = "rpc.system.uptime";
        final String rpcHosts = "rpc.hosts.";
        final String rpcWan = "rpc.network.interface.@wan.";
        //Very important for multiple commands in one rpc
        final String connector = "\"" + ",\n" + "\"";
        //连接gateway之前只考虑了一个命令，所以需要给get参数添加双引号
        String cmd = "\"" + uciSSID + connector + uciSSID5G + connector + rpcUpTime + connector
                + uciEnv + connector + rpcHosts + connector + rpcWan + "\",";

        connectGatewayWithCommand("get", cmd, new MainActivity.VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                String strProdName = MainActivity.getRetValueFromResponse(result, uciEnv, "prod_friendly_name");
                String strHwVersion = MainActivity.getRetValueFromResponse(result, uciEnv, "hardware_version");
                String strSSID = MainActivity.getRetValueFromResponse(result, uciSSID);
                String strSSID5G = MainActivity.getRetValueFromResponse(result, uciSSID5G);
                String strUpTime = MainActivity.getRetValueFromResponse(result, rpcUpTime);
                String strWANIP = MainActivity.getRetValueFromResponse(result, rpcWan, "ipaddr");
                // Log.d("LIUYH", result);
                // Log.d("LIUYH", "wan ip is " + strWANIP);

                int uptime = Integer.parseInt(strUpTime);
                TextView tvProdName = (TextView) findViewById(R.id.tvGatewayModel);
                tvProdName.setText("Product Name: " + strProdName);

                TextView tvSSID = (TextView) findViewById(R.id.tvSSID);
                tvSSID.setText("SSID(2.4G): " + strSSID);
                TextView tvSSID5G = (TextView) findViewById(R.id.tvSSID5G);
                tvSSID5G.setText("SSID(5G): " + strSSID5G);
                TextView tvUpTime = (TextView) findViewById(R.id.tvUpTime);
                tvUpTime.setText("UpTime:" + (uptime / 3600) + "hours " + (uptime % 3600) / 60 + "minutes " + uptime % 60 + "seconds");
                TextView tvWanIP = (TextView) findViewById(R.id.tvWanIP);
                tvWanIP.setText("WAN IP: " + strWANIP);

                getHostFromResponse(result);

                Log.d("LIUYH", "Updating host list in authentDiag");
                statusTab.updateHostsList();

            }
        });
    }

    public void showHostDialog(JSONObject jsonHost) {
        AlertDialog.Builder hostDialog = new AlertDialog.Builder(MainActivity.this);
        hostDialog.setTitle(getJsonParamValue(jsonHost, "FriendlyName"));
        String strLayer2Type = getJsonParamValue(jsonHost, "FriendlyName");
        String strConnectionType = strLayer2Type.contains("wl") ? "Wi-Fi" : "Ethernet";
        strConnectionType = "Connection Mode: " + (strLayer2Type.equals("wl0") ? "2.4G Wi-Fi" : "5G Wi-Fi");
        String strIP = "IP Address: " + getJsonParamValue(jsonHost, "IPAddress");
        //Connection time is in second
        int connTime = Integer.parseInt(getJsonParamValue(jsonHost, "ConnectedTime"));
        String strConnTime = "Connection Time: " +  connTime/3600 + " Hours " + connTime%3600/60
                + " Minutes " + connTime%60 + "Seconds ";
        final String[] items = {strConnectionType, strIP, strIP};

        hostDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this,"CLICK: " + items[which],Toast.LENGTH_SHORT).show();
            }
        });

        hostDialog.show();
    }


    public interface VolleyCallback {
        void onSuccess(String result);
    }

    public void authDialog() {
    /*@setView 装入一个EditView
     */
        final EditText editText = new EditText(MainActivity.this);
        editText.setText(strTokenInHeader);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("Input Password").setView(editText);

        inputDialog.setPositiveButton("Connect",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String uciEnv = "uci.env.var.prod_friendly_name";
                        final String rpcWan = "rpc.network.interface.@wan.ipaddr";
                        //Very important for multiple commands in one rpc
                        final String connector = "\"" + ",\n" + "\"";
                        //String cmd = "\"" + uciEnv + "\",";
                        String cmd = "\"" + uciEnv +connector + rpcWan + "\",";
                        // Set password to x-tch-header
                        strTokenInHeader = editText.getText().toString();
                        //final String cmd = uciProdName;
                        //connectGatewayWithCommand(cmd);

                        connectGatewayWithCommand("get", cmd, new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                isConnected = true;
                                String strProdName = getRetValueFromResponse(result, uciEnv);
                                String strWanIP = getRetValueFromResponse(result, rpcWan);

                                TextView tvProd = (TextView) findViewById(R.id.tvProductName);
                                TextView tvWanIP = (TextView) findViewById(R.id.tvWanIP);
                                tvWanIP.setText(strWanIP);
                                // toolbar support

                                if (strWanIP.length() > 0) {
                                    //Wan is up
                                   ImageButton ibWanStatus =  (ImageButton)findViewById(R.id.ibInternetStatus);
                                    ibWanStatus.setImageResource(R.mipmap.internet_connected);
                                }
                                else {
                                    ImageButton ibWanStatus =  (ImageButton)findViewById(R.id.ibInternetStatus);
                                    ibWanStatus.setImageResource(R.mipmap.internet_no_connection);
                                }

                                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                                toolbar.setTitle(/*strProdName + */" Connected");
                                toolbar.setBackgroundColor(Color.rgb(0,200,0));

                                TextView tvProductName = (TextView) findViewById(R.id.tvProductName);
                                tvProductName.setText(strProdName);

                               //Log.d("LIUYH", "body");
                                //Log.d("LIUYH", result);
                                refreshGateway();


                                //Fragment fgStatus = (Fragment) fragmentManager.findFragmentById(R.id.id_content);
                               // fgStatus.
                                //fgStatus.updateHostsList();


                            }
                        });

                    }
                }).show();


    }
}
