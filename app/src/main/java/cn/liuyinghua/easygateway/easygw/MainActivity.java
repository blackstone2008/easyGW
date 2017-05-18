package cn.liuyinghua.easygateway.easygw;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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

import android.content.DialogInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    ArrayList<String> respStringList = new ArrayList<String>();
    Map<String, JSONObject> jsonMapHostName = new HashMap<String, JSONObject>();
    Map<String, String> strHostName = new HashMap<String, String>();
    List<String> arHostName = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button btRefreshData = (Button) findViewById(R.id.btRefresh);
        btRefreshData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                //final String uciProdName = "uci.env.var.prod_friendly_name";
                final String uciEnv = "uci.env.var.";
                final String uciSSID = "uci.wireless.wifi-iface.@wl0.ssid";
                final String rpcUpTime = "rpc.system.uptime";
                final String rpcHosts = "rpc.hosts.";
                //Very important for multiple commands in one rpc
                final String connector = "\"" + ",\n" + "\"";
                //连接gateway之前只考虑了一个命令，所以需要给get参数添加双引号
                String cmd = uciSSID + connector + rpcUpTime + connector + uciEnv + connector + rpcHosts;
                //final String cmd = uciProdName;
                //connectGatewayWithCommand(cmd);

                getStringFromGateway(cmd, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        String strProdName = getRetValueFromResponse(result, uciEnv, "prod_friendly_name");
                        String strHwVersion = getRetValueFromResponse(result, uciEnv, "hardware_version");
                        String strSSID = getRetValueFromResponse(result, uciSSID);
                        String strUpTime = getRetValueFromResponse(result, rpcUpTime);

                        int uptime = Integer.parseInt(strUpTime);
                        TextView tvProdName = (TextView) findViewById(R.id.tvGatewayModel);
                        tvProdName.setText("Product Name: " + strProdName);
                        TextView tvHWVersion = (TextView) findViewById(R.id.tvHWVersion);
                        tvHWVersion.setText("Hardware Version: " + strHwVersion);
                        TextView tvSSID = (TextView) findViewById(R.id.tvSSID);
                        tvSSID.setText("SSID: " + strSSID);
                        TextView tvUpTime = (TextView) findViewById(R.id.tvUpTime);
                        tvUpTime.setText("UpTime:" + (uptime / 3600) + "hours " + (uptime % 3600) / 60 + "minutes " + uptime % 60 + "seconds");
                        getHostFromResponse(result);
                    }
                });
            }
        });

        Button btHost = (Button) findViewById(R.id.btHostInfo);
        btHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showClickMessage("test");
                showListDialog();
            }
        });

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
    public String getRetValueFromResponse(String jsonStr, String paramPath) {

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
    public String getRetValueFromResponse(String jsonStr, String paramPath, String paraName) {

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

    public JSONObject getJsonFromResponse(String jsonStr, String paramPath) {
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

    public String getStringFromGateway(final String path, final VolleyCallback callback) {

        String retStr;

        final long startTime = System.nanoTime();

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = "http://192.168.1.1/api";

        String body = new String("{\n" +
                "  \"command\":\"get\",\n" +
                "  \"data\":[\n" +
                "    \"" + path + "\",\n" +
                "  ]\n" +
                "}\n");

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
                params.put("X-tch-token", "123456");
                return params;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    if (requestBody != null) {
                        String body = new String(requestBody.getBytes());
                        Log.d("LIUYH", "get body by getBody ==> " + body);
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
        for (int i = 0; i < 50; i++) {
            String rpcHostPath = String.format("rpc.hosts.host.%d.", i);
            JSONObject jsonObj = getJsonFromResponse(result, rpcHostPath);
            if (jsonObj != null) {
                // return json obj
                String strState = null;
                try {
                    strState = jsonObj.getString("State");
                    if (strState.equals("0"))
                        continue;
                    //State = 1, this is an active host

                    JSONObject jsonHostFriendlyName = jsonObj.getJSONObject("FriendlyName");
                    String strHostFriendlyName = jsonHostFriendlyName.getString("value");

                    JSONObject jsonIPAddress = jsonObj.getJSONObject("IPAddress");
                    String strIP = jsonIPAddress.getString("value");

                    strHostInfo += strHostFriendlyName + " : " + strIP + "\n";
                    //for displaying in diaglog
                    strHostName.put(strHostFriendlyName, rpcHostPath);
                    jsonMapHostName.put(strHostFriendlyName, jsonObj);
                    arHostName.add(strHostFriendlyName);
                    if (activeHost++ >= hostNum)
                        break;
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("EasyGateway", "Cannot create json object " + e.getMessage());
                }

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
        int size = arHostName.size();
        final String[] items = (String[]) arHostName.toArray(new String[size]);
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle(size + "Active Hosts Information");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // which 下标从0开始
                // ...To-do
                //jsonMapHostName.put(strHostFriendlyName, jsonObj);  => here we can get the jsonObject of host and display all information.
                Toast.makeText(MainActivity.this,
                        "你点击了" + items[which],
                        Toast.LENGTH_SHORT).show();
            }
        });
        listDialog.show();
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }
}
