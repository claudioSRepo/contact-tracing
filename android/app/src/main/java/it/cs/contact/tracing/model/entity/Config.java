package it.cs.contact.tracing.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Entity
@AllArgsConstructor
public class Config {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "key")
    private String key;

    @ColumnInfo(name = "value")
    private String value;
}
