package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.database.sqlite.*

class MainActivity1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main1)
        val name=this.findViewById<EditText>(R.id.name);
        val password =this.findViewById<EditText>(R.id.password);
        val confirm_password=this.findViewById<EditText>(R.id.confirm_password);
        val login=this.findViewById<Button>(R.id.button_login);
        val register=this.findViewById<Button>(R.id.button_register);
        val sql= MySqliteOpenHelper(this)
        //login
        login.setOnClickListener {
            val db=sql.readableDatabase
//            if (name.getText().toString().trim().equals("123") &&
//                password.getText().toString().trim().equals("123123") &&
//                confirm_password.getText().toString().trim()
//                    .equals(password.getText().toString().trim()))
            val cursor=db.rawQuery("select * from user where name="+(name.getText().toString().trim()),null)
            cursor.moveToFirst()
//            Log.d(null,cursor.getString(1))
            if(name.getText().toString().trim()!="" &&
                password.getText().toString().trim()!="" &&
                confirm_password.getText().toString().trim().equals(password.getText().toString().trim()) &&
                cursor.count!=0 &&
                cursor.getString(1).equals(password.getText().toString().trim())
                )
            {
                Toast.makeText(this@MainActivity1, "Login successfully", Toast.LENGTH_SHORT).show()
                val intent=Intent(this, MainPage::class.java)
                intent.putExtra("name",name.getText().toString().trim())
                startActivity(intent)
//                setContentView(R.layout.main_page)
            }
            else {
                Toast.makeText(
                    this@MainActivity1,
                    "Please input correct name or password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //register
        register.setOnClickListener {
//            setContentView(R.layout.activity_register)
            val intent=Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}