package com.example.newsandlearn.Model;

import java.util.ArrayList;
import java.util.List;

public class Users {
    private String name = "";
    private String email = "";
    private String password = "";
    private String image = "";
    private String level = "";

    private Integer role = 0;

    private List<String> favorites = new ArrayList<>();

    public Users() {
        // Keeping empty constructor for Firestore
    }

    public Users(String name, String email, String password, String image, String level) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.image = image;
        this.level = level;
    }

    // Update your Getter for safety
    public String getName() {
        return name == null ? "" : name;
    }

    public String getImage() {
        // Return a default profile picture URL if image is null or empty
        if (image == null || image.isEmpty()) {
            return "https://your-default-image-url.com/default.png";
        }
        return image;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }



    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<String> favorites) {
        this.favorites = favorites;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
