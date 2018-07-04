package com.example.vanessa.groupsms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
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

public class GroupActivity extends AppCompatActivity {
    MenuItem add, rename, delete;

    DatabaseReference dref;
    DatabaseReference dref2;

    ListView listview;
    ArrayList<String> list=new ArrayList<>();
    ArrayList<String> contacts=new ArrayList<>();
    String group_name=null;
    String new_name=null;

    private FloatingActionButton fab;
    private FloatingActionButton fab2;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    String message;
    ArrayList<String> numbers=new ArrayList<>();

    String uid;

    String temp;

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        Bundle extras = getIntent().getExtras();
        group_name = extras.getString("group_name");
        setTitle(group_name);

        listview=(ListView)findViewById(R.id.list2);
        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listview.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            uid = user.getUid();
        }

        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("groups");

        fab = (FloatingActionButton) findViewById(R.id.floating_button);
        fab2 = (FloatingActionButton) findViewById(R.id.button_choose);


        dref.orderByChild("name").equalTo(group_name).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Group group = dataSnapshot.getValue(Group.class);
                Template templ = dataSnapshot.getValue(Template.class);

                contacts = group.getMembers();

                for(int i=0;i<contacts.size();i++){
                    if(contacts.get(i)==null) continue;
                    list.add(contacts.get(i));
                    adapter.notifyDataSetChanged();
                }
                if(contacts.size()<1) {
                    fab.setVisibility(View.GONE);
                }else{
                    fab.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                contacts = group.getMembers();
                list.clear();
                for(int i=0;i<contacts.size();i++){
                    if(contacts.get(i)==null) continue;
                    list.add(contacts.get(i));
                    adapter.notifyDataSetChanged();
                }
                if(contacts.size()<1) {
                    fab.setVisibility(View.GONE);
                }else{
                    fab.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long arg3) {
                final String name= list.get(position);
                final int index = position;
                Toast.makeText(getApplicationContext(), "ime: " + name + ", poziciija: " + position, Toast.LENGTH_SHORT).show();

                AlertDialog.Builder alert = new AlertDialog.Builder(GroupActivity.this);
                alert.setMessage("Are you sure you want to delete this contact?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final Query applesQuery = dref.orderByChild("name").equalTo(group_name);
                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().child("members").removeValue(); //brisanje iz firebasea
                                    list.remove(index);
                                    appleSnapshot.getRef().child("members").setValue(list);
                                    adapter.notifyDataSetChanged();
                                }
                                if(list.size()<1) {
                                    fab.setVisibility(View.GONE);
                                }else{
                                    fab.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
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
                return false;
            }
        });

        dref2 = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        dref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("templates")) {
                    fab2.setVisibility(View.VISIBLE);
                }else{
                    fab2.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp(){
        Intent main = new Intent(GroupActivity.this, MainActivity.class);
        startActivity(main);
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent main = new Intent(GroupActivity.this, MainActivity.class);
        startActivity(main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        add = menu.findItem(R.id.add_contacts);
        rename = menu.findItem(R.id.rename_group);
        delete = menu.findItem(R.id.delete_group);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_contacts:
                final Intent new_group = new Intent(GroupActivity.this, AddContacts.class);
                new_group.putExtra("group_name", group_name);
                startActivity(new_group);

                return true;

            case R.id.rename_group:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Group name:");

                // Set up the input
                final EditText input = new EditText(this);
                input.setText(group_name);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                builder.setView(input);

                // Set up the buttons
                AlertDialog.Builder next = builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new_name = input.getText().toString();
                        if (new_name.isEmpty() || !(new_name.trim().length() > 0)){
                            Toast.makeText(getApplicationContext(), "Group name can't be empty. ", Toast.LENGTH_SHORT).show();
                            new_name=group_name;
                        }else{
                            final Query applesQuery = dref.orderByChild("name").equalTo(group_name);
                            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                        appleSnapshot.getRef().child("name").setValue(new_name); //brisanje iz firebasea
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e(TAG, "onCancelled", databaseError.toException());
                                }
                            });
                        }
                        Intent intent = new Intent(GroupActivity.this, GroupActivity.class);
                        intent.putExtra("group_name", new_name);
                        startActivity(intent);
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

                return true;

            case R.id.delete_group:

                AlertDialog.Builder alert = new AlertDialog.Builder(GroupActivity.this);
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
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
                        dialog.dismiss();
                        Intent intent = new Intent(GroupActivity.this, MainActivity.class);
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

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void writeSMS(View view){
        showChangeLangDialog();
    }

    public void templates(View view){
        Intent choose_template = new Intent(GroupActivity.this, ChooseTemplate.class);
        choose_template.putExtra("group_name", group_name);

        startActivity(choose_template);
    }


    public void showChangeLangDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);

        edt.setSingleLine(false);
        edt.setLines(4);
        edt.setMaxLines(5);
        edt.setGravity(Gravity.LEFT | Gravity.TOP);
        edt.setHorizontalScrollBarEnabled(false);
        dialogBuilder.setTitle("Write SMS to " + group_name);
        dialogBuilder.setMessage("Enter text below:");
        dialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                sendMessage(edt);
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

    public void sendMessage(EditText edt){
        message = edt.getText().toString();
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        SmsManager smsManager = SmsManager.getDefault();

        for(int i=0;i<contacts.size();i++){
            if(contacts.get(i)==null) continue;
            String[] parts = contacts.get(i).split("\n");
            numbers.add(parts[1]);
        }
        for(String number: numbers) {
            smsManager.sendTextMessage(number, null, message, null, null);
        }
        Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        return;
    }

}
