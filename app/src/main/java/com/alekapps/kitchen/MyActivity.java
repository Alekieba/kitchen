package com.alekapps.kitchen;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutionException;


public class MyActivity extends Activity {

    // Constant
    public static final String RECIPE_QUERY = "com.alekapps.kitchen.query";

    // Our search box
    private EditText mSearchBox = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        // Find our search box.
        mSearchBox = (EditText) findViewById(R.id.search_edit_text);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user presses the search button.
     * @param view
     */
    public void search(View view) {

        // Take the current search box text.
        String query = mSearchBox.getText().toString().trim();

        // Send the query to a new activity.
        if (query.length() > 0) {
            Intent intent = new Intent(getBaseContext(), RecipeActivity.class);
            intent.putExtra(RECIPE_QUERY, query);
            startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_message)
                    .setTitle(R.string.dialog_title)
                    .setPositiveButton(R.string.dialog_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSearchBox.setText("");
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }
}
