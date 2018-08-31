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

public class MainActivityTab extends ListFragment implements SearchView.OnQueryTextListener, GoogleApiClient.OnConnectionFailedListener {
    MenuItem search, add;

    DatabaseReference dref;

    protected static GoogleApiClient mGoogleApiClient;

    ListView listview;
    //  ArrayList<String> list=new ArrayList<>();
    ArrayList<HashMap<String, String>> multiselect_list = new ArrayList<>();
    ArrayList<HashMap<String, String>> list1 = new ArrayList<>();

    private static final String TAG = "MainActivity";

    MenuItem searchMenuItem, delete;
    SearchView searchView;

    SimpleAdapter adapter;
    // ArrayAdapter<String> adapter;

    ActionMode mActionMode;

    FloatingActionButton fab2;

    String name;
    String email;
    Uri photoUrl;

    boolean emailVerified;
    boolean isMultiSelect = false;
    String uid;
    String userId;

    private FirebaseAuth mAuth;

    boolean flag = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_main, container, false);

        listview=(ListView)view.findViewById(android.R.id.list);

        fab2 = (FloatingActionButton)view.findViewById(R.id.add_button2);

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listview.setMultiChoiceModeListener(modeListener);
        listview.setAdapter(adapter);

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add(view);
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            name = user.getDisplayName();
            uid = user.getUid();
        }

        mAuth = FirebaseAuth.getInstance();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("groups");

        dref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Group group = dataSnapshot.getValue(Group.class);
                HashMap<String, String> object = new HashMap<>();
                object.put("name", group.getName());

                list1.add(object);
                // list.add(group.getName());
                // adapter.notifyDataSetChanged();
                refreshAdapter();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                getActivity().finish();
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                //  list.remove(group.getName());

                list1.remove(group.getName());
                refreshAdapter();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String group = (String) parent.getItemAtPosition(position);
                Intent group_activity = new Intent(getActivity(), GroupActivity.class);
                group_activity.putExtra("group_name", group);
                startActivity(group_activity);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {
                /*final String name= list.get(position);
                final int index = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//                alert.setTitle("Alert!");
                alert.setMessage("Are you sure you want to delete group?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Query applesQuery = dref.orderByChild("name").equalTo(name);
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue(); //brisanje iz firebasea
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
                        list.remove(index); //brisanje samo iz arraya, ne iz firebasea
                        adapter.notifyDataSetChanged();

                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();*/
                return true;
            }
        });

    }

    AbsListView.MultiChoiceModeListener modeListener = new AbsListView.MultiChoiceModeListener() {
        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

        }
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.delete_templates_menu, menu);
            isMultiSelect = true;
            mActionMode = actionMode;
            mActionMode.setTitle("Delete groups");
            delete = menu.findItem(R.id.action_delete);
            delete.setVisible(false);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            final ActionMode act = actionMode;
            switch (menuItem.getItemId())
            {
                case R.id.action_delete:
                    AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                    alert.setMessage("Are you sure you want to delete selected groups?");
                    alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeItems(multiselect_list);
                            act.finish();
                            dialog.dismiss();
                        }
                    });
                    alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                    return true;
                default: return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            isMultiSelect = false;
            mActionMode = null;
            multiselect_list.clear();
        }
    };

  /*  @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }*/

    private void refreshAdapter(){
        //adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list)
        adapter = new SimpleAdapter(getActivity().getApplicationContext(), list1,
                R.layout.content_group_item, new String[]{"name"},
                new int[]{R.id.name})
        {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                checkBox.setTag(position);
                if(isMultiSelect)
                {
                    fab2.setVisibility(GONE);
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            int position = (int)compoundButton.getTag();
                            if (multiselect_list.contains(list1.get(position)))
                            {
                                multiselect_list.remove(list1.get(position));
                            }
                            else
                            {
                                multiselect_list.add(list1.get(position));
                            }

                            if (multiselect_list.size() > 0) {
                                if(multiselect_list.size() == 1) mActionMode.setTitle("Delete " + multiselect_list.size() + " group");
                                else mActionMode.setTitle("Delete " + multiselect_list.size() + " groups");
                            }
                            else
                            {
                                mActionMode.setTitle("Delete groups");
                            }


                            if(multiselect_list.size() != 0 )
                            {
                                delete.setVisible(true);
                            }else {
                                delete.setVisible(false);
                            }

                        }
                    });
                }else{
                    checkBox.setOnCheckedChangeListener(null);
                    checkBox.setChecked(false);
                    checkBox.setVisibility(View.GONE);
                    fab2.setVisibility(View.VISIBLE);
                }
                return view;
            }
        };

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                HashMap<String, String> object = (HashMap<String, String>) parent.getItemAtPosition(position);
                String group = object.get("name");
                Intent group_activity = new Intent(getActivity(), GroupActivity.class);
                group_activity.putExtra("group_name", group);
                startActivity(group_activity);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {
               /* final String name= list.get(position);
                final int index = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
//                alert.setTitle("Alert!");
                alert.setMessage("Are you sure you want to delete group?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Query applesQuery = dref.orderByChild("name").equalTo(name);
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue(); //brisanje iz firebasea
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
                        list.remove(index); //brisanje samo iz arraya, ne iz firebasea
                        adapter.notifyDataSetChanged();

                        dialog.dismiss();

                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();*/
                return true;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private String group_name = "";
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
            case R.id.action_logout:
                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getActivity(), Login.class);
                                startActivity(intent);
                            }
                        });

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void add(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Group name:");

        // Set up the input
        final EditText input = new EditText(getContext());

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        builder.setView(input);

        // Set up the buttons
        AlertDialog.Builder next = builder.setPositiveButton("NEXT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                group_name = input.getText().toString();
                if (group_name.isEmpty() || !(group_name.trim().length() > 0)){
                    Toast.makeText(getActivity().getApplicationContext(), "Group name can't be empty. ", Toast.LENGTH_SHORT).show();
                }else {
                    Intent new_group = new Intent(getActivity(), NewGroup.class);
                    new_group.putExtra("group_name", group_name);
                    startActivity(new_group);
                }
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
        input.requestFocus();

    }

    public void templates(View view) {
        Intent templates = new Intent(getActivity(), Templates.class);
        startActivity(templates);
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

    public void removeItems(ArrayList<HashMap<String, String>> selectedItems)
    {
        for(HashMap<String, String> item : selectedItems)
        {
            deleteItem(item);
        }
    }

    public void deleteItem(HashMap<String, String> item)
    {
        final String name = item.get("name");
        Query applesQuery = dref.orderByChild("name").equalTo(name);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    appleSnapshot.getRef().removeValue(); //brisanje iz firebasea
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
        list1.remove(item); //brisanje samo iz arraya, ne iz firebasea
        adapter.notifyDataSetChanged();
    }

}

