package com.example.myapplication

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        val name= this.findViewById<EditText>(R.id.name1)
        val password= this.findViewById<EditText>(R.id.password1)
        val confirm_password = this.findViewById<EditText>(R.id.confirm_password1)
        val back_to_login = this.findViewById<Button>(R.id.button_back_login)
        val register = this.findViewById<Button>(R.id.button_register1)
        val sql= MySqliteOpenHelper(this)
        //back to login page
        back_to_login.setOnClickListener {
            val intent=Intent(this, MainActivity1::class.java);
            startActivity(intent);
        }

        //register
        register.setOnClickListener {
            val db=sql.readableDatabase
            if(name.getText().toString().trim()!="" &&
                password.getText().toString().trim()!="" &&
                confirm_password.getText().toString().trim().equals(password.getText().toString().trim()) &&
                db.rawQuery("select * from user where name="+(name.getText().toString().trim()),null).count==0)
            {
                db.execSQL("insert into user values("+name.getText().toString().trim()+","+password.getText().toString().trim()+")")
                val intent=Intent(this, MainActivity1::class.java);
                Toast.makeText(this, "Register successfully!", Toast.LENGTH_SHORT).show()
                startActivity(intent);
            }
            else
            {
                Toast.makeText(
                    this,
                    "Please input correct name or password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }
}
