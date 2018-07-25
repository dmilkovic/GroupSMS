package com.example.vanessa.groupsms;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
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
import java.util.HashMap;

public class ChooseTemplate extends AppCompatActivity implements SearchView.OnQueryTextListener {
    MenuItem search, add;

    DatabaseReference dref;
    DatabaseReference dref2;

    ListView lv;
    ArrayList<HashMap<String, String>> list = new ArrayList<>();
    private static final String TAG = "MainActivity";

    MenuItem searchMenuItem;
    SearchView searchView;

    private SimpleAdapter adapter;

    String content=null;
    String group_name=null;

    EditText edit_content;
    String message;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    ArrayList<String> contacts=new ArrayList<>();
    ArrayList<String> numbers=new ArrayList<>();

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_template);
        setTitle("Choose template");

        Bundle extras = getIntent().getExtras();
        group_name = extras.getString("group_name");

        lv = (ListView) findViewById(R.id.list);
       /* adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);*/

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates");
        dref2=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("groups");

        dref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Template template = dataSnapshot.getValue(Template.class);

                HashMap<String, String> object = new HashMap<>();
                object.put("name", template.getTitle());
                object.put("message", template.getContent());
                Log.d("object", object.get("name") + object.get("message"));
                //list.add("name", template.getTitle());
                list.add(object);
                ///list.add(template.getContent());
                refreshAdapter();
                //adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Template template = dataSnapshot.getValue(Template.class);
                list.remove(template.getTitle());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

     /*   listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                final String title = (String) parent.getItemAtPosition(position);

                dref.orderByChild("title").equalTo(title).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Template template = dataSnapshot.getValue(Template.class);
                        content = template.getContent();

                        showTemplate(content);
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
                    }
                });

            }
        });
*/
        dref2.orderByChild("name").equalTo(group_name).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Group group = dataSnapshot.getValue(Group.class);
                contacts = group.getMembers();
                int i =0;
                for(int j=0;j<contacts.size(); j++) {
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
            }
        });
    }

    private void refreshAdapter(){
        adapter = new SimpleAdapter(this.getApplicationContext(), list,
                R.layout.contetnt_template, new String[]{"name", "message"},
                new int[]{R.id.name, R.id.message});
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> object = (HashMap<String, String>) parent.getItemAtPosition(position);
                final String title = object.get("name");
                dref.orderByChild("title").equalTo(title).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Template template = dataSnapshot.getValue(Template.class);
                        content = template.getContent();

                        showTemplate(content);
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
                    }
                });

            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View arg1, int position, long id)  {

                HashMap<String, String> object = (HashMap<String, String>) parent.getItemAtPosition(position);
                final String template= object.get("name");
                final int index = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(ChooseTemplate.this);
//                alert.setTitle("Alert!");
                alert.setMessage("Are you sure you want to delete this template?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(getApplicationContext(), "pozicija: " + position + ", ime: " + name, Toast.LENGTH_SHORT).show();

                        Query applesQuery = dref.orderByChild("title").equalTo(template);
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
                alert.show();
                return true;
            }
        });
    }
    String new_content="";

    public void showTemplate(String content){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        edit_content = (EditText) dialogView.findViewById(R.id.edit1);

        edit_content.setText(content);

        dialogBuilder.setTitle("Write SMS to " + group_name + ":");

        edit_content.setSingleLine(false);
        edit_content.setLines(4);
        edit_content.setMaxLines(5);
        edit_content.setGravity(Gravity.LEFT | Gravity.TOP);
        edit_content.setHorizontalScrollBarEnabled(false);
        dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                new_content = edit_content.getText().toString();

                sendMessage(new_content);

                Intent group = new Intent(ChooseTemplate.this, GroupActivity.class);
                group.putExtra("group_name", group_name);
                startActivity(group);

            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void sendMessage(String new_content){
        SmsManager sms = SmsManager.getDefault();
        message = new_content;
        //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
        for(int i=0;i<contacts.size();i++){
            if(contacts.get(i)==null) continue;
            String[] parts = contacts.get(i).split("\n");
            numbers.add(parts[1]);
        }

        for(String number: numbers) {
            sms.sendTextMessage(number, null, message, null, null);
        }
        Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
//        SmsManager smsManager = SmsManager.getDefault();
//        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
//
//        for(int i=0;i<contacts.size();i++){
//            if(contacts.get(i)==null) continue;
//            String[] parts = contacts.get(i).split("\n");
//            numbers.add(parts[1]);
//        }
//
//        for(String number: numbers) {
//            smsManager.sendTextMessage(number, null, message, null, null);
//        }
//        Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
//        return;
//    }

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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
            default:
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
}
