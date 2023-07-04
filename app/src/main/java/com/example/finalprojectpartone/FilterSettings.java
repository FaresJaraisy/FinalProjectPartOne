package com.example.finalprojectpartone;

public class FilterSettings {
    private String type, district, riskLevel, userId;


    public boolean isOrderByDate() { return orderByDate; }

    public void setOrderByDate(boolean orderByDate) { this.orderByDate = orderByDate; }

    private boolean orderByDate;

    public boolean isApprovedByMe() {
        return approvedByMe;
    }

    public void setApprovedByMe(boolean approvedByMe) {
        this.approvedByMe = approvedByMe;
    }

    private boolean approvedByMe;

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FilterSettings(String type, String district, String riskLevel,boolean orderByDate, boolean approvedByMe) {
        this.type = type;
        this.district = district;
        this.riskLevel = riskLevel;
        this.orderByDate = orderByDate;
        this.approvedByMe = approvedByMe;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
