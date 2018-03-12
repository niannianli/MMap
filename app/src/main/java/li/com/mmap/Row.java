package li.com.mmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Row implements ClusterItem{
    int imageId;
    String name;
    LatLng geo;
    String address;
    
    Row(int imageId, String name, LatLng geo, String address) {
        this.imageId = imageId;
        this.name = name;
        this.geo = geo;
        this.address = address;
    }

    public int getImageId(){
        return imageId;
    }

    public String getName() {
        return name;
    }

    public LatLng getGeo() {
        return geo;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public LatLng getPosition() {
        return geo;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return address;
    }
}