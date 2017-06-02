package com.imop.admin.cimol;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.imop.admin.entitas.Trans;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

import static com.imop.admin.server.Config.URL_BASE_API;

public class TransAdminAct extends AppCompatActivity {
    static ArrayList<Trans> arrtrans;
    ListView lvtrans;
    String tag = "listrans";
    String url = URL_BASE_API + "/semua_trans.php";
    SessionManager session;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trans);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        HashMap<String, String> user = session.getUserDetails();
        //new GetData().execute(url);
        lvtrans = (ListView) findViewById(R.id.listViewtr);
        listransaksi();
        arrtrans = new ArrayList<Trans>();
        lvtrans.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int i, long id) {
                Trans tt = arrtrans.get(i);
                Trans t = new Trans();
                Bundle b = new Bundle();
                b.putString("id_transaksi", tt.getId_transaksi());
                b.putString("total", tt.getTotal());

                Intent intent = new Intent();
                intent.setClass(v.getContext(), DetailAct.class);
                intent.putExtras(b);
                startActivity(intent);
            }
        });
    }

    private void listransaksi() {
        final ProgressDialog pDialog = ProgressDialog.show(this, null, "Please wait....", true);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //   super.onSuccess(statusCode, headers, response);
                pDialog.dismiss();
                Log.e("TAG", response.toString());
                JSONArray arrBarang = null;
                try {
                    arrBarang = response.getJSONArray("transaksi");
                    for (int i = 0; i < arrBarang.length(); i++) {
                        JSONObject obj = arrBarang.getJSONObject(i);
                        Trans t = new Trans();
                        t.setId_transaksi(obj.getString("id_transaksi"));
                        t.setTanggal(obj.getString("tgl_transaksi"));
                        t.setTotal(obj.getString("total"));
                        t.setStatus(obj.getString("status"));
                        t.setKonf_bay(obj.getString("konf_bay"));
                        t.setNo_resi(obj.getString("no_resi"));
                        arrtrans.add(t);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                AdapterTrans adapter = new AdapterTrans(TransAdminAct.this, R.layout.coba, arrtrans);
//                lvtrans.setAdapter(adapter);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                pDialog.dismiss();

                throwable.printStackTrace();
                Toast.makeText(TransAdminAct.this, "Cek koneksi internet anda!", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
