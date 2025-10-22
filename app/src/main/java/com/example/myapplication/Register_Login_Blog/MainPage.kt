package com.example.myapplication

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.*
//import android.widget.*
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.util.Log
import java.util.ArrayList

class MainPage : AppCompatActivity() ,AdapterView.OnItemClickListener{

    val list=arrayListOf<Blog>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent=intent
        val sql= MySqliteOpenHelper_blog(this)
        val db= sql.readableDatabase
        val blog=Blog("小明",R.drawable.baosi,"暴死！")
        list.add(blog)
        val blog4=Blog("王五",0,"行百里者半九十")
        list.add(blog4)
        val blog1=Blog("张三",R.drawable.halo,"又出bug了？")
        list.add(blog1)
        val blog2=Blog("李四",R.drawable.dragon,"勇者斗恶龙。")
        list.add(blog2)

        val cursor=db.rawQuery("select * from blog2",null)
        cursor.moveToFirst()
        while(cursor.isAfterLast==false)
        {
//            cursor.getString(1)
//            Log.d("tag",cursor.getString(0)+cursor.getString(1))
            list.add(Blog(cursor.getString(0),cursor.getInt(2),cursor.getString(1)))
            cursor.moveToNext()
        }

        val name: String =intent.getStringExtra("name")!!
//        Log.d("tag",name)
        enableEdgeToEdge()
        setContentView(R.layout.main_page)
        val toolbar=findViewById<Toolbar>(R.id.toolbar);
        val edittext=findViewById<EditText>(R.id.editTextText)

        val listview=findViewById<ListView>(R.id.listview)

//        val list=arrayListOf<String>("1","2","3","4")

        val adapter= BlogAdapter(this,R.layout.blog_item,list)

        listview.setAdapter(adapter)

        listview.setOnItemClickListener(this)






        edittext.setText("hello,"+name)
//        setSupportActionBar(toolbar)

        toolbar.inflateMenu(R.menu.toolbar_menu)
        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.action_post->{
//                    Log.d("wrong","1")
                    val intent= Intent(this, PostBlog::class.java)
                    intent.putExtra("name",name)
                    startActivity(intent)
                }
                R.id.action_logout->{
//                    Log.d("wrong","1")
                    val intent= Intent(this, MainActivity1::class.java)
                    startActivity(intent)
                }
                R.id.action_run->{
//                    Log.d("wrong","2")
                val intent= Intent(this, MainActivity::class.java)
                startActivity(intent)
                }
                else -> {
//                    Log.d("wrong", "2")
                    super.onOptionsItemSelected(it)
                }
            }
            true
        }


    }

    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val res=list[p2]
        Log.d("Tag",res.text())
    }
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        return super.onCreateOptionsMenu(menu)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.action_logout->{
//                Log.d("wrong","1")
//                val intent= Intent(this, MainActivity::class.java)
//                startActivity(intent)
//            }
//            R.id.action_run->{
//                Log.d("wrong","2")
////                val intent= Intent(this, MainActivity::class.java)
////                startActivity(intent)
//            }
//            else -> {
//                Log.d("wrong", "2")
//                super.onOptionsItemSelected(item)
//            }
//        }
//        return true
//    }

}