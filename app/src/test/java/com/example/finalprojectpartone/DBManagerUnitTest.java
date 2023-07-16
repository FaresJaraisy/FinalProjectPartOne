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
    DBManager dbManager = new DBManager(null, null);

    @Test(expected = DBManagerException.class)
    public void test_AddComment_method() throws DBManagerException {
        // Set up mock behavior
    }
}
