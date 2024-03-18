package com.example.recipeapp

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var listView: ListView

    private val apiKey = "d88d5383f2dc4379a0d33bae22529ab7"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinner = findViewById(R.id.spinnerRecipeTypes)
        listView = findViewById(R.id.listViewRecipes)

        val recipeTypes = listOf("Main Course", "Dessert", "Appetizer", "Side Dish", "Snack")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRecipeType = parent?.getItemAtPosition(position).toString()
                FetchRecipesTask().execute(selectedRecipeType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    inner class FetchRecipesTask : AsyncTask<String, Void, List<Recipe>>() {

        override fun doInBackground(vararg params: String?): List<Recipe> {
            val recipeType = params[0]
            val recipes = mutableListOf<Recipe>()

            try {
                val url = URL("https://api.spoonacular.com/recipes/complexSearch?apiKey=$apiKey&type=$recipeType&number=10")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonObject = JSONObject(response.toString())
                    val jsonArray = jsonObject.getJSONArray("results")
                    for (i in 0 until jsonArray.length()) {
                        val recipeObject = jsonArray.getJSONObject(i)
                        val title = recipeObject.getString("title")
                        val imageUrl = recipeObject.getString("image")
                        recipes.add(Recipe(title, imageUrl))
                    }
                } else {
                    Log.e("FetchRecipesTask", "HTTP Error: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("FetchRecipesTask", "Error: ${e.message}")
            }

            return recipes
        }

        override fun onPostExecute(result: List<Recipe>?) {
            result?.let {
                val adapter = RecipeAdapter(it)
                listView.adapter = adapter
            }
        }
    }

    inner class RecipeAdapter(private val recipes: List<Recipe>) :
        ArrayAdapter<Recipe>(this@MainActivity, R.layout.list_item_recipe, recipes) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.list_item_recipe, parent, false)

            val recipe = recipes[position]
            val textRecipeTitle = view.findViewById<TextView>(R.id.textRecipeTitle)
            val imageRecipe = view.findViewById<ImageView>(R.id.imageRecipe)

            textRecipeTitle.text = recipe.title

            // Load image using Picasso library
            Picasso.get().load(recipe.imageUrl).placeholder(R.drawable.ic_launcher_background).into(imageRecipe)

            return view
        }
    }
}

data class Recipe(val title: String, val imageUrl: String)