package com.harsh.pdfstudio.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.harsh.pdfstudio.model.AddImageObject;
import com.harsh.pdfstudio.utils.GenerateBitmapFromImgUri;

import java.io.IOException;
import java.util.List;

public class AddImageRenderer {

    private List<AddImageObject> addImageObjectList ;

    private Bitmap pdfBitmap;

    private  float scaleFactor;

    private Context context;

    private  Canvas canvas;

    public AddImageRenderer(Context context,Canvas canvas, List<AddImageObject> list, Bitmap bitmap){

        this.addImageObjectList = list;

        this.pdfBitmap = bitmap;

        this.scaleFactor = (float) bitmap.getWidth() / bitmap.getHeight() ;

        this.context = context;

        this.canvas = canvas;

    }



    public Bitmap renderImagesOnPages(){

        for(AddImageObject obj: addImageObjectList){



            Bitmap img = getImageBitmap(obj.getImageUri() , context );


            if(img == null ) {

                Toast.makeText(context , "Something went wrong" , Toast.LENGTH_LONG).show();
                continue;
            }

            RectF imgBounds = getBoundsForImage(obj.getBounds() , scaleFactor);


            float centerX = imgBounds.centerX();

            float centerY = imgBounds.centerY();

            canvas.save();

            canvas.rotate(obj.getRotation(),centerX,centerY);

            canvas.scale(
                    obj.isFlipHorizontal() ? -1 : 1 ,
                    obj.isFlipVertical() ? -1 : 1 ,
                    centerX,
                    centerY

            );

            canvas.drawBitmap(img,null,imgBounds,null);

            canvas.restore();




        }


        return  this.pdfBitmap;

    }

    private RectF getBoundsForImage(RectF imageBounds, float scaleFactor){

        return  new RectF(imageBounds.left / scaleFactor ,
                        imageBounds.top / scaleFactor ,
                        imageBounds.right / scaleFactor ,
                        imageBounds.bottom / scaleFactor);

    }

    private Bitmap getImageBitmap(Uri imageUri ,  Context context ){

        try {
            Bitmap bitmap = GenerateBitmapFromImgUri.generateBitmapFromUri(context,imageUri ,1f );

            return  bitmap;

        } catch (IOException e) {
            return null;
        }



    }



}