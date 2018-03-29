/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.android.gms.samples.vision.ocrreader.yandexpackage;

/**
 *
 * @author samiul_siddiqui
 */
public class Yand {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
         Translate.setKey("trnsl.1.1.20180324T123828Z.2b9ffa716760ad4e.d9995e9e7e47f773cdc06f3c7ad25139455efe1d");

        String translatedText = Translate.execute("Hola, amigo!", Language.SPANISH, Language.ENGLISH);

        System.out.println(translatedText);// TODO code application logic here
    }
    
}
