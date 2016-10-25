/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.

Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.Books.app.Books;

import com.vuforia.samples.Books.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

// Custom View with Book Overlay Data
public class BookOverlayView extends RelativeLayout {
    public BookOverlayView(Context context) {
        this(context, null);
    }


    public BookOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public BookOverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflateLayout(context);

    }


    // Inflates the Custom View Layout
    private void inflateLayout(Context context) {

        final LayoutInflater inflater = LayoutInflater.from(context);

        // Generates the layout for the view
        inflater.inflate(R.layout.bitmap_layout, this, true);
    }


    // Sets Book title in View
    public void setBookTitle(String bookTitle) {
        TextView tv = (TextView) findViewById(R.id.custom_view_title);
        tv.setText(bookTitle);
    }


    // Sets Book Author in View
    public void setBookAuthor(String bookAuthor) {
        TextView tv = (TextView) findViewById(R.id.custom_view_author);
        tv.setText(bookAuthor);
    }

    public void setAll(HashMap<Integer, HashMap<Integer, List<String>>> hs, int object, int side) {
        HashMap<Integer, List<String>> hashOfStrings = hs.get(object);
        //List<String> list = hashOfStrings.get(side);
        if (object == 0 && side == 0) {
            Toast.makeText(getContext(), "inside of set all" , Toast.LENGTH_SHORT).show();
            if (!Books.a001.equals("")) {
                LinearLayout tv = (LinearLayout) findViewById(R.id.custom_view_book_horizontal);
                tv.setVisibility(VISIBLE);
            } else if (Books.a001.equals("")) {
                LinearLayout tv = (LinearLayout) findViewById(R.id.custom_view_book_horizontal);
                tv.setVisibility(GONE);
            }
            if (!Books.a002.equals("")) {

                byte[] thumb = downloadImage("http://cdn-wov.anitaborg.org/wp-content/uploads/sites/4/2016/03/alyssia-jovellanos-700x467.jpg");

                if (thumb != null) {

                    Bitmap bitmap = BitmapFactory.decodeByteArray(thumb, 0,
                            thumb.length);

                    ImageView iv = (ImageView) findViewById(R.id.custom_view_book_cover);
                    iv.setImageBitmap(bitmap);
                }
            } // end if
                TextView tv = (TextView) findViewById(R.id.custom_view_title);
                tv.setText(Books.a003);

            tv = (TextView) findViewById(R.id.custom_view_author);
            tv.setText(Books.a004);

                tv = (TextView) findViewById(R.id.under_company);
                tv.setText(Books.a005);
            tv.setVisibility(GONE);

                tv = (TextView) findViewById(R.id.textView);
                tv.setText(Books.a005);
            //REMOVE THIS AND REMEMBER YEAH THATS IT
                //tv.setVisibility(GONE);

                LinearLayout tvs = (LinearLayout) findViewById(R.id.linearLayout3);
                tvs.setVisibility(VISIBLE);

            tv = (TextView) findViewById(R.id.secondList_text1);
            tv.setText(Books.a007);
            tv.setTextColor(Color.parseColor("#00ff00"));

            ImageView tvss = (ImageView) findViewById(R.id.graph);
            tvss.setVisibility(GONE);

            tv = (TextView) findViewById(R.id.secondList_text2);
            tv.setText(Books.a009);
            tv.setTextColor(Color.parseColor("#ffff00"));

            tv = (TextView) findViewById(R.id.secondList_text3);
            tv.setText(Books.a0010);
            tv.setTextColor(Color.parseColor("#ff0000"));

            tv = (TextView) findViewById(R.id.secondList_text4);
            tv.setText(Books.a0011);

            tv = (TextView) findViewById(R.id.secondList_text5);
            tv.setText(Books.a0012);

            tv = (TextView) findViewById(R.id.secondList_text6);
            tv.setText(Books.a0013);

        }


    if(object == 0 && side==1)
    {
        LinearLayout tvs = (LinearLayout) findViewById(R.id.custom_view_book_horizontal);
        tvs.setVisibility(GONE);

        TextView tv = (TextView) findViewById(R.id.custom_view_title);
        tv.setText(Books.a003);
        //tv.setVisibility(GONE);

        tv = (TextView) findViewById(R.id.custom_view_author);
        tv.setText(Books.a004);
        //tv.setVisibility(GONE);

        tv = (TextView) findViewById(R.id.under_company);
        tv.setText(Books.a005);
        //tv.setVisibility(GONE);

        tv = (TextView) findViewById(R.id.textView);
        tv.setText(Books.a005);
        //tv.setVisibility(GONE);
    }

        if (object == 1){
            TextView tv = (TextView) findViewById(R.id.custom_view_title);
            tv.setText("Coca cola");

            tv = (TextView) findViewById(R.id.custom_view_author);
            tv.setText("2 packages");

            tv = (TextView) findViewById(R.id.under_company);
            tv.setText("2 packages left");
            tv.setVisibility(GONE);

            tv = (TextView) findViewById(R.id.textView);
            tv.setText("Packages are of sets of 12");

            tv = (TextView) findViewById(R.id.secondList_text1);
            tv.setText("3 Packages sold yesterday!");
            tv.setTextColor(Color.parseColor("#00ff00"));

            ImageView tvss = (ImageView) findViewById(R.id.graph);
            tvss.setVisibility(GONE);

            tv = (TextView) findViewById(R.id.secondList_text2);
            tv.setText("$16 gross and $5 net");
            tv.setTextColor(Color.parseColor("#00ff00"));

            tv = (TextView) findViewById(R.id.secondList_text3);
            tv.setText("You need to order more!");
            tv.setTextColor(Color.parseColor("#ff0000"));

            tv = (TextView) findViewById(R.id.secondList_text4);
            tv.setText(Books.a0011);
            tv.setVisibility(GONE);

            tv = (TextView) findViewById(R.id.secondList_text5);
            tv.setText(Books.a0012);
            tv.setVisibility(GONE);

            tv = (TextView) findViewById(R.id.secondList_text6);
            tv.setText(Books.a0013);
            tv.setVisibility(GONE);

        }
    }

    // Sets Book Price in View
    public void setBookPrice(String bookPrice)
    {
        //TextView tv = (TextView) findViewById(R.id.custom_view_price_old);
        //tv.setText(getContext().getString(R.string.string_$) + bookPrice);
    }
    
    
    // Sets Book Number of Ratings in View
    public void setBookRatingCount(String ratingCount)
    {
        // Deleted this
        /*TextView tv = (TextView) findViewById(R.id.custom_view_rating_text);
        tv.setText(getContext().getString(R.string.string_openParentheses)
            + ratingCount + getContext().getString(R.string.string_ratings)
            + getContext().getString(R.string.string_closeParentheses));
            */
    }
    
    
    // Sets Book Special Price in View
    public void setYourPrice(String yourPrice)
    {
        //TextView tv = (TextView) findViewById(R.id.badge_price_value);
        //tv.setText(getContext().getString(R.string.string_$) + yourPrice);
    }
    
    
    // Sets Book Cover in View from a bitmap
    public void setCoverViewFromBitmap(Bitmap coverBook)
    {
        ImageView iv = (ImageView) findViewById(R.id.custom_view_book_cover);
        iv.setImageBitmap(coverBook);
    }



    public void setRightMoveArrow(int position){
        Toast.makeText(getContext(),"BUTTON set",Toast.LENGTH_SHORT).show();
        ImageView arrow = (ImageView) findViewById(R.id.arrow);
        arrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(),"BUTTON CLICKED",Toast.LENGTH_SHORT).show();
            }
        });

    }
    
    // Sets Book Rating in View
    /*public void setRating(String rating)
    {
        //RatingBar rb = (RatingBar) findViewById(R.id.custom_view_rating);
        //rb.setRating(Float.parseFloat(rating));
    }
    */

    /**
     * Downloads and image from an Url specified as a paremeter returns the
     * array of bytes with the image Data for storing it on the Local Database
     */
    private byte[] downloadImage(final String imageUrl)
    {
        ByteArrayBuffer baf = null;

        try
        {
            URL url = new URL(imageUrl);
            URLConnection ucon = url.openConnection();
            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is, 128);
            baf = new ByteArrayBuffer(128);

            // get the bytes one by one
            int current = 0;
            while ((current = bis.read()) != -1)
            {
                baf.append((byte) current);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        if (baf == null)
        {
            return null;
        } else
        {
            return baf.toByteArray();
        }
    }
}
