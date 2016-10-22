package io.github.yusukeiwaki.realmsample2;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Word extends RealmObject {
    @PrimaryKey
    private String id;
    private long createdAt;
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
