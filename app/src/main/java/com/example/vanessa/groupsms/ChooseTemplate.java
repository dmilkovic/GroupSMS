package com.example.vanessa.groupsms;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import android.widget.TextView;
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

import static android.view.View.GONE;

public class ChooseTemplate extends AppCompatActivity implements SearchView.OnQueryTextListener {
    MenuItem search, add;

    DatabaseReference dref;
    DatabaseReference dref2;

    ListView lv;
    ArrayList<HashMap<String, String>> list = new ArrayList<>();
    ArrayList<HashMap<String, String>> multiselect_list = new ArrayList<>();
   // ArrayList<HashMap<String, String>> newList = new ArrayList<>();
    private static final String TAG = "MainActivity";

    MenuItem searchMenuItem, delete, orderByMenuItem;
    SearchView searchView;

    Menu context_menu;
   // MultiSelectAdapter multiSelectAdapter;
    ActionMode mActionMode;
    SimpleAdapter.ViewBinder binder;

    boolean isMultiSelect = false;

    private SimpleAdapter adapter;

    String content=null;
    String group_name=null;

    EditText edit_title;
    EditText edit_content;
    String message;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;

    ArrayList<String> contacts=new ArrayList<>();
    ArrayList<String> numbers=new ArrayList<>();

    FloatingActionButton fab;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_template);
        setTitle("Choose template");

        Bundle extras = getIntent().getExtras();
        group_name = extras.getString("group_name");

        lv = (ListView) findViewById(R.id.list);
        lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(modeListener);

        fab = (FloatingActionButton)findViewById(R.id.add_button2);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       /* adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        lv.setAdapter(adapter);*/

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }
        getData();

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

    }

    //public ArrayList<HashMap<String, String>> getData()
    public void getData()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }
        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates");
        dref2=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("groups");
        //newList.clear();
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
               // adapter.notifyDataSetChanged();
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
        //return newList;
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
                    AlertDialog.Builder alert = new AlertDialog.Builder(ChooseTemplate.this);
                    alert.setMessage("Are you sure you want to delete selected templates?");
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
    /*
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.template_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final int listPosition = info.position;
        switch (item.getItemId()){
            case R.id.delete:

                final String template = list.get(listPosition).get("name");

               // final int index = position;
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
                        list.remove(listPosition); //brisanje samo iz arraya, ne iz firebasea
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
            case R.id.edit:
                Toast.makeText(getApplicationContext(), "Edit.", Toast.LENGTH_LONG).show();

                final String title1 = list.get(listPosition).get("name");
                Log.d("title", title1);
                dref.orderByChild("title").equalTo(title1).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Template template1 = dataSnapshot.getValue(Template.class);
                        content = template1.getContent();

                        showEditTemplate(title1, content);
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
                return true;
            default:  return super.onContextItemSelected(item);

        }
    }*/

    private void refreshAdapter(){
        adapter = new SimpleAdapter(this.getApplicationContext(), list,
                R.layout.contetnt_template, new String[]{"name", "message"},
                new int[]{R.id.name, R.id.message}) {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                checkBox.setTag(position);
                if(isMultiSelect)
                {
                    fab.setVisibility(GONE);
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            int position = (int)compoundButton.getTag();
                            if (multiselect_list.contains(list.get(position)))
                            {
                                multiselect_list.remove(list.get(position));
                            }
                            else
                            {
                                multiselect_list.add(list.get(position));
                            }

                            if (multiselect_list.size() > 0) {
                                mActionMode.setTitle("" + multiselect_list.size());
                            }
                            else
                            {
                                mActionMode.setTitle("");
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
                    fab.setVisibility(View.VISIBLE);
                }
                return view;
            }
        };

        lv.setAdapter(adapter);
       // registerForContextMenu(lv);

        /*lv.setOnTouchListener(new AdapterView.OnTouchListener(){
                                  @Override
                                  public boolean onTouch(View v, MotionEvent event) {
                                      switch (event.getAction()){
                                          case MotionEvent.ACTION_DOWN:
                                              break;
                                          default:
                                              break;
                                      }
                                  }});
        */
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (!isMultiSelect)
                {
                    HashMap<String, String> object = (HashMap<String, String>) parent.getItemAtPosition(position);
                    final String title = object.get("name");
                    dref.orderByChild("title").equalTo(title).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Template template = dataSnapshot.getValue(Template.class);
                            content = template.getContent();
                            //showTemplate(content, template.getTitle());
                            showTemplate(template, position);
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
            }
        });

       lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View arg1, int position, long id) {
                /*if (!isMultiSelect) {
                    multiselect_list = new ArrayList();
                    isMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }
                multi_select(position);*/

               /* HashMap<String, String> object = (HashMap<String, String>) parent.getItemAtPosition(position);
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
               */
                return true;
            }
        });
    }

    String new_content="";
    AlertDialog bEdit, b;
    public void showTemplate(Template template, final int position){
        if(b != null) b.dismiss();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
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
                b.dismiss();
            }
        });

        content = template.getContent();
        final String title1 = template.getTitle();
        dialogBuilder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dref.orderByChild("title").equalTo(title1).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Template template1 = dataSnapshot.getValue(Template.class);
                        new_content = template1.getContent();
                        showEditTemplate(template1, position);
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
        b = dialogBuilder.create();
        b.show();
    }

    String new_title="";
    String oldTitle="";

    public void showEditTemplate(final Template template, final int position){
        if(bEdit != null) bEdit.dismiss();
        final String title = template.getTitle();
        final String content = template.getContent();

        oldTitle=title;
        final AlertDialog.Builder dialogBuilderEdit = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog2, null);
        dialogBuilderEdit.setView(dialogView);

        edit_title = (EditText) dialogView.findViewById(R.id.title);
        edit_content = (EditText) dialogView.findViewById(R.id.content);

        edit_title.setText(title);
        edit_content.setText(content);

        dialogBuilderEdit.setTitle("New template");

        //dialogBuilder.setMessage("Title:");
        edit_title.setSingleLine(true);
        edit_title.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        edit_title.setGravity(Gravity.LEFT | Gravity.TOP);
        edit_title.setHint("Title...");

        edit_content.setSingleLine(false);
        edit_content.setHint("Content...");
        edit_content.setLines(4);
        edit_content.setMaxLines(5);
        edit_content.setGravity(Gravity.LEFT | Gravity.TOP);
        edit_content.setHorizontalScrollBarEnabled(false);


       dialogBuilderEdit.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

//                Query applesQuery = dref.orderByChild("title").equalTo(oldTitle);
//                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
//
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
//                            appleSnapshot.getRef().removeValue(); //brisanje iz firebasea
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        Log.e(TAG, "onCancelled", databaseError.toException());
//                    }
//                });

                new_title = edit_title.getText().toString();
                new_content = edit_content.getText().toString();

                final Query applesQuery = dref.orderByChild("title").equalTo(oldTitle);
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().child("title").setValue(new_title); //brisanje iz firebasea
                            appleSnapshot.getRef().child("content").setValue(new_content); //brisanje iz firebasea
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled", databaseError.toException());
                    }
                });
                HashMap<String, String> newObject = new HashMap<>();
                newObject.put("name", new_title);
                newObject.put("message", new_content);
                list.set(position, newObject);
                adapter.notifyDataSetChanged();
                /*Intent templates = new Intent(ChooseTemplate.this, ChooseTemplate.class);
                templates.putExtra("group_name", group_name);
                startActivity(templates);*/
            }
        });

        dialogBuilderEdit.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                bEdit.dismiss();
                showTemplate(template, position);
            }
        });


        dialogBuilderEdit.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(ChooseTemplate.this);
//                alert.setTitle("Alert!");
                alert.setMessage("Are you sure you want to delete this template?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, String> object = new HashMap<>();
                        object.put("name", template.getTitle());
                        object.put("message", template.getContent());
                        deleteItem(object);
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
            }
        });

        bEdit = dialogBuilderEdit.create();
        bEdit.show();
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
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        orderByMenuItem = menu.findItem(R.id.order);
        orderByMenuItem.setVisible(false);
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
            case android.R.id.home:
                this.finish();
                return true;
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

    private String template_title = "";
    private String template_content = "";
    private String templateId;
    private android.support.v7.app.AlertDialog addDialog;


    public void add(View view){
        if(addDialog!=null) addDialog.dismiss();
        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog2, null);
        dialogBuilder.setView(dialogView);

        final EditText title = (EditText) dialogView.findViewById(R.id.title);
        final EditText content = (EditText) dialogView.findViewById(R.id.content);

        dialogBuilder.setTitle("New template");

        //dialogBuilder.setMessage("Title:");
        title.setSingleLine(true);
        title.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        title.setGravity(Gravity.LEFT | Gravity.TOP);
        title.setHint("Title...");

        content.setSingleLine(false);
        content.setHint("Content...");
        content.setLines(4);
        content.setMaxLines(5);
        content.setGravity(Gravity.LEFT | Gravity.TOP);
        content.setHorizontalScrollBarEnabled(false);
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                template_title = title.getText().toString();
                template_content = content.getText().toString();

                if((template_title.isEmpty() || !(template_title.trim().length() > 0)) && (template_content.isEmpty() || !(template_content.trim().length() > 0))) {
                    Toast.makeText(getApplicationContext(), "Template can't be empty. ", Toast.LENGTH_SHORT).show();
                }else if (template_content.isEmpty() || !(template_content.trim().length() > 0)){
                    Toast.makeText(getApplicationContext(), "Template content can't be empty. ", Toast.LENGTH_SHORT).show();
                }else if (template_title.isEmpty() || !(template_title.trim().length() > 0)) {
                    Toast.makeText(getApplicationContext(), "Template title can't be empty. ", Toast.LENGTH_SHORT).show();
                }else{
                    dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                    templateId= dref.push().getKey();

                    Template template = new Template(template_title, template_content, templateId);
                    dref.child("templates").child(templateId).setValue(template);
                    dref.child(template_title);
                    dref.child(template_content);

                }
                /*list.clear();
                getData();
              //  refreshAdapter();
                adapter.notifyDataSetChanged();*/
               // refreshAdapter();
                finish();
                Intent templates = new Intent(ChooseTemplate.this, ChooseTemplate.class);
                templates.putExtra("group_name", group_name);
                startActivity(templates);

            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        addDialog = dialogBuilder.create();
        addDialog.show();

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
        final String template = item.get("name");
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
        list.remove(item); //brisanje samo iz arraya, ne iz firebasea
        adapter.notifyDataSetChanged();
    }
}
