package com.jadem.androidpitscout;

/**
 * Created by jadem on 3/17/2018.
 */

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FirebaseList<T> {
    private List<String> keys = new ArrayList<>();
    private List<T> values = new ArrayList<>();

    public FirebaseList(String url, FirebaseUpdatedCallback firebaseUpdatedCallback, Class<T> firebaseClass) {
        setupFirebaseListening(url, firebaseClass, firebaseUpdatedCallback);
    }

    public void setupFirebaseListening(String url, final Class<T> firebaseClass, final FirebaseUpdatedCallback firebaseUpdatedCallback) {
        String childString = "";
        if(url.contains("Matches")){
            childString = "Matches";
        } else if(url.contains("TeamInMatchDatas")){
            childString = "TeamInMatchDatas";
        } else if(url.contains("Teams")){
            childString = "Teams";
        }
        DatabaseReference firebase = FirebaseDatabase.getInstance().getReference().getRef().child(childString);
        firebase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                Log.e("Datasnapshot", dataSnapshot.toString() + " " + key);
                T model = dataSnapshot.getValue(firebaseClass);


                // Insert into the correct location, based on s
                if (s == null) {
                    values.add(0, model);
                    keys.add(0, key);
                } else {
                    int previousIndex = keys.indexOf(s);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == values.size()) {
                        values.add(model);
                        keys.add(key);
                    } else {
                        values.add(nextIndex, model);
                        keys.add(nextIndex, key);
                    }
                }
                firebaseUpdatedCallback.execute();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                T newModel = dataSnapshot.getValue(firebaseClass);
                int index = keys.indexOf(key);

                values.set(index, newModel);

                firebaseUpdatedCallback.execute();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = keys.indexOf(key);

                keys.remove(index);
                values.remove(index);

                firebaseUpdatedCallback.execute();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                String key = dataSnapshot.getKey();
                T newModel = dataSnapshot.getValue(firebaseClass);
                int index = keys.indexOf(key);
                values.remove(index);
                keys.remove(index);
                if (s == null) {
                    values.add(0, newModel);
                    keys.add(0, key);
                } else {
                    int previousIndex = keys.indexOf(s);
                    int nextIndex = previousIndex + 1;
                    if (nextIndex == values.size()) {
                        values.add(newModel);
                        keys.add(key);
                    } else {
                        values.add(nextIndex, newModel);
                        keys.add(nextIndex, key);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e("Firebase List", "Listen was cancelled, no more updates will occur");
            }
        });
    }

    public interface FirebaseUpdatedCallback {
        void execute();
    }

    public T getFirebaseObjectByKey(String key) {
        int index = keys.indexOf(key);
        return values.get(index);
    }

    public List<String> getKeys() {
        return keys;
    }

    public List<T> getValues() {
        return values;
    }
}
