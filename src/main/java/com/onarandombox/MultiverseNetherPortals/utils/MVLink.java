
package com.onarandombox.MultiverseNetherPortals.utils;


/**
 * Store Destionation info
 */
public class MVLink {

    protected String destination;
    protected Double x = null;
    protected Double y = null;
    protected Double z = null;
    
    public MVLink(String destination) {
        this.destination = destination;
    }
    
    public MVLink(String destination, Double x, Double y, Double z ) {
        this.destination = destination;
        if(x != 0.0 && y != 0.0 && z != 0.0){
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }

    public boolean hasCoordonate() {
        return this.x != null && this.x != 0.0 && this.y != null && this.y != 0.0 && this.z != null && this.z != 0.0 ;
    }
    
    

    
}
