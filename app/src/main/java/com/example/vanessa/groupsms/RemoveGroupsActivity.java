package com.example.vanessa.groupsms;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.GONE;

public class RemoveGroupsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, GoogleApiClient.OnConnectionFailedListener {
    MenuItem search, add;

    DatabaseReference dref;

    protected static GoogleApiClient mGoogleApiClient;

    ListView listview;
    //  ArrayList<String> list=new ArrayList<>();
    ArrayList<HashMap<String, String>> multiselect_list = new ArrayList<>();
    ArrayList<HashMap<String, String>> list1 = new ArrayList<>();
    ArrayList<String> new_id = new ArrayList<String>();
    ArrayList<String> existingGroups = new ArrayList<>();

    private static final String TAG = "MainActivity";

    MenuItem searchMenuItem, delete;
    SearchView searchView;

    SimpleAdapter adapter;
    FloatingActionButton fabDone;

    String name;
    String uid;

    private FirebaseAuth mAuth;
    private Intent intent;

    boolean flag = false;

    String templateID;

    private Template template;
    private ArrayList<String> groups;
    private ArrayList<Group> groupsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setTitle("Remove Groups");
        intent = getIntent();
        Bundle extras = intent.getExtras();
        templateID = extras.getString("id");

        setContentView(R.layout.activity_add_group);
        //View view = inflater.inflate(R.layout.activity_main, container, false);

        groups = new ArrayList<String>();
        groupsArrayList = new ArrayList<Group>();

        listview=(ListView)findViewById(android.R.id.list);

        fabDone = (FloatingActionButton)findViewById(R.id.fab_done);
        //promijeni ikonu na ikonu za brisanje
        fabDone.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_delete));

        listview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listview.setItemsCanFocus(false);
        listview.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            name = user.getDisplayName();
            uid = user.getUid();
        }

        mAuth = FirebaseAuth.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates");

        Query applesQuery = dref.orderByChild("id").equalTo(templateID);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                   template =  appleSnapshot.getValue(Template.class);
                   groups = template.getGroups();
                   dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("groups");
                   for (String group:template.getGroups())
                   {
                       Query groupQuery = dref.orderByKey().equalTo(group);
                       groupQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(DataSnapshot dataSnapshot) {
                               for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                   Group group = appleSnapshot.getValue(Group.class);
                                   groupsArrayList.add(group);
                                   HashMap<String, String> object = new HashMap<>();
                                   object.put("name", group.getName());
                                   object.put("id", group.getId());
                                   Log.d("grupa", group.getName() + list1.size());
                                   list1.add(object);
                                   new_id.add(group.getId());
                                   // adapter.notifyDataSetChanged();
                                   refreshAdapter();
                               }
                           }
                           @Override
                           public void onCancelled(DatabaseError databaseError) {
                               Log.e(TAG, "onCancelled", databaseError.toException());
                           }
                       });
                   }
               }
           }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String group = (String) parent.getItemAtPosition(position);
                Intent group_activity = new Intent(getApplicationContext(), GroupActivity.class);
                group_activity.putExtra("group_name", group);
                startActivity(group_activity);
            }
        });

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("GROUP1", new_id.size() + "SIZE" + new_id.toArray());
                dref = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates").child(templateID).child("groups");
                dref.setValue(new_id);
              /*  Bundle bundle = new Bundle();
                bundle.putSerializable("new_groups", new_id);*/
            //    intent.putExtras();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }
  /*  @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }*/

    private void refreshAdapter(){
        //adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list)
        adapter = new SimpleAdapter(this.getApplicationContext(), list1,
                R.layout.content_group_item, new String[]{"name"},
                new int[]{R.id.name})
        {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                checkBox.setTag(position);
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        int position = (int)compoundButton.getTag();
                        if (multiselect_list.contains(list1.get(position)))
                        {
                            multiselect_list.remove(list1.get(position));
                            new_id.add(list1.get(position).get("id"));
                        }
                        else
                        {
                            multiselect_list.add(list1.get(position));
                            new_id.remove(list1.get(position).get("id"));
                            Log.d("GROUP1", list1.get(position).get("id"));
                        }
                    }
                });
               /* }else{
                    checkBox.setOnCheckedChangeListener(null);
                    checkBox.setChecked(false);
                    checkBox.setVisibility(View.GONE);
                    fab2.setVisibility(View.VISIBLE);
                }*/
                return view;
            }
        };

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> object = (HashMap<String, String>) parent.getItemAtPosition(position);
                String group = object.get("name");
                Intent group_activity = new Intent(RemoveGroupsActivity.this, GroupActivity.class);
                group_activity.putExtra("group_name", group);
                startActivity(group_activity);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.add_remove_groups_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private String group_name = "";
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



}

