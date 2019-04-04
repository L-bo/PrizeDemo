# PrizeDemo

* attrs介绍

~~~
<declare-styleable name="PrizeView">
    <!--文字大小-->
    <attr name="prize_text_size"/>
    <!--文字颜色-->
    <attr name="prize_text_color" format="color" />
    <!--设置文字内容-->
    <attr name="prize_text_content" format="string" />
    <!--是否加粗-->
    <attr name="prize_text_type" format="boolean" />
    <!--设置背景图片-->
    <attr name="prize_bag_image" format="reference" />
    <!--遮盖层的颜色-->
    <attr name="prize_cover_color" format="color"/>
</declare-styleable>
~~~

* 使用时布局引用

~~~
<com.example.prizedemo.view.PrizeView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:prize_bag_image="@mipmap/back"
    app:prize_cover_color="#7c7c7c"
    app:prize_text_color="#000000"
    app:prize_text_content="$100"
    app:prize_text_size="30sp"
    app:prize_text_type="true" />
~~~

> 可以再代码中设置这些属性
 
![效果图](https://img-blog.csdnimg.cn/20190404171829901.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3dlaXhpbl80MzczNjY2NQ==,size_16,color_FFFFFF,t_70)


