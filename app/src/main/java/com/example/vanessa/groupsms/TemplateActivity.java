package com.example.vanessa.groupsms;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ActionMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.fasterxml.jackson.core.JsonProcessingException;
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

public class TemplateActivity extends AppCompatActivity  {
    MenuItem search, add;

    DatabaseReference dref;

    private int identifierAdd = 3, identifierRemove = 4 ;

    private GoogleApiClient mGoogleApiClient;

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

    FloatingActionButton addGroup, deleteGroup;

    String name;
    String email;
    Uri photoUrl;
    EditText textView;

    boolean emailVerified;
    boolean isMultiSelect = false;
    String uid;
    String userId, templateId;

    private FirebaseAuth mAuth;

    Button send;
    ImageButton saveButton;

    Template template;
    private ArrayList<String> groups;
    private ArrayList<Group> groupsArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_template);

        Bundle extras = getIntent().getExtras();
        templateId = extras.getString("id");

        textView = (EditText)findViewById(R.id.edit_text);
        int lineheight = textView.getLineHeight();
     //   textView.setMaxHeight(5*lineheight);
        textView.setHeight(5*lineheight);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        addGroup = (FloatingActionButton)findViewById(R.id.button_add);
        deleteGroup = (FloatingActionButton)findViewById(R.id.button_remove);

        addGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent group_activity = new Intent(TemplateActivity.this, AddGroups.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("groups", groups);
                group_activity.putExtras(bundle);
                startActivityForResult(group_activity, identifierAdd);
            }
        });

        deleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent group_activity = new Intent(TemplateActivity.this, RemoveGroupsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("id", templateId);
                group_activity.putExtras(bundle);
                startActivityForResult(group_activity, identifierRemove);
            }
        });

       /* listview=(ListView)findViewById(R.id.list);
        listview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listview.setMultiChoiceModeListener(modeListener);

        fab1 = (FloatingActionButton)findViewById(R.id.button1);
        fab2 = (FloatingActionButton)findViewById(R.id.add_button2);

        // adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listview.setAdapter(adapter);
*/
        listview=(ListView)findViewById(android.R.id.list);

        send = (Button)findViewById(R.id.button_send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToGroups();
            }
        });

        saveButton = (ImageButton) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveContent();
            }
        });

        groups = new ArrayList<String>();
        groupsArrayList = new ArrayList<Group>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            name = user.getDisplayName();
            uid = user.getUid();
        }

        mAuth = FirebaseAuth.getInstance();

        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates");

      /*  dref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Template t = dataSnapshot.getValue(Template.class);
                HashMap<String, String> object = new HashMap<>();
                object.put("id", t.id);
                if(t.id.matches(templateId)) textView.setText(t.getTitle());
                list1.add(object);
                // list.add(group.getName());
                Log.d("data", list1.toArray()[0].toString());
                // adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                finish();
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                //  list.remove(group.getName());

                list1.remove(group.getName());
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/

        Query applesQuery = dref.orderByChild("id").equalTo(templateId);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    template =  appleSnapshot.getValue(Template.class);
                    setTitle(template.getTitle());
                    groups = template.getGroups();
                    textView.setText(template.getContent());
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
                                    Log.d("grupa", group.getName() + list1.size());
                                    list1.add(object);
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

       /* dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("groups");
        for (String group:template.getGroups())
        {
            applesQuery = dref.equalTo(group);
            applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                        Log.d("grupa", appleSnapshot.getValue(Group.class).getName());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "onCancelled", databaseError.toException());
                }
            });
        }*/
       refreshAdapter();
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    ArrayList<String> numbers=new ArrayList<>();
    private void sendToGroups()
    {
        ArrayList<String> contacts=new ArrayList<>();
        ArrayList<String> alreadyAddedContacts = new ArrayList<>();

        for(Group group : groupsArrayList)
        {
            contacts = group.getMembers();
            SmsManager sms = SmsManager.getDefault();
            //message = new_content;
            //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            for(int i=0;i<contacts.size();i++){
                if(contacts.get(i)==null) continue;
                String[] parts = contacts.get(i).split("\n");
                numbers.add(parts[1]);
            }

            for(String number: numbers) {
                if(alreadyAddedContacts.contains(number)) continue;
                sms.sendTextMessage(number, null, textView.getText().toString(), null, null);
                alreadyAddedContacts.add(number);
                Toast.makeText(getApplicationContext(), "SMS sent to group:" + group.getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.template_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_title:
                editTitle();
                return true;
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refreshAdapter(){
        //adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list)
        adapter = new SimpleAdapter(this.getApplicationContext(), list1,
                R.layout.content_group_item, new String[]{"name"},
                new int[]{R.id.name})
        {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
               /* CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
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
                }*/
                return view;
            }
        };

        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                HashMap<String, String> object = (HashMap<String, String>) parent.getItemAtPosition(position);
                String group = object.get("name");
                Intent group_activity = new Intent(TemplateActivity.this, GroupActivity.class);
                group_activity.putExtra("group_name", group);
                startActivity(group_activity);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == identifierAdd) {
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                if((ArrayList<String>)bundle.getSerializable("new_groups") != null) {
                    groups.addAll((ArrayList<String>)bundle.getSerializable("new_groups"));
                    for (String group:groups)
                    {
                        Log.d("GROUP1", group);
                        Query groupQuery = dref.orderByKey().equalTo(group);
                        groupQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    Group group = appleSnapshot.getValue(Group.class);
                                    groupsArrayList.add(group);
                                    HashMap<String, String> object = new HashMap<>();
                                    object.put("name", group.getName());
                                    Log.d("grupa", "Intent " + group.getName() + list1.size());
                                    // list.add(group.getName());
                                   // list1.add(object);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e(TAG, "onCancelled", databaseError.toException());
                            }
                        });
                    }
                    dref = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates").child(templateId).child("groups");
                    dref.setValue(groups);
                }else{
                    //myObjects.remove(position);
                }
            }
            //adapter.notifyDataSetChanged();
            finish();
            Intent group_activity = new Intent(TemplateActivity.this, TemplateActivity.class);
            group_activity.putExtra("id", templateId);
            startActivity(group_activity);
        }else if(requestCode == identifierRemove)
        {
            if (resultCode == RESULT_OK) {
                finish();
                Intent group_activity = new Intent(TemplateActivity.this, TemplateActivity.class);
                group_activity.putExtra("id", templateId);
                startActivity(group_activity);
            }
        }
    }

    private void saveContent()
    {
        final String new_content = textView.getText().toString();
        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates");
        final Query applesQuery = dref.orderByChild("id").equalTo(templateId);
        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    for(HashMap<String, String> object : Templates.list)
                    {
                        if(object.get("id").matches(templateId))
                        {
                            HashMap<String, String> newObject = new HashMap<>();
                            newObject.put("name", template.getTitle());
                            newObject.put("message", new_content);
                            newObject.put("id", templateId);
                            Templates.list.set(Templates.list.indexOf(object), newObject);
                        }
                    }
                    appleSnapshot.getRef().child("content").setValue(new_content); //brisanje iz firebasea
                    Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    private void editTitle()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Template title:");

        // Set up the input
        final EditText input = new EditText(getApplicationContext());
        input.setTextColor(Color.BLACK);
        input.setText(template.getTitle());

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        builder.setView(input);

        // Set up the buttons
        AlertDialog.Builder next = builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String title = input.getText().toString();
                if (title.isEmpty() || !(title.trim().length() > 0)){
                    Toast.makeText(getApplication().getApplicationContext(), "Template name can't be empty. ", Toast.LENGTH_SHORT).show();
                }else {
                    dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates");
                    final Query applesQuery = dref.orderByChild("id").equalTo(templateId);
                    applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                for(HashMap<String, String> object : Templates.list)
                                {
                                    if(object.get("id").matches(templateId))
                                    {
                                        HashMap<String, String> newObject = new HashMap<>();
                                        newObject.put("name", title);
                                        newObject.put("message", template.getContent());
                                        newObject.put("id", templateId);
                                        Templates.list.set(Templates.list.indexOf(object), newObject);
                                    }
                                }
                                appleSnapshot.getRef().child("title").setValue(title); //brisanje iz firebasea
                                setTitle(title);
                                Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "onCancelled", databaseError.toException());
                        }
                    });
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
}

