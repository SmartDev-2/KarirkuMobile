package com.tem2.karirku;

import java.io.Serializable;

public class Job implements Serializable {
    private String companyName;
    private String location;
    private String jobTitle;
    private String postedTime;
    private String applicants;
    private String tag1;
    private String tag2;
    private String tag3;
    private int idLowongan;
    private String deskripsi;
    private String kualifikasi;
    private String tipePekerjaan;
    private String gajiRange;
    private String modeKerja;
    private String benefit;
    private String noTelp;
    private String logoUrl;
    private String logoPath;
    private int idPerusahaan;


    // Field baru untuk data lamaran
    private String statusLamaran;
    private String catatanLamaran;
    private String cvUrl;
    private String tanggalLamaran;
    private int idLamaran;

    // Konstruktor utama
    public Job(String companyName, String location, String jobTitle,
               String postedTime, String applicants,
               String tag1, String tag2, String tag3) {
        this.companyName = companyName;
        this.location = location;
        this.jobTitle = jobTitle;
        this.postedTime = postedTime;
        this.applicants = applicants;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.noTelp = "";
        this.logoUrl = "";
        this.logoPath = "";
        this.idPerusahaan = 0;
        this.statusLamaran = "";
        this.catatanLamaran = "";
        this.cvUrl = "";
        this.tanggalLamaran = "";
        this.idLamaran = 0;
    }

    // Konstruktor lengkap untuk data dari API
    public Job(int idLowongan, String companyName, String location, String jobTitle,
               String postedTime, String applicants, String kategori, String tipePekerjaan,
               String gajiRange, String modeKerja, String deskripsi, String kualifikasi,
               String benefit, String noTelp, String logoUrl, String logoPath, int idPerusahaan) {
        this.idLowongan = idLowongan;
        this.companyName = companyName;
        this.location = location;
        this.jobTitle = jobTitle;
        this.postedTime = postedTime;
        this.applicants = applicants;
        this.tag1 = kategori;
        this.tag2 = tipePekerjaan;
        this.tag3 = modeKerja;
        this.deskripsi = deskripsi;
        this.kualifikasi = kualifikasi;
        this.tipePekerjaan = tipePekerjaan;
        this.gajiRange = gajiRange;
        this.modeKerja = modeKerja;
        this.benefit = benefit;
        this.noTelp = noTelp != null ? noTelp.trim() : "";
        this.logoUrl = logoUrl != null ? logoUrl : "";
        this.logoPath = logoPath != null ? logoPath : "";
        this.idPerusahaan = idPerusahaan;
        this.statusLamaran = "";
        this.catatanLamaran = "";
        this.cvUrl = "";
        this.tanggalLamaran = "";
        this.idLamaran = 0;
    }

    // Konstruktor lengkap dengan data lamaran
    public Job(int idLowongan, String companyName, String location, String jobTitle,
               String postedTime, String applicants, String kategori, String tipePekerjaan,
               String gajiRange, String modeKerja, String deskripsi, String kualifikasi,
               String benefit, String noTelp, String logoUrl, String logoPath, int idPerusahaan,
               String statusLamaran, String catatanLamaran, String cvUrl, String tanggalLamaran, int idLamaran) {
        this.idLowongan = idLowongan;
        this.companyName = companyName;
        this.location = location;
        this.jobTitle = jobTitle;
        this.postedTime = postedTime;
        this.applicants = applicants;
        this.tag1 = kategori;
        this.tag2 = tipePekerjaan;
        this.tag3 = modeKerja;
        this.deskripsi = deskripsi;
        this.kualifikasi = kualifikasi;
        this.tipePekerjaan = tipePekerjaan;
        this.gajiRange = gajiRange;
        this.modeKerja = modeKerja;
        this.benefit = benefit;
        this.noTelp = noTelp != null ? noTelp.trim() : "";
        this.logoUrl = logoUrl != null ? logoUrl : "";
        this.logoPath = logoPath != null ? logoPath : "";
        this.idPerusahaan = idPerusahaan;
        this.statusLamaran = statusLamaran != null ? statusLamaran : "";
        this.catatanLamaran = catatanLamaran != null ? catatanLamaran : "";
        this.cvUrl = cvUrl != null ? cvUrl : "";
        this.tanggalLamaran = tanggalLamaran != null ? tanggalLamaran : "";
        this.idLamaran = idLamaran;
    }

    // Getter dan Setter
    public String getCompanyName() { return companyName; }
    public String getLocation() { return location; }
    public String getJobTitle() { return jobTitle; }
    public String getPostedTime() { return postedTime; }
    public String getApplicants() { return applicants; }
    public String getTag1() { return tag1; }
    public String getTag2() { return tag2; }
    public String getTag3() { return tag3; }
    public int getIdLowongan() { return idLowongan; }
    public void setIdLowongan(int idLowongan) { this.idLowongan = idLowongan; }
    public String getDeskripsi() { return deskripsi; }
    public String getKualifikasi() { return kualifikasi; }
    public String getTipePekerjaan() { return tipePekerjaan; }
    public String getGajiRange() { return gajiRange; }
    public String getModeKerja() { return modeKerja; }
    public String getBenefit() { return benefit; }

    // Getter & Setter untuk noTelp
    public String getNoTelp() {
        return noTelp != null ? noTelp : "";
    }

    public void setNoTelp(String noTelp) {
        this.noTelp = noTelp != null ? noTelp : "";
    }

    // Getter dan Setter untuk logo
    public String getLogoUrl() {
        return logoUrl != null ? logoUrl : "";
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl != null ? logoUrl : "";
    }

    public String getLogoPath() {
        return logoPath != null ? logoPath : "";
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath != null ? logoPath : "";
    }

    public int getIdPerusahaan() {
        return idPerusahaan;
    }

    public void setIdPerusahaan(int idPerusahaan) {
        this.idPerusahaan = idPerusahaan;
    }

    // Getter dan Setter untuk data lamaran
    public String getStatusLamaran() {
        return statusLamaran != null ? statusLamaran : "";
    }

    public void setStatusLamaran(String statusLamaran) {
        this.statusLamaran = statusLamaran != null ? statusLamaran : "";
    }

    public String getCatatanLamaran() {
        return catatanLamaran != null ? catatanLamaran : "";
    }

    public void setCatatanLamaran(String catatanLamaran) {
        this.catatanLamaran = catatanLamaran != null ? catatanLamaran : "";
    }

    public String getCvUrl() {
        return cvUrl != null ? cvUrl : "";
    }

    public void setCvUrl(String cvUrl) {
        this.cvUrl = cvUrl != null ? cvUrl : "";
    }

    public String getTanggalLamaran() {
        return tanggalLamaran != null ? tanggalLamaran : "";
    }

    public void setTanggalLamaran(String tanggalLamaran) {
        this.tanggalLamaran = tanggalLamaran != null ? tanggalLamaran : "";
    }

    public int getIdLamaran() {
        return idLamaran;
    }

    public void setIdLamaran(int idLamaran) {
        this.idLamaran = idLamaran;
    }

    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setKualifikasi(String kualifikasi) { this.kualifikasi = kualifikasi; }
    public void setTipePekerjaan(String tipePekerjaan) { this.tipePekerjaan = tipePekerjaan; }
    public void setGajiRange(String gajiRange) { this.gajiRange = gajiRange; }
    public void setModeKerja(String modeKerja) { this.modeKerja = modeKerja; }
    public void setBenefit(String benefit) { this.benefit = benefit; }

    @Override
    public String toString() {
        return "Job{" +
                "idLowongan=" + idLowongan +
                ", companyName='" + companyName + '\'' +
                ", location='" + location + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", postedTime='" + postedTime + '\'' +
                ", applicants='" + applicants + '\'' +
                ", tag1='" + tag1 + '\'' +
                ", tag2='" + tag2 + '\'' +
                ", tag3='" + tag3 + '\'' +
                ", noTelp='" + noTelp + '\'' +
                ", logoUrl='" + logoUrl + '\'' +
                ", logoPath='" + logoPath + '\'' +
                ", idPerusahaan=" + idPerusahaan +
                ", statusLamaran='" + statusLamaran + '\'' +
                ", tanggalLamaran='" + tanggalLamaran + '\'' +
                ", idLamaran=" + idLamaran +
                '}';
    }
}