package com.harsh.pdfstudio.utils;

public class GenerateName {

    public static String generateUniqueName(String prefix){

        String uniqueNumber=(System.currentTimeMillis()+"").substring(6,13);

        return String.format("%s%s.pdf",prefix.toLowerCase(),uniqueNumber);

    }

}
