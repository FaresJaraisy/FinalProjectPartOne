package com.example.finalprojectpartone;

import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import android.content.ContentValues;

import database.DBManager;
import database.DBManagerException;
import database.DatabaseHelper;

public class DBManagerUnitTest {
    @Mock
    ContentValues mockContentValues = new ContentValues();
    DBManager dbManager = new DBManager(null);

    @Test(expected = DBManagerException.class)
    public void test_AddComment_method() throws DBManagerException {
        // Set up mock behavior
        when(mockContentValues.get(DatabaseHelper.CREATOR_COL)).thenReturn("miras@gmail.com");
        when(mockContentValues.get(DatabaseHelper.CONTENT_COL)).thenReturn("Lorem ipsum");
        when(mockContentValues.get(DatabaseHelper.EVENT_ID_COL)).thenReturn(1);

        dbManager.addComment("miras@gmail.com","Lorem ipsum", 1);
        verify(mockContentValues).get(DatabaseHelper.CREATOR_COL);

    }
}
