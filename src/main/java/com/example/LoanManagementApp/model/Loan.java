package com.example.LoanManagementApp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String customerId;
    private String phone;
    private String address;
    private String loanDate;

    private String goldType;
    private String weight;
    private String goldPrice;
    private String ltv;
    private String interestRate;
    private String tenure;

    private double loanAmount;
    private double emi;
    private double totalInterest;
    private double totalAmount;
    
    private double paidAmount = 0.0;      // New
    private double remainingAmount = 0.0; // New
    private int totalEmis;                // New
    private int paidEmis = 0;            // New
    private int remainingEmis = 0;       // New
    private String nextEmiDate;          // New

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getLoanDate() {
        return loanDate;
    }
    public void setLoanDate(String loanDate) {
        this.loanDate = loanDate;
    }

    public String getGoldType() {
        return goldType;
    }
    public void setGoldType(String goldType) {
        this.goldType = goldType;
    }

    public String getWeight() {
        return weight;
    }
    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getGoldPrice() {
        return goldPrice;
    }
    public void setGoldPrice(String goldPrice) {
        this.goldPrice = goldPrice;
    }

    public String getLtv() {
        return ltv;
    }
    public void setLtv(String ltv) {
        this.ltv = ltv;
    }

    public String getInterestRate() {
        return interestRate;
    }
    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public String getTenure() {
        return tenure;
    }
    public void setTenure(String tenure) {
        this.tenure = tenure;
    }

    public double getLoanAmount() {
        return loanAmount;
    }
    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public double getEmi() {
        return emi;
    }
    public void setEmi(double emi) {
        this.emi = emi;
    }

    public double getTotalInterest() {
        return totalInterest;
    }
    public void setTotalInterest(double totalInterest) {
        this.totalInterest = totalInterest;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

	public double getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(double paidAmount) {
		this.paidAmount = paidAmount;
	}

	public double getRemainingAmount() {
		return remainingAmount;
	}

	public void setRemainingAmount(double remainingAmount) {
		this.remainingAmount = remainingAmount;
	}

	public int getTotalEmis() {
		return totalEmis;
	}

	public void setTotalEmis(int totalEmis) {
		this.totalEmis = totalEmis;
	}

	public int getPaidEmis() {
		return paidEmis;
	}

	public void setPaidEmis(int paidEmis) {
		this.paidEmis = paidEmis;
	}

	public int getRemainingEmis() {
		return remainingEmis;
	}

	public void setRemainingEmis(int remainingEmis) {
		this.remainingEmis = remainingEmis;
	}

	public String getNextEmiDate() {
		return nextEmiDate;
	}

	public void setNextEmiDate(String nextEmiDate) {
		this.nextEmiDate = nextEmiDate;
	}
    
}
