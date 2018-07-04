package com.example.vanessa.groupsms;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
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

public class Templates extends AppCompatActivity implements SearchView.OnQueryTextListener {
    MenuItem search, add;

    DatabaseReference dref;
    ListView listview;
    ArrayList<String> list=new ArrayList<>();
    private static final String TAG = "MainActivity";

    MenuItem searchMenuItem;
    SearchView searchView;

    ArrayAdapter<String> adapter;

    private String templateId;

    boolean flag = false;
    String content=null;

    EditText edit_title;
    EditText edit_content;

    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_templates);
        setTitle("Templates");

        listview=(ListView)findViewById(R.id.list);
        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listview.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        dref=FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("templates");

        dref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Template template = dataSnapshot.getValue(Template.class);
                list.add(template.getTitle());
                adapter.notifyDataSetChanged();
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

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                final String title = (String) parent.getItemAtPosition(position);

                dref.orderByChild("title").equalTo(title).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Template template = dataSnapshot.getValue(Template.class);
                        content = template.getContent();

                        showTemplate(title,content);
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

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long arg3) {
                final String template= list.get(position);
                final int index = position;
                AlertDialog.Builder alert = new AlertDialog.Builder(Templates.this);
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
        if (adapter==null) flag=true;
        //showText();
    }

//    public void showText(){
//        TextView text_main = (TextView) findViewById(R.id.text_main);
//        String text="\n\nYou have no groups yet.";
//        if(flag==true){
//            text_main.setText(text);
//        }
//    }


    String new_title="";
    String new_content="";
    String oldTitle="";

    public void showTemplate(String title, String content){
        oldTitle=title;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
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


                Intent templates = new Intent(Templates.this, Templates.class);
                startActivity(templates);

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

    @Override
    public boolean onSupportNavigateUp(){
        Intent main = new Intent(Templates.this, MainActivity.class);
        startActivity(main);
        return true;
    }

    @Override
    public void onBackPressed(){
        Intent main = new Intent(Templates.this, MainActivity.class);
        startActivity(main);
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

    private String template_title = "";
    private String template_content = "";

    public void add(View view){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
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

                    Template template = new Template(template_title, template_content);
                    dref.child("templates").child(templateId).setValue(template);
                    dref.child(template_title);

                }
                finish();
                Intent templates = new Intent(Templates.this, Templates.class);
                startActivity(templates);

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
}
