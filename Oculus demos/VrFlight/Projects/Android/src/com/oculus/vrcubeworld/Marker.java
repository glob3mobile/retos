package com.oculus.vrcubeworld;

/**
 * Created by Chano on 21/11/2015.
 */
import org.glob3.mobile.generated.Geodetic2D;

public class Marker {

    private String name;
    private String icon;
    private Geodetic2D point;

    public Marker (String name, String icon, Geodetic2D point){
        this.name = name;
        //this.icon = icon;
        this.icon = "file:///bolita.png";
        this.point = point;
    }

    public String getName () { return name; }
    public String getIcon () { return icon; }
    public Geodetic2D getPoint() { return point; }
}