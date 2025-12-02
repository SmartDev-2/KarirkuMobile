package com.tem2.karirku;

import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeywordMapper {

    // ðŸŽ¯ Mapping keyword CV ke kategori lowongan Supabase
    private static final Map<String, Set<String>> keywordMap = new HashMap<>();

    static {
        // Teknologi / IT
        Set<String> techKeywords = new HashSet<>();
        techKeywords.add("teknologi");
        techKeywords.add("technology");
        techKeywords.add("software");
        techKeywords.add("developer");
        techKeywords.add("programmer");
        techKeywords.add("IT");
        techKeywords.add("coding");
        techKeywords.add("engineer");
        techKeywords.add("frontend");
        techKeywords.add("backend");
        techKeywords.add("fullstack");
        techKeywords.add("web");
        techKeywords.add("mobile");
        techKeywords.add("android");
        techKeywords.add("java");
        techKeywords.add("python");
        keywordMap.put("Teknologi", techKeywords);

        // Desain
        Set<String> designKeywords = new HashSet<>();
        designKeywords.add("desain");
        designKeywords.add("design");
        designKeywords.add("UI");
        designKeywords.add("UX");
        designKeywords.add("graphic");
        designKeywords.add("creative");
        designKeywords.add("photoshop");
        designKeywords.add("illustrator");
        designKeywords.add("figma");
        keywordMap.put("Desain", designKeywords);

        // Marketing (tidak ada di list, tapi siapa tahu ada lowongan lain)
        Set<String> marketingKeywords = new HashSet<>();
        marketingKeywords.add("marketing");
        marketingKeywords.add("sales");
        marketingKeywords.add("promosi");
        marketingKeywords.add("digital marketing");
        marketingKeywords.add("social media");
        keywordMap.put("Marketing", marketingKeywords);

        // Finance / Keuangan
        Set<String> financeKeywords = new HashSet<>();
        financeKeywords.add("keuangan");
        financeKeywords.add("finance");
        financeKeywords.add("accounting");
        financeKeywords.add("akuntan");
        financeKeywords.add("pajak");
        financeKeywords.add("akuntansi");
        financeKeywords.add("financial");
        keywordMap.put("Keuangan", financeKeywords);

        // Perbankan
        Set<String> bankingKeywords = new HashSet<>();
        bankingKeywords.add("perbankan");
        bankingKeywords.add("bank");
        bankingKeywords.add("banking");
        bankingKeywords.add("teller");
        bankingKeywords.add("credit");
        bankingKeywords.add("loan");
        keywordMap.put("Perbankan", bankingKeywords);

        // Produksi / Hardware
        Set<String> productionKeywords = new HashSet<>();
        productionKeywords.add("produksi");
        productionKeywords.add("production");
        productionKeywords.add("hardware");
        productionKeywords.add("manufaktur");
        productionKeywords.add("operator");
        productionKeywords.add("pabrik");
        productionKeywords.add("quality control");
        keywordMap.put("Produksi", productionKeywords);

        // Admin / Administrasi
        Set<String> adminKeywords = new HashSet<>();
        adminKeywords.add("administrasi");
        adminKeywords.add("admin");
        adminKeywords.add("administrative");
        adminKeywords.add("sekretaris");
        adminKeywords.add("office");
        adminKeywords.add("data entry");
        keywordMap.put("Administrasi", adminKeywords);

        // Teknik
        Set<String> engineerKeywords = new HashSet<>();
        engineerKeywords.add("teknik");
        engineerKeywords.add("engineering");
        engineerKeywords.add("mekanik");
        engineerKeywords.add("elektro");
        engineerKeywords.add("sipil");
        engineerKeywords.add("mechanical");
        engineerKeywords.add("electrical");
        keywordMap.put("Teknik", engineerKeywords);

        // Pertanian
        Set<String> agricultureKeywords = new HashSet<>();
        agricultureKeywords.add("pertanian");
        agricultureKeywords.add("agriculture");
        agricultureKeywords.add("agronomi");
        agricultureKeywords.add("perkebunan");
        agricultureKeywords.add("farming");
        keywordMap.put("Pertanian", agricultureKeywords);

        // Pendidikan
        Set<String> educationKeywords = new HashSet<>();
        educationKeywords.add("pendidikan");
        educationKeywords.add("education");
        educationKeywords.add("guru");
        educationKeywords.add("teacher");
        educationKeywords.add("dosen");
        educationKeywords.add("pengajar");
        educationKeywords.add("training");
        keywordMap.put("Pendidikan", educationKeywords);
    }

    /**
     * Cek apakah keyword dari CV cocok dengan kategori lowongan
     */
    public static boolean isRelated(String cvKeyword, String jobField) {
        if (cvKeyword == null || jobField == null) return false;

        String cvLower = cvKeyword.toLowerCase().trim();
        String jobLower = jobField.toLowerCase().trim();

        // Jika string kosong atau "-"
        if (cvLower.isEmpty() || jobLower.isEmpty() || jobLower.equals("-")) {
            return false;
        }

        // 1. Cek exact match dulu
        if (jobLower.equals(cvLower) || jobLower.contains(cvLower) || cvLower.contains(jobLower)) {
            return true;
        }

        // 2. Cek via mapping - HARUS SAMA-SAMA ADA DI GRUP YANG SAMA
        for (Map.Entry<String, Set<String>> entry : keywordMap.entrySet()) {
            String mainCategory = entry.getKey(); // Misal: "Teknologi"
            Set<String> synonyms = entry.getValue(); // ["software", "developer", dll]

            boolean cvInThisGroup = false;
            boolean jobInThisGroup = false;

            // Cek apakah CV keyword ada di grup ini
            for (String syn : synonyms) {
                String synLower = syn.toLowerCase();
                if (cvLower.equals(synLower) || cvLower.contains(synLower) || synLower.contains(cvLower)) {
                    cvInThisGroup = true;
                    break;
                }
            }

            // Cek apakah job field ada di grup ini
            String mainCategoryLower = mainCategory.toLowerCase();
            if (jobLower.equals(mainCategoryLower) || jobLower.contains(mainCategoryLower)) {
                jobInThisGroup = true;
            } else {
                // Cek juga di synonyms
                for (String syn : synonyms) {
                    String synLower = syn.toLowerCase();
                    if (jobLower.equals(synLower) || jobLower.contains(synLower) || synLower.contains(jobLower)) {
                        jobInThisGroup = true;
                        break;
                    }
                }
            }

            // HANYA return true jika KEDUANYA ada di grup yang SAMA
            if (cvInThisGroup && jobInThisGroup) {
                return true;
            }
        }

        return false;
    }
}