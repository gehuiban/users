package com.logan19gp.users;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.logan19gp.users.model.User;
import com.logan19gp.users.utils.TextWatcherCondensed;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by george on 11/4/2016.
 */
public class UserActivity extends AppCompatActivity {

    public static final String EXTRA_USER = "EXTRA_USER";
    private static final String TAG = "UserActivity";

    private TextView nameText;
    private TextView ageText;
    private TextView genderText;
    private TextView hobbyText;
    private TextView editHobbyText;
    private EditText editHobbyEditText;
    private TextView discardHobbyText;
    private TextInputLayout hobbyInputLayout;
    private LinearLayout editHobbyContainer;
    private LinearLayout itemsHobbyContainer;
    private ImageView userPhoto;
    private DatabaseReference mDatabase;
    private List<String> tempHobbies;
    private User user;
    private LayoutInflater layoutInflater;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        nameText = (TextView) findViewById(R.id.name_text);
        ageText = (TextView) findViewById(R.id.age_text);
        genderText = (TextView) findViewById(R.id.gender_text);
        hobbyText = (TextView) findViewById(R.id.hobby_text);
        TextView addHobbyText = (TextView) findViewById(R.id.add_hobby_text);
        editHobbyText = (TextView) findViewById(R.id.update_hobby_text);
        editHobbyEditText = (EditText) findViewById(R.id.edit_hobby);
        discardHobbyText = (TextView) findViewById(R.id.discard_text);
        hobbyInputLayout = (TextInputLayout) findViewById(R.id.input_hobby);
        editHobbyContainer = (LinearLayout) findViewById(R.id.edit_hobby_container);
        itemsHobbyContainer = (LinearLayout) findViewById(R.id.items_hobby_container);
        userPhoto = (ImageView) findViewById(R.id.photo);
        ImageView backImg = (ImageView) findViewById(R.id.back);
        ImageView deleteImg = (ImageView) findViewById(R.id.delete);
        layoutInflater = getLayoutInflater();
        context = this;

        final Intent intent = getIntent();
        Object obj = intent.getParcelableExtra(EXTRA_USER);
        if (obj instanceof User) {
            user = (User) obj;
            setViews();
        }

        tempHobbies = new ArrayList<>();
        if (user.getHobbies() != null) {
            tempHobbies.addAll(user.getHobbies());
        }
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Query query = mDatabase.child("users").child(user.getKey());
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> mapObj = (Map<String, Object>) dataSnapshot.getValue();
                user = new User(dataSnapshot.getKey(), mapObj);
                if (user.isDeleted()) {
                    openDialogUserDeleted(context);
                } else {
                    setViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        query.addValueEventListener(userListener);
        discardHobbyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editHobbyText.setText(R.string.edit);
                discardHobbyText.setVisibility(View.GONE);
                if (editHobbyContainer.getVisibility() == View.VISIBLE) {
                    hobbyText.setVisibility(View.VISIBLE);
                    editHobbyContainer.setVisibility(View.GONE);
                }
                tempHobbies = new ArrayList<>();
                tempHobbies.addAll(user.getHobbies());
            }
        });
        editHobbyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hobbyText.getVisibility() == View.VISIBLE) {
                    hobbyText.setVisibility(View.GONE);
                    editHobbyText.setText(R.string.save);
                    editHobbyContainer.setVisibility(View.VISIBLE);
                    discardHobbyText.setVisibility(View.VISIBLE);
                    itemsHobbyContainer.removeAllViews();
                    for (String hobby : tempHobbies) {
                        itemsHobbyContainer.addView(getHobbyItem(hobby));
                    }
                    if (hobbyText != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(hobbyText.getWindowToken(), 0);
                    }
                } else if (editHobbyContainer.getVisibility() == View.VISIBLE) {
                    hobbyText.setText(getHobbiesString(tempHobbies));
                    hobbyText.setVisibility(View.VISIBLE);
                    editHobbyContainer.setVisibility(View.GONE);
                    editHobbyText.setText(R.string.edit);
                    discardHobbyText.setVisibility(View.GONE);
                    user.setHobbies(tempHobbies);
                    mDatabase.child("users").child(user.getKey()).child(User.HOBBIES).setValue(user.getHobbies());
                }
            }
        });

        addHobbyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hobbyToAdd = editHobbyEditText.getText().toString().trim();
                if (hobbyToAdd.length() > 0) {
                    tempHobbies.add(0, hobbyToAdd);
                    editHobbyEditText.setText("");
                    itemsHobbyContainer.addView(getHobbyItem(hobbyToAdd), 0);
                } else {
                    editHobbyEditText.setError(getString(R.string.empty_hobby));
                    hobbyInputLayout.setErrorEnabled(true);
                }
            }
        });

        editHobbyEditText.addTextChangedListener(new TextWatcherCondensed() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    editHobbyEditText.setError(null);
                    hobbyInputLayout.setErrorEnabled(false);
                }
            }
        });
        deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialogDelete(UserActivity.this);
            }
        });

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserActivity.this.onBackPressed();
            }
        });
    }

    private LinearLayout getHobbyItem(String hobby) {
        final LinearLayout itemLayout = (LinearLayout) layoutInflater.inflate(R.layout.item_hobby, null);
        itemLayout.setTag(hobby);
        TextView hobbyItemText = (TextView) itemLayout.findViewById(R.id.hobby_name_text);
        View hobbyDelete = itemLayout.findViewById(R.id.delete_hobby);
        hobbyDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempHobbies.remove(itemLayout.getTag());
                itemsHobbyContainer.removeView(itemLayout);
            }
        });
        hobbyItemText.setText(hobby);
        return itemLayout;
    }

    private String getHobbiesString(List<String> hobbies) {
        String hobbiesStr = "";
        for (String hobby : hobbies) {
            hobbiesStr += (hobbiesStr.length() > 0 ? "\n" : "") + hobby;
        }
        return hobbiesStr;
    }

    private void setViews() {
        if (user.getName() != null) {
            nameText.setText(user.getName());
        }
        if (user.getAge() != null) {
            ageText.setText(String.format("%d yo", user.getAge()));
        }
        if (user.getGender() != null) {
            genderText.setText(user.getGenderText());
        }
        if (user.getHobbies() != null) {
            hobbyText.setText(getHobbiesString(user.getHobbies()));
        }
        Picasso.with(this).load(user.getPhoto()).into(userPhoto);
    }

    public void openDialogDelete(final Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder
                .setTitle(R.string.warning)
                .setMessage(R.string.confirm_msg)
                .setCancelable(true)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(getString(R.string.delete),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                                mDatabase.getReference().child(User.USERS).child(user.getKey()).child(User.IS_DEL).setValue(true);
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void openDialogUserDeleted(final Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle(getString(R.string.info))
                .setMessage(R.string.deleted_msg)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                onBackPressed();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        Boolean isWindowActive = getWindow().getDecorView().getRootView().isShown();
        if (isWindowActive) {
            alertDialog.show();
        } else {
            Log.e(TAG, "activity is destroyed");
            onBackPressed();
        }
    }
}