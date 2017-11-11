package main.cs5340.topaz_turtles;

import java.util.ArrayList;

public class Weapon {
    private ArrayList<String> weapons;

    public Weapon(String[] arr){
        weapons = new ArrayList<String>();
        for(String s : arr){
            weapons.add(s);
        }
    }

    public ArrayList<String> getWeapons() {
        return weapons;
    }
}
