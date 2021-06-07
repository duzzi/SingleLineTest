最近在做迭代的时候碰到`ViewPager`无法滑动的问题，排查后发现在`ViewPager`中的`TextView`修改了`ingleLine=true`和`gravity`，触摸`extView`后无法左右滑动`ViewPager`，具体情况总结如下：


```
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="5dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:orientation="vertical">

        <TextView
            android:id="@+id/textView"
            android:layout_width="match_parent"
            android:layout_height="100dp"
            android:layout_gravity="center_horizontal"
            android:background="#22ffff00"
            android:gravity="center"
            android:textColor="#000000"
            android:textSize="22sp" />

        <TextView
            android:background="#22ffff00"
            android:singleLine="false"
            android:text="当仅设置singleLine=false时，不影响ViewPager滑动"
            style="@style/text_style" />

        <TextView
            style="@style/text_style"
            android:background="#22ffff00"
            android:singleLine="true"
            android:text="当仅设置singleLine=true时，不影响ViewPager滑动" />

        <TextView
            style="@style/text_style"
            android:layout_gravity="center_horizontal"
            android:background="#22ffff00"
            android:singleLine="true"
            android:text="当singleLine=true且gravity为默认值start|top，不影响ViewPager滑动" />

        <TextView
            style="@style/text_style"
            android:background="#22ffff00"
            android:gravity="center"
            android:singleLine="true"
            android:text="当singleLine=true且gravity=center，会影响ViewPager滑动"
            android:textColor="#F44336"
            android:textStyle="bold" />

        <TextView
            style="@style/text_style"
            android:background="#22ffff00"
            android:gravity="right|center_vertical"
            android:singleLine="true"
            android:text="当singleLine=true且gravity=right，会影响ViewPager滑动"
            android:textColor="#F44336"
            android:scrollHorizontally="false"
            android:textStyle="bold" />

        <com.duzzi.singleline.UnScrollTextView
            style="@style/text_style"
            android:background="#22ffff00"
            android:gravity="center_vertical"
            android:singleLine="true"
            android:text="自定义TextView重写canScrollHorizontally返回false，可正常滑动ViewPager"
            android:textColor="#009688" />

        <TextView
            android:id="@+id/manualUnScrollTextView"
            style="@style/text_style"
            android:background="#22ffff00"
            android:gravity="right"
            android:singleLine="true"
            android:text="手动设置TextView为setHorizontallyScrolling(false)，可正常滑动ViewPager"
            android:textColor="#009688"
            android:scrollHorizontally="false"
            android:textStyle="bold" />

        <TextView
            style="@style/text_style"
            android:background="#22ffff00"
            android:gravity="right"
            android:maxLines="1"
            android:text="使用maxLines=1，可正常滑动ViewPager"
            android:textColor="#009688"
            android:scrollHorizontally="false"
            android:textStyle="bold" />


    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

通过查看源码发现:
`singleLine`约束了text的横向滑动，但该属性已经过时

```
        <!-- Constrains the text to a single horizontally scrolling line
             instead of letting it wrap onto multiple lines, and advances
             focus instead of inserting a newline when you press the
             enter key.

             The default value is false (multi-line wrapped text mode) for non-editable text, but if
             you specify any value for inputType, the default is true (single-line input field mode).

             {@deprecated This attribute is deprecated. Use <code>maxLines</code> instead to change
             the layout of a static text, and use the <code>textMultiLine</code> flag in the
             inputType attribute instead for editable text views (if both singleLine and inputType
             are supplied, the inputType flags will override the value of singleLine). } -->
        <attr name="singleLine" format="boolean" />
```

`View.java`中的`canScrollHorizontally`会判断`View`是否可以横向滑动
```
    /**
     * Check if this view can be scrolled horizontally in a certain direction.
     *
     * @param direction Negative to check scrolling left, positive to check scrolling right.
     * @return true if this view can be scrolled in the specified direction, false otherwise.
     */
    public boolean canScrollHorizontally(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range =
        
        computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }
```
而`TextView`重写了`computeHorizontalScrollRange()`，可以看到这里`mSingleLine=true`且修改了`mGravity`后，就没有调用`View`的`computeHorizontalScrollRange()`，导致`TextView`的`canScrollHorizontally`返回`true`，影响了后续横向滑动事件

```
    @Override
    protected int computeHorizontalScrollRange() {
        if (mLayout != null) {
            return mSingleLine && (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT
                    ? (int) mLayout.getLineWidth(0) : mLayout.getWidth();
        }

        return super.computeHorizontalScrollRange();
    }
```

解决方法：
1.使用`maxLine="1"`
2.使用`ingleLine="true"`时，不要修改`gravity`
3.自定义`extView`重写`anScrollHorizontally`返回`false`

```
    override fun canScrollHorizontally(direction: Int): Boolean {
        return false
    }
```
代码：https://github.com/duzzi/SingleLineTest


参考：
https://stackoverflow.com/questions/16915040/can-not-scroll-the-viewpager-when-touching-textviewwith-androidgravity-center



