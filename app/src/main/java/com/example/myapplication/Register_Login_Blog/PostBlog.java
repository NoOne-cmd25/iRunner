package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PostBlog extends AppCompatActivity implements View.OnClickListener{
    EditText text;
    Button button;
    String name;

    MySqliteOpenHelper_blog sql=new MySqliteOpenHelper_blog(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_post);
        text=findViewById(R.id.blog_text_post);
        button=findViewById(R.id.Post_Button);
        Log.d("Wrong","1");
        text.setHint("Please input your text.");
        Log.d("Wrong","1");
        Log.d("Wrong","1");
        Intent get_intent=getIntent();
        Log.d("Wrong","1");
        name=get_intent.getStringExtra("name");
        Log.d("name",name);
        Log.d("Wrong","1");
        button.setOnClickListener(this);
    }
    @Override
    public void onClick(View v)
    {
        SQLiteDatabase db =sql.getWritableDatabase();
        if(text.getText().toString().trim().isEmpty())
        {
            Toast.makeText(this,"Text can't be empty",Toast.LENGTH_SHORT).show();
        }
        else
        {
//            Cursor cursor=db.rawQuery("select * from blog1 where name="+name+" and text="+text.getText().toString().trim(),null);
//            cursor.moveToFirst();
//            if(cursor.getCount()!=0)
//            {
//                Toast.makeText(this,"Don't post same blog again",Toast.LENGTH_SHORT).show();
//            }
//            else {
//                Intent intent = new Intent(this, MainPage.class);
//                db.execSQL("insert into blog1 values('" + name + "','" + text.getText().toString().trim() + "',0" + ")");
//                Log.d("Tag", text.getText().toString().trim());
////            intent.putExtra("Text",text.getText().toString().trim());
////            intent.putExtra("Name",name);
//                startActivity(intent);
//            }
            try {
                Cursor cursor = db.rawQuery("select exists (select * from blog2 where name=" + name + " and text=" + text.getText().toString().trim()+")", null);
                cursor.moveToFirst();
                Toast.makeText(this,"Don't post same blog again",Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                int cnt=db.rawQuery("select 1 from (select * from blog2)", null).getCount();
                Intent intent = new Intent(this, MainPage.class);
                db.execSQL("insert into blog2 values('" + name + "','" + text.getText().toString().trim() + "',0," +cnt+ ")");
                Log.d("Tag", text.getText().toString().trim());
                Toast.makeText(this,"Post successfully",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        }
    }

}
