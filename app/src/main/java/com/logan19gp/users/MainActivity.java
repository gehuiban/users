package com.logan19gp.users;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.logan19gp.users.adapter.UserAdapter;
import com.logan19gp.users.model.User;
import com.logan19gp.users.utils.TextWatcherCondensed;
import com.logan19gp.users.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "USERS_MAIN";
    private static final String PREFS_FILE = "PREFS_FILE";
    private static final String MENU_SELECTED = "MENU_SELECTED";
    private static final String MALE_SELECTED = "MALE_SELECTED";
    private static final String FEMALE_SELECTED = "FEMALE_SELECTED";
    private static final String OTHER_SELECTED = "OTHER_SELECTED";
    private static final Integer SELECT_IMAGE_CODE = 12345;
    private static final Integer CAPTURE_IMAGE_CODE = 11111;

    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private StorageReference storageRef;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private LinearLayout addUserContainer, addUserParent;
    private UserAdapter userAdapter;
    private CheckBox maleCheckBox;
    private CheckBox femaleCheckBox;
    private CheckBox otherCheckBox;
    private TextView noUsersText;

    private List<User> users;
    private User userToAdd = new User();
    private Uri selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(MainActivity.PREFS_FILE, Context.MODE_PRIVATE);
        editor = prefs.edit();

        addUserParent = (LinearLayout) findViewById(R.id.add_user_parent);
        addUserContainer = (LinearLayout) findViewById(R.id.add_user_container);
        maleCheckBox = (CheckBox) findViewById(R.id.male);
        femaleCheckBox = (CheckBox) findViewById(R.id.female);
        otherCheckBox = (CheckBox) findViewById(R.id.other);
        noUsersText = (TextView) findViewById(R.id.no_user_text);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this);
        recyclerView.setAdapter(userAdapter);
        maleCheckBox.setChecked(prefs.getBoolean(MALE_SELECTED, false));
        femaleCheckBox.setChecked(prefs.getBoolean(FEMALE_SELECTED, false));
        otherCheckBox.setChecked(prefs.getBoolean(OTHER_SELECTED, false));

        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://messages-7180e.appspot.com");

        database = FirebaseDatabase.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query query = mDatabase.child(User.USERS);

        ValueEventListener usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, User> usersMap = (Map<String, User>) dataSnapshot.getValue();
                Log.e(TAG, "" + dataSnapshot.getValue());
                users = new ArrayList<>();
                if (usersMap != null || usersMap.size() > 0) {
                    for (String key : usersMap.keySet()) {
                        if (usersMap.get(key) instanceof Map) {
                            Map<String, Object> mapObj = (Map<String, Object>) usersMap.get(key);
                            User user = new User(key, mapObj);
                            if (!user.isDeleted()) {
                                users.add(user);
                            }
                        }
                    }
                }
                if (users.size() > 0) {
                    int savedMenuId = prefs.getInt(MENU_SELECTED, 0);
                    sortUsers(savedMenuId == R.id.sort_default_id ? R.id.other : savedMenuId,
                            maleCheckBox.isChecked(), femaleCheckBox.isChecked(),
                            otherCheckBox.isChecked());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        query.addValueEventListener(usersListener);

        maleCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (femaleCheckBox.isChecked()) {
                    femaleCheckBox.setChecked(false);
                }
                if (otherCheckBox.isChecked()) {
                    otherCheckBox.setChecked(false);
                }
                int savedMenuId = prefs.getInt(MENU_SELECTED, R.id.sort_default_id);
                sortUsers(savedMenuId == R.id.sort_default_id ? R.id.other : savedMenuId, maleCheckBox.isChecked(),
                        femaleCheckBox.isChecked(), otherCheckBox.isChecked());
                saveCheckBoxes();
            }
        });

        otherCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (femaleCheckBox.isChecked()) {
                    femaleCheckBox.setChecked(false);
                }
                if (maleCheckBox.isChecked()) {
                    maleCheckBox.setChecked(false);
                }
                int savedMenuId = prefs.getInt(MENU_SELECTED, R.id.sort_default_id);
                sortUsers(savedMenuId == R.id.sort_default_id ? R.id.other : savedMenuId, maleCheckBox.isChecked(),
                        femaleCheckBox.isChecked(), otherCheckBox.isChecked());
                saveCheckBoxes();
            }
        });

        femaleCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (maleCheckBox.isChecked()) {
                    maleCheckBox.setChecked(false);
                }
                if (otherCheckBox.isChecked()) {
                    otherCheckBox.setChecked(false);
                }
                int savedMenuId = prefs.getInt(MENU_SELECTED, R.id.sort_default_id);
                sortUsers(savedMenuId == R.id.sort_default_id ? R.id.female : savedMenuId, maleCheckBox.isChecked(),
                        femaleCheckBox.isChecked(), otherCheckBox.isChecked());
                saveCheckBoxes();
            }
        });
        addUserParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "Network not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCheckBoxes() {
        editor.putBoolean(MALE_SELECTED, maleCheckBox.isChecked());
        editor.putBoolean(FEMALE_SELECTED, femaleCheckBox.isChecked());
        editor.putBoolean(OTHER_SELECTED, otherCheckBox.isChecked());
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_user_id) {
            Log.e(TAG, "add user clicked");
            addUser();
            return true;
        } else {
            Boolean menuUsed = sortUsers(item.getItemId(), maleCheckBox.isChecked(),
                    femaleCheckBox.isChecked(), otherCheckBox.isChecked());
            if (menuUsed) {
                editor.putInt(MENU_SELECTED, item.getItemId());
                editor.apply();
                return menuUsed;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void addUser() {
        addUserParent.setVisibility(View.VISIBLE);
        final RelativeLayout itemLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.add_user, null);
        addUserContainer.addView(itemLayout);
        TextView cancelButton = (TextView) itemLayout.findViewById(R.id.cancel_action);
        TextView saveButton = (TextView) itemLayout.findViewById(R.id.save_action);
        TextView addHobbyButton = (TextView) itemLayout.findViewById(R.id.add_hobby_text);

        final TextInputLayout genderInputLayout = (TextInputLayout) itemLayout.findViewById(R.id.input_gender);
        final TextInputLayout nameInputLayout = (TextInputLayout) itemLayout.findViewById(R.id.input_name);
        final TextInputLayout ageInputLayout = (TextInputLayout) itemLayout.findViewById(R.id.input_age);
        nameInputLayout.setErrorEnabled(false);
        ageInputLayout.setErrorEnabled(false);
        genderInputLayout.setErrorEnabled(false);
        final EditText nameEdit = (EditText) itemLayout.findViewById(R.id.edit_name);
        final EditText ageEdit = (EditText) itemLayout.findViewById(R.id.edit_age);
        final EditText hobbyEdit = (EditText) itemLayout.findViewById(R.id.edit_hobby);
        final RadioGroup genderGroup = (RadioGroup) itemLayout.findViewById(R.id.gender_group);
        final LinearLayout itemsHobbyContainer = (LinearLayout) itemLayout.findViewById(R.id.items_hobby_container);
        final ImageView addPhoto = (ImageView) itemLayout.findViewById(R.id.photo_add);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserParent.setVisibility(View.GONE);
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        });
        addHobbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hobbyToAdd = hobbyEdit.getText().toString().trim();
                if (hobbyToAdd.length() > 0) {
                    userToAdd.addHobby(0, hobbyToAdd);
                    hobbyEdit.setText("");
                    itemsHobbyContainer.addView(getHobbyItem(itemsHobbyContainer, hobbyToAdd), 0);
                } else {
                    Toast.makeText(view.getContext(), "Hobby can not be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        nameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                String name = nameEdit.getText().toString().trim();
                if (!hasFocus) {
                    if (name.length() < 1) {
                        nameInputLayout.setError(getString(R.string.name_required));
                        nameInputLayout.setErrorEnabled(true);
                    } else {
                        userToAdd.setName(nameEdit.getText().toString().trim());
                    }
                }
            }
        });
        nameEdit.addTextChangedListener(new TextWatcherCondensed() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    nameInputLayout.setErrorEnabled(false);
                }
            }
        });
        ageEdit.addTextChangedListener(new TextWatcherCondensed() {
            @Override
            public void afterTextChanged(Editable editable) {
                Long age = Utils.getAge(ageEdit);
                if (age != null) {
                    userToAdd.setAge(age);
                    ageInputLayout.setErrorEnabled(false);
                }
            }
        });
        ageEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    Long age = Utils.getAge(ageEdit);
                    if (age == null) {
                        ageInputLayout.setError("Invalid data");
                        ageInputLayout.setErrorEnabled(true);
                    }
                    userToAdd.setAge(age);
                }
            }
        });
        genderGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                genderInputLayout.setErrorEnabled(false);
                userToAdd.setGenderFromId(checkedId);
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                String[] selectionItems = new String[]{view.getContext().getResources().getString(R.string.gallery),
                        view.getContext().getResources().getString(R.string.camera)};
                builder.setTitle(R.string.select_image)
                       .setItems(selectionItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        Intent selectFromGalleryIntent = new Intent(Intent.ACTION_PICK,
                                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        selectFromGalleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                                        MainActivity.this.startActivityForResult(selectFromGalleryIntent, SELECT_IMAGE_CODE);
                                        break;

                                    case 1:
                                        Intent captureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        captureFromCameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                                        String appPath = MainActivity.this.getApplicationContext().getFilesDir().getAbsolutePath();
                                        captureFromCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, appPath);
                                        MainActivity.this.startActivityForResult(captureFromCameraIntent, CAPTURE_IMAGE_CODE);
                                        break;
                                }
                            }
                        })
                       .setNegativeButton(getResources().getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                builder.create().show();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userToAdd.setAge(Utils.getAge(ageEdit));
                userToAdd.setName(nameEdit.getText().toString().trim());
                userToAdd.setGenderFromId(genderGroup.getCheckedRadioButtonId());
                String hobbyToAdd = hobbyEdit.getText().toString().trim();
                if (hobbyToAdd.length() > 0) {
                    userToAdd.addHobby(0, hobbyToAdd);
                    hobbyEdit.setText("");
                    itemsHobbyContainer.addView(getHobbyItem(itemsHobbyContainer, hobbyToAdd), 0);
                }
                if (userToAdd.isValid()) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    DatabaseReference pushedPostRef = database.getReference().push();
                    String userKey = pushedPostRef.getKey();

                    mDatabase.child(User.USERS).child(userKey).setValue(userToAdd.toMap(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.e(TAG, "Data could not be saved " + databaseError.getMessage());
                            } else {
                                Log.d(TAG, "Data saved successfully." + databaseReference);
                                userToAdd.setKey(databaseReference.getKey());
                                if (selectedImage != null) {
                                    String pathImgOnServer = "images/" + databaseReference.getKey() + ".jpg";
                                    StorageReference riversRef = storageRef.child(pathImgOnServer);
                                    UploadTask uploadTask = riversRef.putFile(selectedImage);
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                        }
                                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                            Log.d(TAG, "Upload is " + progress + "% done");
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                            Log.d(TAG, "downloadUrl: " + downloadUrl);
                                            mDatabase.child(User.USERS).child(userToAdd.getKey()).child(User.PHOTO).setValue(downloadUrl.toString());
                                        }
                                    });
                                }
                            }
                        }
                    });
                    addUserContainer.removeAllViews();
                    addUserParent.setVisibility(View.GONE);
                    userToAdd = new User();
                } else {
                    if (userToAdd.getGender() == null) {
                        genderInputLayout.setError(getString(R.string.gender_required));
                        genderInputLayout.setErrorEnabled(true);
                        genderInputLayout.requestFocus();
                    }
                    if (ageEdit.getText().toString().trim().length() < 1) {
                        ageInputLayout.setError(getString(R.string.age_required));
                        ageInputLayout.setErrorEnabled(true);
                        ageEdit.requestFocus();
                    }
                    if (nameEdit.getText().toString().trim().length() < 1) {
                        nameInputLayout.setError(getString(R.string.name_required));
                        nameInputLayout.setErrorEnabled(true);
                        nameEdit.requestFocus();
                    }
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_IMAGE_CODE || requestCode == CAPTURE_IMAGE_CODE) {
            selectedImage = data.getData();
            if (selectedImage == null) {
                Log.e(TAG, "selected Image is null");
            }
            Picasso.with(this).load(selectedImage).into((ImageView) findViewById(R.id.photo_add));
        }
    }

    private LinearLayout getHobbyItem(final LinearLayout itemsHobbyContainer, String hobby) {
        final LinearLayout itemLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.item_hobby, null);
        itemLayout.setTag(hobby);
        TextView hobbyItemText = (TextView) itemLayout.findViewById(R.id.hobby_name_text);
        View hobbyDelete = itemLayout.findViewById(R.id.delete_hobby);
        hobbyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userToAdd.getHobbies().remove(itemLayout.getTag());
                itemsHobbyContainer.removeView(itemLayout);
            }
        });
        hobbyItemText.setText(hobby);
        return itemLayout;
    }

    private Boolean sortUsers(Integer menuId, Boolean filterMale, Boolean filterFemale, Boolean filterOther) {
        noUsersText.setVisibility(View.GONE);
        if (menuId != null && menuId.equals(R.id.sort_default_id)) {
            if (femaleCheckBox.isChecked()) {
                femaleCheckBox.setChecked(false);
                editor.putBoolean(FEMALE_SELECTED, false);
                filterFemale = false;
            }
            if (maleCheckBox.isChecked()) {
                maleCheckBox.setChecked(false);
                editor.putBoolean(MALE_SELECTED, false);
                filterMale = false;
            }
            if (otherCheckBox.isChecked()) {
                otherCheckBox.setChecked(false);
                editor.putBoolean(OTHER_SELECTED, false);
                filterOther = false;
            }
        }
        List<User> usersToShow = new ArrayList<>();
        if (users != null && (filterFemale || filterMale || filterOther)) {
            for (User user : users) {
                if (filterFemale && user.isFemale() ||
                        filterMale && user.isMale() ||
                        filterOther && !user.isMale() && !user.isFemale()) {
                    usersToShow.add(user);
                }
            }
        } else if (users != null) {
            usersToShow.addAll(users);
        }
        if (usersToShow.size() < 1) {
            userAdapter.clearData();
            noUsersText.setVisibility(View.VISIBLE);
            return true;
        }
        Boolean isSorted = false;
        switch (menuId) {
            case R.id.sort_age_asc_id:
                Collections.sort(usersToShow, new Comparator<User>() {
                    public int compare(User user1, User user2) {
                        int value = user1.getAge().compareTo(user2.getAge());
                        if (value == 0) {
                            return user1.getName().compareTo(user2.getName());
                        } else {
                            return value;
                        }
                    }
                });
                isSorted = true;
                break;

            case R.id.sort_age_desc_id:
                Collections.sort(usersToShow, new Comparator<User>() {
                    public int compare(User user1, User user2) {
                        int value = user2.getAge().compareTo(user1.getAge());
                        if (value == 0) {
                            return user2.getName().compareTo(user1.getName());
                        } else {
                            return value;
                        }
                    }
                });
                isSorted = true;
                break;

            case R.id.sort_name_asc_id:
                Collections.sort(usersToShow, new Comparator<User>() {
                    public int compare(User user1, User user2) {
                        return user1.getName().compareTo(user2.getName());
                    }
                });
                isSorted = true;
                break;

            case R.id.sort_name_desc_id:
                Collections.sort(usersToShow, new Comparator<User>() {
                    public int compare(User user1, User user2) {
                        return user2.getName().compareTo(user1.getName());
                    }
                });
                isSorted = true;
                break;

            default:
                editor.apply();
                Collections.sort(usersToShow, new Comparator<User>() {
                    public int compare(User user1, User user2) {
                        if (user1.getKey() == null) {
                            return user2.getKey() == null ? 0 : -1;
                        } else if (user2.getKey() == null) {
                            return user1.getKey() == null ? 0 : 1;
                        }
                        return user1.getKey().compareTo(user2.getKey());
                    }
                });
                isSorted = true;
                break;
        }
        userAdapter.clearData();
        userAdapter.addUsers(usersToShow);
        return isSorted;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}