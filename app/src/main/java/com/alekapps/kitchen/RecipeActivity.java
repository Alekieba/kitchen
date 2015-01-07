package com.alekapps.kitchen;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;


public class RecipeActivity extends Activity {

    // Text Views
    private TextView mTitleTextView = null;
    private TextView mIngredientsTextView = null;
    private TextView mIngredientsListTextView = null;
    private TextView mInstructionsTextView = null;
    private TextView mInstructionsListTextView = null;
    private ImageView mImageView = null;

    // Other variables
    String query = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Get UI elements
        mTitleTextView = (TextView) findViewById(R.id.recipe_title_text_view);
        mIngredientsTextView = (TextView) findViewById(R.id.ingredients_text_view);
        mIngredientsListTextView = (TextView) findViewById(R.id.ingredients_list_text_view);
        mInstructionsTextView = (TextView) findViewById(R.id.instructions_text_view);
        mInstructionsListTextView = (TextView) findViewById(R.id.instructions_list_text_view);
        mImageView = (ImageView) findViewById(R.id.image_view);

        // Get the query
        Bundle extras = getIntent().getExtras();
        try {
            query = URLEncoder.encode(extras.getString(MyActivity.RECIPE_QUERY), "utf-8");
            setTitle(query);
        } catch (UnsupportedEncodingException e) {
            // handled
        }

        setup();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.recipe, menu);
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

    public void setup() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Get stuff from the site
                try {
                    // Get a list of recipes.
                    String recipeSiteHtml = new RequestTask().execute("http://www.food.com/recipe-finder/all/" + query).get();
                    Document doc = Jsoup.parse(recipeSiteHtml);
                    Elements recipes = doc.select(".recipe-main-title");

                    // Pick a recipe
                    int recipePosition = (int)(Math.random() * (recipes.size() - 1));

                    // Access the recipe's page
                    String recipeUrl = recipes.get(recipePosition).attr("href");
                    String recipeHtml = new RequestTask().execute(recipeUrl).get();
                    Document recipeDoc = Jsoup.parse(recipeHtml);

                    // Get the title
                    final String title = recipes.get(recipePosition).text();

                    // Get the image
                    Elements images = recipeDoc.select(".smallPageImage");
                    String imageUrl = images.first().attr("src");
                    new DownloadImage().execute(imageUrl);

                    // Get the instructions
                    Elements instructions = recipeDoc.select(".instructions .txt");
                    String instructionsString = "";
                    for (int i = 0; i < instructions.size(); i++) {
                        instructionsString += Integer.toString(i+1) + ". ";
                        instructionsString += instructions.get(i).text() +
                                ((i != instructions.size() - 1) ? "\n" : "");
                    }
                    final String finalInstructions = instructionsString;

                    // Get the ingredients
                    Elements ingredients = recipeDoc.select(".ingredients [itemprop]");
                    String ingredientsString = "";
                    for (int i = 0; i < ingredients.size(); i++) {
                        ingredientsString += Integer.toString(i+1) + ". ";
                        Elements value = ingredients.get(i).select(".value");
                        Elements type = ingredients.get(i).select(".type");
                        Elements name = ingredients.get(i).select(".name");
                        ingredientsString += value.text() + " " + type.text() + " " + name.text() +
                                ((i != ingredients.size() - 1) ? "\n" : "");
                    }
                    final String finalIngredients = ingredientsString;

                    // UI thread stuff
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mTitleTextView.setText(title);
                            mIngredientsTextView.setText(getString(R.string.ingredients));
                            mIngredientsListTextView.setText(finalIngredients);
                            mInstructionsTextView.setText(getString(R.string.instructions));
                            mInstructionsListTextView.setText(finalInstructions);
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();
    }


    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            mImageView.setImageBitmap(result);
        }
    }
}
