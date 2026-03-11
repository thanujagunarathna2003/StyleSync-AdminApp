package lk.tgcompany.stylesync_admin.model;

public class BookingModel {
    String documentId, serviceName, customerName, date, time, status, userId;
    double price;

    public BookingModel() {}

    public BookingModel(String documentId, String serviceName, String customerName, String date, String time, String status, String userId, double price) {
        this.documentId = documentId;
        this.serviceName = serviceName;
        this.customerName = customerName;
        this.date = date;
        this.time = time;
        this.status = status;
        this.userId = userId;
        this.price = price;
    }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}