package com.bob.android.testgaode;

/**
 * @package com.bob.android.testgaode
 * @fileName PositionEntity
 * @Author Bob on 2018/6/25 15:34.
 * @Describe TODO
 */

public class PositionEntity {
    @Override
    public String toString() {
        return "PositionEntity{" +
                "latitue=" + latitue +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
    public double latitue;
    public double longitude;
    public String address;
    public String city;
    public PositionEntity() {}
    public PositionEntity(double latitude, double longtitude, String address, String city) {
        this.latitue = latitude;
        this.longitude = longtitude;
        this.address = address;
        this.city = city;
    }

}
