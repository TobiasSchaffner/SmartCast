/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hm.edu.model;

/**
 *
 * @author platypus
 */
public class Song {

    public String title;
    public String url;

    public Song(String title, String url) {
        this.title = title;
        this.url = url;
    }
    
    public String getTitle(){
        return title;
    }
    
    public String getUrl(){
        return url;
    }

}
