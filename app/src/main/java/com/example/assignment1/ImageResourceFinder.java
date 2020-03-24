package com.example.assignment1;

import android.util.Log;

import java.lang.reflect.Field;

public class ImageResourceFinder {

    public static int FindImageResource(String name){
        /*----------------------------------------------------------------------------
        This method of getting a drawable based on its name found on
        http://daniel-codes.blogspot.com/2009/12/dynamically-retrieving-resources-in.html
        on 26/02-20
        ----------------------------------------------------------------------------*/
        try{
            //Get the entire class of drawables
            Class res = R.drawable.class;

            //Remove any extra characters, since there is one at the start of "Lion"
            //Trim() doesn't work for some reason
            name = name.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");;

            /*Get a single field of the drawables class -
            in this case an image -
            based on its name */
            Field field = res.getField(name);

            //Get the id of the image
            int drawableId = field.getInt(null);

            return drawableId;

        }
        catch (Exception e){
            Log.e("Get drawable", "Failed to get animal image", e);
            return 0;
        }
        //----------------------------------------------------------------------------

    }


}
