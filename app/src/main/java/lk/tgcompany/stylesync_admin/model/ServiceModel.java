package lk.tgcompany.stylesync_admin.model;

public class ServiceModel {
    private String serviceId;
    private String name;
    private String price;
    private String imageUrl;

    // Empty constructor for Firebase
    public ServiceModel() {}

    public ServiceModel(String serviceId, String name, String price, String imageUrl) {
        this.serviceId = serviceId;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}