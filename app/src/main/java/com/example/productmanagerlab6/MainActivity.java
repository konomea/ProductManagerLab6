package com.example.productmanagerlab6;

import static android.widget.Toast.LENGTH_LONG;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;
    ProductList productsAdapter;

    DatabaseReference databaseProducts;
    List<Product> products;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        listViewProducts =(ListView) findViewById(R.id.listViewProducts);
        buttonAddProduct = (Button) findViewById((R.id.addButton));

        databaseProducts = FirebaseDatabase.getInstance().getReference("products");
        products = new ArrayList<>();
        productsAdapter = new ProductList(MainActivity.this, products);
        listViewProducts.setAdapter(productsAdapter);

        //adding an onClickListener to button
        buttonAddProduct.setOnClickListener(view -> addProduct());

        listViewProducts.setOnItemLongClickListener((adapterView, view, i, l) -> {
            Product product = products.get(i);
            showUpdateDeleteDialog(product.getId(), product.getProductName());
            return true;
        });
    }
    @Override
    protected void onStart(){
        super.onStart();

        //attaching value event listener
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //clearing the previous artist list
                products.clear();

                //iterating through all the nodes

                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    //getting product
                    Product product = postSnapshot.getValue(Product.class);
                    //adding product to the list
                    products.add(product);
                }
                productsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // return;
            }
        });

    }

    private void showUpdateDeleteDialog(final String productId,String productName){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater= getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName =(EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice = (EditText) dialogView.findViewById((R.id.editTextPrice));
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(view -> {
            String name = editTextName.getText().toString().trim();
            double price = Double.parseDouble(editTextPrice.getText().toString());
            if(!TextUtils.isEmpty(name)){
                updateProduct(productId, name, price);
                b.dismiss();
            }

        });

        buttonDelete.setOnClickListener(view -> {
            deleteProduct(productId);
            b.dismiss();
        });
    }
    private void updateProduct(String id, String name, double price){
        //getting the specified product reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);
        //updating product
        Product product = new Product(id, name, price);
        dR.setValue(product);
        Toast.makeText(getApplicationContext(), "Product Updated", Toast.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), "NOT IMPLEMENTED YET", Toast.LENGTH_LONG).show();
    }

    private void deleteProduct(String id){
        //getting the specified product reference
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);
        //removing product
        dR.removeValue();
        Toast.makeText(getApplicationContext(), "Product Deleted", LENGTH_LONG).show();
    }

    private void addProduct(){
        //getting the values to save
        String name = editTextName.getText().toString().trim();
        double price = Double.parseDouble(editTextPrice.getText().toString());

        //checking if the value is provided
        if(!TextUtils.isEmpty(name)){
            //getting a unique id using push().getKey() method
            // it will create a unqiue id and we will use it as the primary key for the method
            String id = databaseProducts.push().getKey();

            //creating a Product object
            Product product = new Product( id, name, price);

            //saving the product
            databaseProducts.child(id).setValue(product);

            //setting editText to blank again
            editTextName.setText("");
            editTextPrice.setText("");

            //displaying a success toast
            Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
        }

    }





}