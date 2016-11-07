package com.logan19gp.users.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.firebase.database.Exclude;
import com.logan19gp.users.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by george on 11/4/2016.
 */

public class User implements Parcelable {

    public static final String IS_DEL = "isDel";
    public static final String HOBBIES = "hobbies";
    public static final String PHOTO = "photo";
    public static final String USERS = "users";
    private String key;
    private String gender;
    private String name;
    private Long age;
    private String photo;
    private List<String> hobbies;
    private Boolean isDel;

    public User() {
    }

    public User(String key, Map<String, Object> mapObj) {
        if (mapObj == null) {
            return;
        }
        setKey(key);
        setGender((String) mapObj.get("gender"));
        setName((String) mapObj.get("name"));
        setAge((Long) mapObj.get("age"));
        setPhoto((String) mapObj.get("photo"));
        setHobbies((ArrayList<String>) mapObj.get("hobbies"));
        setDel((Boolean) mapObj.get("isDel"));
    }

    protected User(Parcel in) {
        setKey(in.readString());
        setGender(in.readString());
        setName(in.readString());
        setAge((Long) in.readValue(Long.class.getClassLoader()));
        setPhoto(in.readString());
        setHobbies(in.createStringArrayList());
        setDel((Boolean) in.readValue(Boolean.class.getClassLoader()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(gender);
        dest.writeString(name);
        dest.writeValue(age);
        dest.writeString(photo);
        dest.writeStringList(hobbies);
        dest.writeValue(isDel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("gender", gender);
        result.put("name", name);
        result.put("age", age);
        result.put("photo", photo);
        result.put("isDel", isDel);
        result.put("hobbies", hobbies);

        return result;
    }

    public String getGender() {
        return gender;
    }

    public int getGenderText() {
        if (gender == null) {
            return R.string.other;
        }
        return isMale() ? R.string.male : (isFemale() ?  R.string.female : R.string.other);
    }
    public int getGenderColorId() {
        if (gender == null) {
            return R.color.white;
        }
        return isMale() ? R.color.color_male : (isFemale() ?  R.color.color_female : R.color.white) ;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setGenderFromId(int genderRadioId) {
        if (genderRadioId == R.id.male_radio) {
            setGender("m");
        }
        else if (genderRadioId == R.id.female_radio) {
            setGender("f");
        }
        else if (genderRadioId == R.id.other_radio) {
            setGender("o");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<String> getHobbies() {
        return hobbies;
    }

    public void setHobbies(List<String> hobbies) {
        this.hobbies = hobbies;
    }

    public Boolean getDel() {
        return isDel;
    }

    public Boolean isDeleted() {
        return Boolean.TRUE.equals(isDel);
    }

    public void setDel(Boolean del) {
        isDel = del;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "User{" +
                ", key=" + key +
                ", gender='" + gender + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", photo='" + photo + '\'' +
                ", hobbies=" + hobbies +
                ", isDel=" + isDel +
                '}';
    }

    public boolean isMale() {
        return "m".equals(gender);
    }

    public boolean isFemale() {
        return "f".equals(gender);
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(name) && age != null && age > 0 &&
                !TextUtils.isEmpty(gender);
    }

    public void addHobby(int id, String hobbyStr) {
        if (hobbies == null) {
            hobbies = new ArrayList<>();
        }
        hobbies.add(id, hobbyStr);
    }
}
