package com.example.ashu.zomatotwo;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantFragment extends Fragment {

    //15 Declare reqiuired variable
    RecyclerView recyclerView;
    ArrayList<Restaurant> restaurants;
    MyRecyclerViewAdapter myRecyclerViewAdapter;
    MyTask myTask;
    LinearLayoutManager linearLayoutManager;
    int pos;
    double curlat, curlong;
    int count = 0;
    EditText et;
    Button b;

    public void showPopup(View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.overflowmenu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.map:
                        Intent intent = new Intent(getActivity(), MapsActivity.class);
                        Restaurant restaurant = restaurants.get(pos);
                        intent.putExtra("latitude", restaurant.getLatitude());
                        intent.putExtra("longitude", restaurant.getLongitude());
                        intent.putExtra("name", restaurant.getName());
                        startActivity(intent);
                        break;
                    case R.id.web:
                        Intent intent2 = new Intent(getActivity(), WebFragment.class);
                        startActivity(intent2);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    //14.a)
    public class MyTask extends AsyncTask<String, Void, String> {
        URL myurl;
        HttpURLConnection httpURLConnection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        String line;
        StringBuilder result;

        @Override
        protected String doInBackground(String... string) {
            //20   7 step process
            try {
                myurl = new URL(string[0]);
                httpURLConnection = (HttpURLConnection) myurl.openConnection();
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestProperty("user-key", "e46faab9e2e5d0e8f5d57c4be68c269d");
                httpURLConnection.connect();

                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                line = bufferedReader.readLine();
                result = new StringBuilder();
                while (line != null) {
                    result.append(line);
                    line = bufferedReader.readLine();
                }
                return result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject a = new JSONObject(s);
                JSONArray j = a.getJSONArray("nearby_restaurants");
                for (int i = 0; i < j.length(); i++) {
                    JSONObject k = j.getJSONObject(i);
                    JSONObject p = k.getJSONObject("restaurant");
                    String name = p.getString("name");
                    String thumb = p.getString("thumb");
                    JSONObject m = p.getJSONObject("location");
                    String locality = m.getString("locality");
                    String address = m.getString("address");
                    String latitude = m.getString("latitude");
                    String longitude = m.getString("longitude");

                    Restaurant rest = new Restaurant(name, locality, address, thumb, latitude, longitude);
                    rest.setName(name);
                    rest.setAddress(address);
                    rest.setLocality(locality);
                    rest.setImageUrl(thumb);
                    rest.setLatitude(latitude);
                    rest.setLongitude(longitude);

                    restaurants.add(rest);
                }
                myRecyclerViewAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(s);
        }
    }

    // 14.b
    public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.row, parent, false);
            ViewHolder viewHolder = new ViewHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Restaurant restaurant = restaurants.get(position);
            holder.tv1.setText(restaurant.getName());
            holder.tv2.setText(restaurant.getLocality());
            holder.tv3.setText(restaurant.getAddress());
            holder.overflow.setTag(position);// new code
            Glide.with(getActivity()).load(restaurant.getImageUrl()).placeholder(R.mipmap.ic_launcher).crossFade().into(holder.thumb);
            holder.overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView imageView = (ImageView) v;
                    pos = (int) imageView.getTag();
                    showPopup(v);

                }
            });
        }

        @Override
        public int getItemCount() {
            return restaurants.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView tv1, tv2, tv3;
            public ImageView thumb, overflow;

            public ViewHolder(View itemView) {
                super(itemView);
                tv1 = (TextView) itemView.findViewById(R.id.textViewName_row);
                tv2 = (TextView) itemView.findViewById(R.id.textviewLocality_row);
                tv3 = (TextView) itemView.findViewById(R.id.textviewAddress_row);
                thumb = (ImageView) itemView.findViewById(R.id.imageview_row);
                overflow = (ImageView) itemView.findViewById(R.id.imageview50);

            }
        }
    }


    public RestaurantFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_restaurant, container, false);
        et = (EditText) v.findViewById(R.id.edittext1);
        b = (Button) v.findViewById(R.id.button50);
        recyclerView = (RecyclerView) v.findViewById(R.id.recyclerview1);
        restaurants = new ArrayList<Restaurant>();
        myRecyclerViewAdapter = new MyRecyclerViewAdapter();
        myTask = new MyTask();
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        //17 establish all link
        recyclerView.setAdapter(myRecyclerViewAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //read address
                String address = et.getText().toString();// user given address
                Geocoder geocoder = new Geocoder(getActivity());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(address, 10);
                    Address best = addresses.get(0);
                    curlat = best.getLatitude();
                    curlong = best.getLongitude();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //19
                MainActivity homeActivity = (MainActivity) getActivity();
                if (homeActivity.checkInternet()) {
                    myTask.execute("https://developers.zomato.com/api/v2.1/geocode?lat=" + curlat + "&lon=" + curlong);
                } else
                    Toast.makeText(getActivity(), "CHECK INTERNET CONNECTION", Toast.LENGTH_SHORT).show();
            }
        });

        return v;
    }
}