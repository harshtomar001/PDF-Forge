package com.harsh.pdfstudio.model;

public class CardItem {


     int image;
     String title;
     String text;
     Class<?> activityClass;
     Boolean needIntent=false;

     String type;

     public CardItem(int image,String title ,String text,Class<?> activityClass) {
          this.image = image;
          this.title=title;
          this.text = text;
          this.activityClass=activityClass;

     }

     public Class<?> getActivityClass() {
          return activityClass;
     }

     public int getImage() {
          return image;
     }

     public String getText(){
          return text;
     }
     public String getTitle(){
          return title;
     }

     public void setNeedIntent(Boolean flag){
         this.needIntent=flag;
     }

     public  Boolean getNeedIntent(){
         return needIntent;
     }

     public void setType(String type){
         this.type=type;
     }

     public String getType(){
         return type;
     }

}
