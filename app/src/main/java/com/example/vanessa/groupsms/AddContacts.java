package com.example.vanessa.groupsms;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AddContacts extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {
    MenuItem search;
    String group_name;

    DatabaseReference dref;

    private String groupId;

    private ListView mListView;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;

    ArrayList<String> contactList;
    List<String> sublist;

    Cursor cursor;
    int counter;

    MenuItem searchMenuItem;
    SearchView searchView;
    ArrayAdapter<Model> adapter;
    List<Model> list = new ArrayList<Model>();

    ArrayList<String> contacts;
    ArrayList<String> members;
    ArrayList<String> members_now;

    String uid;

    boolean flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        Bundle extras = getIntent().getExtras();
        group_name = extras.getString("group_name");
        setTitle("Add members");

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();

        mListView = (ListView) findViewById(R.id.list_add);
        updateBarHandler =new Handler();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            uid = user.getUid();
        }

        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("groups");

        dref.orderByChild("name").equalTo(group_name).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                int i=0;
                Group group = dataSnapshot.getValue(Group.class);
                members_now = group.getMembers();
//                Log.d("contacts1", members_now.toArray()[0].toString());
                for(int j = 0;j < members_now.size(); j++) {
                    i++;
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });

        flag=false;

        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts();
            }
        }).start();
    }

    public void getContacts() {
        contactList = new ArrayList<String>();
        sublist = new ArrayList<String>();

        String phoneNumber = null;
        String number = null;
        String item = null;

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        StringBuffer output;
        ContentResolver contentResolver = getContentResolver();

        cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {

            counter = 0;
            while (cursor.moveToNext()) {
                output = new StringBuffer();

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = "";

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        if (phoneNumber.startsWith("09") || phoneNumber.startsWith("+3859")){
                            number=phoneNumber.replaceAll("-", "");
                            name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                            flag=true;
                        }
                    }

                    if(flag) {
                        output.append(name);
                        output.append("\n" + number);
                    }
                    flag=false;
                    phoneCursor.close();
                }

                // Add the contact to the ArrayList
                item = output.toString();
                if(!item.isEmpty()) {
                    contactList.add(item);
                    sublist = contactList.subList(1, contactList.size());
                    Collections.sort(sublist);
                }
            }

            // ListView has to be updated using a ui thread
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    contacts = new ArrayList<String>();
                    //sublist.removeAll(members_now);
                    for(String element: sublist){
                        Model m = new Model(element);
                        if(members_now.contains(element)) {
                            m.setSelected(true);
                        }
                        list.add(m);
                        //list.add(new Model(element));
                    }
                    adapter = new MyAdapter(AddContacts.this,list);
                    mListView.setAdapter(adapter);
                    mListView.setOnItemClickListener(AddContacts.this);
                    for(int i = 0; i < members_now.size();i++)
                    {
                        Log.d("cont1", "kontakt" + members_now.get(i) + list.contains(members_now.get(i)) + list.get(i));
                        if(list.contains(members_now.get(i)))
                        {

                            mListView.setItemChecked(i, true);
                        }
                    }
                }
            });

            // Dismiss the progressbar after 500 millisecondds
            updateBarHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 100);
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    public void addMembers(View view) {
        writeNewGroup(group_name);
    }

    private void writeNewGroup(String group_name) {
        members = new ArrayList<String>();

        dref.orderByChild("name").equalTo(group_name).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Group group = dataSnapshot.getValue(Group.class);
                members = group.getMembers();

                for(Model item: list ){
                    if(item.isSelected() && !members.contains(item.getName()))
                    {
                        members.add(item.getName());
                    }
                    else if(!item.isSelected()){
                        if(members.contains(item.getName())) members.remove(item.getName());
                    }
                }
                dataSnapshot.getRef().child("members").removeValue(); //brisanje iz firebasea
                dataSnapshot.getRef().child("members").setValue(members);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
               // Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });

        finish();
        Intent group = new Intent(AddContacts.this, GroupActivity.class);
        group.putExtra("group_name", group_name);
        startActivity(group);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
