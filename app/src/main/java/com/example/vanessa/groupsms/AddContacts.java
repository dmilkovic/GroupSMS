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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class AddContacts extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {
    MenuItem search, delete_group;
    String group_name = null;

    DatabaseReference dref;

    private String groupId;
    private String[] orderBy;

    private int checkedOrder = 0;
    private ListView mListView;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;

    ArrayList<String> contactList;
    List<String> sublist;

    Cursor cursor;
    int counter;

    MenuItem searchMenuItem, delete, orderItem;
    SearchView searchView;
    ArrayAdapter<Model> adapter;
    List<Model> list = new ArrayList<Model>();

    ArrayList<String> contacts;
    ArrayList<String> members;
    ArrayList<String> members_now;

    int members_cnt;
    String uid;
    Thread myThread;

    boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        Bundle extras = getIntent().getExtras();
        group_name = extras.getString("group_name");

        try{
            checkedOrder = extras.getInt("order");
        }catch (Exception e)
        {

        }
        setTitle("Add members");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        orderBy = getResources().getStringArray(R.array.contacts_order);

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
        startThread();
    }

    private void startThread()
    {
        // Since reading contacts takes more time, let's run it on a separate thread.
        myThread = new Thread(new Runnable() {

            @Override
            public void run() {
                getContacts(checkedOrder);
            }
        });
        myThread.start();
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

                String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = "";

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

    /*@Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_contacts_menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        delete_group = menu.findItem(R.id.delete_group);
        orderItem = menu.findItem(R.id.order);

        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_group:
                AlertDialog.Builder alert = new AlertDialog.Builder(AddContacts.this);
//                alert.setTitle("Alert!");
                alert.setMessage("Are you sure you want to delete group?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getApplicationContext(), "pozicija: " + position + ", ime: " + name, Toast.LENGTH_SHORT).show();

                        Query applesQuery = dref.orderByChild("name").equalTo(group_name);
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue(); //brisanje iz firebasea
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("TAG", "onCancelled", databaseError.toException());
                            }
                        });
                        dialog.dismiss();
                        Intent intent = new Intent(AddContacts.this, MainActivity.class);
                        startActivity(intent);
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

            case R.id.order:
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(AddContacts.this);
                mBuilder.setTitle("Order contacts by: ");
                mBuilder.setSingleChoiceItems(orderBy,  checkedOrder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       // mResult.setText(listItems[i]);
                        checkedOrder = i;
                        writeNewGroup(group_name, false);
                        finish();
                        Intent contacts = new Intent(AddContacts.this, AddContacts.class);
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

    public void addMembers(View view) {
        writeNewGroup(group_name, true);
    }


    private void writeNewGroup(final String group_name, boolean startNew) {
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
                    Log.d("members", " " + members.size());
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

        for(Model item: list ){
            if(item.isSelected() && !members.contains(item.getName()))
            {
                members.add(item.getName());
            }
            else if(!item.isSelected()){
                if(members.contains(item.getName())) members.remove(item.getName());
            }
            Log.d("members", " " + members.size());
        }

        if(members.size() == 0){
            Toast.makeText(getApplicationContext(), "Add at least one contact", Toast.LENGTH_SHORT).show();
            return;
        }

        if(startNew)
        {
            GroupActivity.activity.finish();
            finish();
            Intent group = new Intent(AddContacts.this, GroupActivity.class);
            group.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            group.putExtra("group_name", group_name);
            startActivity(group);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
