package com.example.vanessa.groupsms;

import android.Manifest;
import android.support.v4.app.ListFragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
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

public class Templates extends ListFragment implements SearchView.OnQueryTextListener {
    MenuItem search, add;

    DatabaseReference dref;
    ListView lv;
    ArrayList<HashMap<String, String>> list = new ArrayList<>();
    ArrayList<HashMap<String, String>> multiselect_list = new ArrayList<>();
    private static final String TAG = "MainActivity";

    MenuItem searchMenuItem, delete, orderByMenuItem;
    SearchView searchView;

    ActionMode mActionMode;

    private SimpleAdapter adapter;

    private String templateId;

    private boolean loading = true;
    boolean isMultiSelect = false;
    boolean flag = false;
    String content=null;

    EditText edit_title;
    EditText edit_content;
    FloatingActionButton fab;
    String uid;

    AlertDialog alertAdd, alertShow;

   /* @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templates);
        setTitle("Templates");

        lv=(ListView)findViewById(R.id.list);
        lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(modeListener);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fab = (FloatingActionButton)findViewById(R.id.add_button2);

        if (adapter==null) flag=true;
    }*/

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       View view = inflater.inflate(R.layout.fragment_templates, container, false);

       lv=(ListView)view.findViewById(android.R.id.list);

       fab = (FloatingActionButton)view.findViewById(R.id.add_button2);


       setHasOptionsMenu(true);
       return view;
   }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(modeListener);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add(view);
            }
        });
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getData();
        refreshAdapter();

    }

   /* protected void onStart()
    {
        super.onStart();
        list.clear();
        getData();
        refreshAdapter();
    }*/

    private void getData()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates");

      /*  dref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                list.clear();

                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                    Template t = appleSnapshot.getValue(Template.class);
                    HashMap<String, String> newObject = new HashMap<>();
                    newObject.put("name", t.getTitle());
                    newObject.put("message", t.getContent());
                    list.add(newObject);
                    Log.d("NOVI", new_content + t.getContent());
                }
                refreshAdapter();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

      // dref.orderByChild("title").addChildEventListener(new ChildEventListener()

            dref.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Template template = dataSnapshot.getValue(Template.class);
                HashMap<String, String> object = new HashMap<>();
                object.put("name", template.getTitle());
                object.put("message", template.getContent());
                object.put("id", String.valueOf(template.id));

                Log.d("loading", loading + "  " +  object.get("name") + "ID: " + template.id);
                list.add(object);
                adapter.notifyDataSetChanged();

           //     refreshAdapter();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                refreshAdapter();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Template template = dataSnapshot.getValue(Template.class);
                list.remove(template.getTitle());
                refreshAdapter();
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

//    public void showText(){
//        TextView text_main = (TextView) findViewById(R.id.text_main);
//        String text="\n\nYou have no groups yet.";
//        if(flag==true){
//            text_main.setText(text);
//        }
//    }

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
            mActionMode.setTitle("Delete templates");
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
                    android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(getContext());
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

    private void refreshAdapter(){
        adapter = new SimpleAdapter(getActivity().getApplicationContext(), list,
                R.layout.contetnt_template, new String[]{"name", "message"},
                new int[]{R.id.name, R.id.message}) {
            @Override
            public View getView (int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
                checkBox.setTag(position);
                if(isMultiSelect)
                {
                    fab.setVisibility(View.GONE);
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

                            if (multiselect_list.size() > 0 ) {
                                if(multiselect_list.size() == 1) mActionMode.setTitle("Delete " + multiselect_list.size() + " template");
                                else mActionMode.setTitle("Delete " + multiselect_list.size() + " templates");
                            }
                            else
                            {
                                mActionMode.setTitle("Delete templates");
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

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if(!isMultiSelect)
                {
                    HashMap<String, String> object = (HashMap<String, String>) parent.getItemAtPosition(position);
                    final String title = object.get("name");
                    content = object.get("message");
                    Log.d("ID", "OVo je id"+ object.get("id"));
                    showTemplate(title, content, position, object.get("id"));
                  /*  dref.orderByChild("title").equalTo(title).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            Template template = dataSnapshot.getValue(Template.class);
                            content = template.getContent();

                            showTemplate(title, content, position);
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
                    });*/
                }
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {
                return true;
            }
        });

    }

    String new_title="";
    String new_content="";
    String oldTitle="";

    public void showTemplate(final String title, final String content, final int position, final String id){

        if(alertShow != null) alertShow.dismiss();
        oldTitle=title;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog2, null);
        dialogBuilder.setView(dialogView);

        edit_title = (EditText) dialogView.findViewById(R.id.title);
        edit_content = (EditText) dialogView.findViewById(R.id.content);

        edit_title.setText(title);
        edit_content.setText(content);

        dialogBuilder.setTitle("New template");

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
        dialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                new_title = edit_title.getText().toString();
                new_content = edit_content.getText().toString();

                final Query applesQuery = dref.orderByChild("id").equalTo(id);
                //final Query applesQuery = dref.orderByChild("id").equalTo(id);
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            Template t = new Template(new_title, new_content, id);
                            appleSnapshot.getRef().setValue(t);
                         //   appleSnapshot.getRef().child("title").setValue(new_title); //brisanje iz firebasea
                         //   appleSnapshot.getRef().child("content").setValue(new_content); //brisanje iz firebasea
                            Log.d("NOVI", new_content + dataSnapshot.getValue(Template.class).getContent() + "  " + id);
                          /*  HashMap<String, String> newObject = new HashMap<>();
                            newObject.put("name", new_title);
                            newObject.put("message", new_content);
                            list.set(position, newObject);
                            //adapter.notifyDataSetChanged();*/
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled", databaseError.toException());
                    }
                });
               /* Intent templates = new Intent(Templates.this, Templates.class);
                startActivity(templates);*/
              //  adapter.notifyDataSetChanged();
         //       refreshAdapter();
                HashMap<String, String> newObject = new HashMap<>();
                newObject.put("name", new_title);
                newObject.put("message", new_content);
                list.set(position, newObject);
                adapter.notifyDataSetChanged();
                refreshAdapter();
              //adapter.notifyDataSetChanged();
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });

        dialogBuilder.setNeutralButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity().getApplicationContext());
//                alert.setTitle("Alert!");
                alert.setMessage("Are you sure you want to delete this template?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    HashMap<String, String> object = new HashMap<>();
                    object.put("name", title);
                    object.put("message", content);
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

        alertShow = dialogBuilder.create();
        alertShow.show();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        orderByMenuItem = menu.findItem(R.id.order);
        orderByMenuItem.setVisible(false);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:

            case android.R.id.home:
                //this.finish();
                return true;
            case R.id.action_logout:
                Auth.GoogleSignInApi.signOut(MainActivityTab.mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getActivity(), Login.class);
                                startActivity(intent);
                            }
                        });
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String template_title = "";
    private String template_content = "";

    public void add(View view){
        if(alertAdd != null) alertAdd.dismiss();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
                loading = false;

                template_title = title.getText().toString();
                template_content = content.getText().toString();


                if((template_title.isEmpty() || !(template_title.trim().length() > 0)) && (template_content.isEmpty() || !(template_content.trim().length() > 0))) {
                    Toast.makeText(getActivity().getApplicationContext(), "Template can't be empty. ", Toast.LENGTH_SHORT).show();
                }else if (template_content.isEmpty() || !(template_content.trim().length() > 0)){
                    Toast.makeText(getActivity().getApplicationContext(), "Template content can't be empty. ", Toast.LENGTH_SHORT).show();
                }else if (template_title.isEmpty() || !(template_title.trim().length() > 0)) {
                    Toast.makeText(getActivity().getApplicationContext(), "Template title can't be empty. ", Toast.LENGTH_SHORT).show();
                }else{
                    dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                    templateId= dref.push().getKey();

                    Template template = new Template(template_title, template_content, templateId);
                    // dref.child("templates").push().setValue(template);
                    dref.child("templates").child(templateId).setValue(template);
                   // dref.child(template_title);
                  //  dref.child(template_content);
                }

                //ako ovo makneš neće se spremiti promjene ako editaš tek napravljeni template!
              /*  getActivity().finish();
                Intent templates = new Intent(getActivity(), Templates.class);
                startActivity(templates);

                */

              adapter.notifyDataSetChanged();
              refreshAdapter();
              /*  HashMap<String, String> newObject = new HashMap<>();
                newObject.put("name",template_title);
                newObject.put("message", template_content);
                list.add(newObject);
                adapter.notifyDataSetChanged();
              /*  HashMap<String, String> newObject = new HashMap<>();
                newObject.put("name",template_title);
                newObject.put("message", template_content);
                list.add(newObject);
                adapter.notifyDataSetChanged();
                refreshAdapter();*/
               /* loading = true;
                list.clear();
                getData();
                adapter.notifyDataSetChanged();*/
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //pass
            }
        });
        alertAdd = dialogBuilder.create();
        alertAdd.show();

    }

    public void templates(View view) {
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

    public void removeItems(ArrayList<HashMap<String, String>> selectedItems)
    {
        for(HashMap<String, String> item : selectedItems)
        {
            deleteItem(item);
        }
        //adapter.notifyDataSetChanged();
        //  refreshAdapter();
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
