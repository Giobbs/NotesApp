package com.example.notesapp.data.local;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.notesapp.security.CryptoManager;

@Entity(tableName = "notes")
public class Note {

    // ID locale (Room)
    @PrimaryKey(autoGenerate = true)
    public long id;

    // UUID per sync remoto
    @NonNull
    public String uuid = "";

    // Contenuto principale
    @NonNull
    public String title = "";

    @NonNull
    public String content = "";

    // Metadati UI
    public boolean pinned = false;

    public boolean archived = false;

    public boolean deleted = false; // soft delete

    // Organizzazione
    @NonNull
    public String tags = ""; // CSV semplice

    @NonNull
    public String color = "#FFFFFF"; // personalizzazione UI

    // Priorità nota
    public int priority = 0; // 0=low, 1=medium, 2=high

    // Reminder (timestamp, opzionale)
    public Long reminderAt;

    // Audit
    public long createdAt;

    public long updatedAt;

    public long lastOpenedAt;

     public boolean synced = false;

    public long syncVersion = 0;

    // 🔐 SECURITY
    public boolean isProtected = false;
    public String encryptedContent;
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    public void setUuid(@NonNull String uuid) {
        this.uuid = uuid;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getContent() {
        return content;
    }

    public void setContent(@NonNull String content) {
        this.content = content;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @NonNull
    public String getTags() {
        return tags;
    }

    public void setTags(@NonNull String tags) {
        this.tags = tags;
    }

    @NonNull
    public String getColor() {
        return color;
    }

    public void setColor(@NonNull String color) {
        this.color = color;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Long getReminderAt() {
        return reminderAt;
    }

    public void setReminderAt(Long reminderAt) {
        this.reminderAt = reminderAt;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getLastOpenedAt() {
        return lastOpenedAt;
    }

    public void setLastOpenedAt(long lastOpenedAt) {
        this.lastOpenedAt = lastOpenedAt;
    }

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public long getSyncVersion() {
        return syncVersion;
    }

    public void setSyncVersion(long syncVersion) {
        this.syncVersion = syncVersion;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean aProtected) {
        isProtected = aProtected;
    }
    public String getSafeContent() {
        if (isProtected && encryptedContent != null) {
            return CryptoManager.decrypt(encryptedContent);
        }
        return content;
    }
    public String getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }
}