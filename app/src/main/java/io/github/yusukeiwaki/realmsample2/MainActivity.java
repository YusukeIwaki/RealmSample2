package io.github.yusukeiwaki.realmsample2;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.UUID;

import io.realm.Credentials;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.User;
import io.realm.rx.RealmObservableFactory;
import jp.co.crowdworks.realm_java_helpers.RealmHelper;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.internal.util.SubscriptionList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    Realm mRealm;
    private SubscriptionList mSubs;
    private static final String SERVER = "realm.example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ProgressDialog dialog = ProgressDialog.show(this, "Configuring Realm", "logging in...", true, false);
        User.loginAsync(Credentials.usernamePassword("iwaki.yusuke@example.com", "hogehoge", false), "http://"+SERVER+":9080/auth", new User.Callback() {
            @Override
            public void onSuccess(User user) {
                SyncConfiguration config = new SyncConfiguration.Builder(user, "realm://"+SERVER+":9080/~/default")
                        .rxFactory(new RealmObservableFactory())
                        .build();
                Realm.setDefaultConfiguration(config);

                dialog.dismiss();
                setup();
            }

            @Override
            public void onError(ObjectServerError error) {
                Log.e(TAG, "error", error);
                dialog.dismiss();
                finish();
            }
        });
    }

    private void setup() {
        mRealm = Realm.getDefaultInstance();
        RealmResults<Word> words = mRealm.where(Word.class).findAll();
        mSubs = new SubscriptionList();
        mSubs.add(words.asObservable()
                .filter(new Func1<RealmResults<Word>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<Word> words) {
                        return words!=null && !words.isEmpty();
                    }
                })
                .map(new Func1<RealmResults<Word>, Word>() {
                    @Override
                    public Word call(RealmResults<Word> words) {
                        return words.last();
                    }
                })
                .map(new Func1<Word, String>() {
                    @Override
                    public String call(Word word) {
                        return word.getValue();
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String word) {
                        render(word);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d(TAG,"error", throwable);
                    }
                })
        );

        setupComposer();
    }

    private void setupComposer() {
        findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String word = ((TextView) findViewById(R.id.editor)).getText().toString();
                insert(word);
            }
        });
    }

    private void insert(final String word) {
        RealmHelper.rxExecuteTransactionAsync(new RealmHelper.Transaction() {
            @Override
            public Object execute(Realm realm) throws Throwable {
                Word w = realm.createObject(Word.class, UUID.randomUUID().toString());
                w.setValue(word);
                w.setCreatedAt(System.currentTimeMillis());
                return null;
            }
        }).subscribe();
    }

    private void render(String word) {
        ((TextView)findViewById(R.id.text)).setText(word);
    }

    @Override
    protected void onDestroy() {
        if (mSubs != null && !mSubs.isUnsubscribed()) mSubs.unsubscribe();
        if (mRealm!=null) mRealm.close();
        super.onDestroy();
    }
}
