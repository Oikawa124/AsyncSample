package com.example.asyncsample;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 画面部品のListViewを取得
        ListView lvCityList = findViewById(R.id.lvCityList);

        // SimpleAdapterで使用するListオブジェクトを用意
        List<Map<String, String>> cityList = new ArrayList<>();

        // 都市データを格納するMapオブジェクトの用意とcityListへのデータ登録
        Map<String, String> city = new HashMap<>();

        city.put("name", "大阪");
        city.put("id", "270000");
        cityList.add(city);

        city = new HashMap<>();
        city.put("name", "神戸");
        city.put("id", "280010");
        cityList.add(city);

        // SimpleAdapterで使用するfrom to用変数の用意
        String[] from = {"name"};
        int[] to = {android.R.id.text1};

        // SimpleAdapterを生成
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, cityList,
                android.R.layout.simple_list_item_1, from, to);

        // ListViewにSimpleAdapterを設定
        lvCityList.setAdapter(adapter);

        // ListViewにリスナを設定
        lvCityList.setOnItemClickListener(new ListItemClickListener());
    }

    private class ListItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String cityName = item.get("name");
            String cityID = item.get("id");

            TextView tvCityName = findViewById(R.id.tvCityName);
            tvCityName.setText(cityName + "の天気:");

            // 天気情報を表示するtvを取得
            TextView tvWeatherTelop = findViewById(R.id.tvWeatherTelop);

            // 天気詳細を表示するtvを取得
            TextView tvWeatherDesc = findViewById(R.id.tvWeatherDesc);

            // WeatherInfoReceiverをnew
            WeatherinfoReceiver receiver = new WeatherinfoReceiver(tvWeatherTelop, tvWeatherDesc);

            // WeatherInfoReceiverを実行
            receiver.execute(cityID);

        }
    }

    private class WeatherinfoReceiver extends AsyncTask<String, String, String> {

        // 現在の天気を表示する画面部品フィールド
        private TextView _tvWeatherTrlop;

        // 天気の詳細を表示する画面部品のフィールド
        private TextView _tvWeatherDesc;

        // コンストラクタ
        // お天気情報を表示する画面部品をあらかじめ取得してフィールドに格納
        public WeatherinfoReceiver(TextView tvWeatherTelop, TextView tvWeatherDesc) {
            _tvWeatherTrlop = tvWeatherTelop;
            _tvWeatherDesc = tvWeatherDesc;
        }


        // 非同期で行いたい処理を記述する
        @Override
        protected String doInBackground(String... params) {
            // 可変長引数の1個め（インデックス0）を取得、都市のID
            String id = params[0];

            // 都市のIdを使って接続URL文字列を作成
            String urlStr = "http://weather.livedoor.com/forecast/webservice/json/v1?city=" + id;

            // 天気情報サービスから取得したJSON文字列

            String result = "";

            // ここに上記URLに接続してJSON文字列を取得する処理を書く

            HttpURLConnection con = null;

            InputStream is = null;

            try{

                // URLオブジェクトを生成
                URL url = new URL(urlStr);

                // URLオブジェクトからHttpURLconnectionオブジェクトを取得
                con = (HttpURLConnection) url.openConnection();

                // Http接続メソッドを設定
                con.setRequestMethod("GET");

                // 接続
                con.connect();

                // レスポンスデータを取得
                is = con.getInputStream();

                // レスポンスデータから文字列を取得
                result = is2String(is);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                if (con != null) {
                    con.disconnect();
                }

                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // JSON文字列を返す
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // 天気情報用文字列変数を用意
            String Telop = "";
            String desc = "";

            // ここにJSON文字列を解析する処理を記述

            try {
                // JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする
                JSONObject rootJSON = new JSONObject(result);

                JSONObject descriptionJSON = rootJSON.getJSONObject("description");
                desc = descriptionJSON.getString("text");

                JSONArray forecasts = rootJSON.getJSONArray("forecasts");

                JSONObject forecastNow = forecasts.getJSONObject(0);

                Telop = forecastNow.getString("telop");

            } catch (JSONException e) {
                e.printStackTrace();
            }


            // 天気情報用文字列をTextViewにセット。
            _tvWeatherTrlop.setText(Telop);
            _tvWeatherDesc.setText(desc);
        }

        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];

            int line;
            while (0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }
    }

}
