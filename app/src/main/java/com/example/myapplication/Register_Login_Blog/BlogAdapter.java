package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class BlogAdapter extends ArrayAdapter<Blog> {
    public BlogAdapter(Context context, int src, List<Blog> object)
    {
        super(context,src,object);
    }

    public View getView(int position,  View convertView, ViewGroup parent) {
        Blog blog=getItem(position);//得到当前项的 Blog 实例
        //为每一个子项加载设定的布局
        View view;
        if(blog.pic()!=0)
            view= LayoutInflater.from(getContext()).inflate(R.layout.blog_item,parent,false);
        else
            view= LayoutInflater.from(getContext()).inflate(R.layout.blog_item_nopic,parent,false);
        //分别获取 image view 和 textview 的实例
        ImageView blogimage =view.findViewById(R.id.blog_image);
        TextView blogname =view.findViewById(R.id.blog_name);
        TextView blogprice=view.findViewById(R.id.blog_text);
        // 设置要显示的图片和文字
        if(blog.pic()!=0)
            blogimage.setImageResource(blog.pic());
        blogname.setText(blog.name());
        blogprice.setText(blog.text());
        return view;
    }

}
