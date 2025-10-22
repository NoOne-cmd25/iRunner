package com.example.myapplication

import java.lang.reflect.Constructor

class Blog (name:String,pic:Int ,text:String ){
    var name: String=name
    var pic:Int=pic
    var text:String=text
    fun pic():Int
    {
        return this.pic
    }
    fun name():String
    {
        return this.name
    }
    fun text():String
    {
        return this.text
    }
//    fun Constructor(name:String,pic:String ,text:String )
//    {
//        this.name =name
//        this.pic=pic
//        this.text=text
//    }
}