package com.example.vanessa.groupsms;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NewGroup extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {
    MenuItem search;
    String group_name;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String groupId;
    private String[] orderBy;

    private ListView mListView;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;

    ArrayList<String> contactList;
    List<String> sublist;

    Cursor cursor;
    int counter;
    private int checkedOrder = 0;

    MenuItem searchMenuItem, orderItem;
    SearchView searchView;

    ArrayAdapter<Model> adapter;
    List<Model> list = new ArrayList<Model>();

    ArrayList<String> contacts;

    String uid;
    boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        Bundle extras = getIntent().getExtras();
        group_name = extras.getString("group_name");
        setTitle("Add members");

        try{
            checkedOrder = extras.getInt("order");
        }catch (Exception e)
        {

        }

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Reading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();

        orderBy = getResources().getStringArray(R.array.contacts_order);


        mListView = (ListView) findViewById(R.id.list);
        updateBarHandler =new Handler();

        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts(checkedOrder);
            }
        }).start();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }
        flag=false;

    }
    public void getContacts(int order) {
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
                String name="";
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contact_id}, null);

                    while (phoneCursor.moveToNext()) {
                        output = new StringBuffer();
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        number = phoneNumber.replaceAll("-", "").replaceAll("\\s+", "");

                        if(order == 0)
                        {
                            name = cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME ));
                        }else
                        {
                            name = cursor.getString(cursor.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME_ALTERNATIVE ));
                        }
                        //  Log.d("con", "Name1: " + name);
                        //   Log.d("con", "Phone Number1: " + phoneNumber);
                        flag=true;

                        output.append(name);
                        output.append("\n" + number);

                        item = output.toString();
                        if(!item.isEmpty() && !contactList.contains(item)) {
                            contactList.add(item);
                            sublist = contactList.subList(1, contactList.size());
                            Collections.sort(sublist);
                        }
                    }
                    phoneCursor.close();
                }

            }

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    contacts = new ArrayList<String>();
                    //sublist.removeAll(members_now);
                    for(String element: sublist){
                        Model m = new Model(element);
                        list.add(m);
                        //list.add(new Model(element));
                    }

                    adapter = new MyAdapter(NewGroup.this,list);
                    mListView.setAdapter(adapter);
                    mListView.setOnItemClickListener(NewGroup.this);
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

        orderItem = menu.findItem(R.id.order);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.order:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(NewGroup.this);
                mBuilder.setTitle("Order contacts by: ");
                mBuilder.setSingleChoiceItems(orderBy,  checkedOrder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // mResult.setText(listItems[i]);
                        checkedOrder = i;
                        writeNewGroup(group_name);
                        finish();
                        Intent contacts = new Intent(NewGroup.this, AddContacts.class);
                        contacts.putExtra("order", checkedOrder);
                        contacts.putExtra("group_name", group_name);
                        startActivity(contacts);
                    }
                });
                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
                return true;

            case android.R.id.home:
                this.finish();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    public void createGroup(View view) {
        writeNewGroup(group_name);
    }

    private void writeNewGroup(String group_name) {
        mFirebaseDatabase=FirebaseDatabase.getInstance().getReference().child("users").child(uid);
        groupId= mFirebaseDatabase.push().getKey();

        contacts = new ArrayList<String>();
        for(Model item: list ){
            if(item.isSelected()) contacts.add(item.getName());
        }

        if(contacts.size() == 0){
            Toast.makeText(getApplicationContext(), "Add at least one contact", Toast.LENGTH_SHORT).show();
            return;
        }
        Group group = new Group(group_name,contacts, groupId);
        mFirebaseDatabase.child("groups").child(groupId).setValue(group);
        mFirebaseDatabase.child(group_name);

        Toast.makeText(getApplicationContext(), "New group: " + group_name, Toast.LENGTH_SHORT).show();

        finish();
        Intent main = new Intent(NewGroup.this, MainActivity.class);
        startActivity(main);
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
    public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
    }

}
