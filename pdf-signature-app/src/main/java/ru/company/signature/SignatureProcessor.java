package ru.company.signature;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;


import com.itextpdf.text.Rectangle;
import com.itextpdf.text.Image;
import ru.CryptoPro.JCP.JCP;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public class SignatureProcessor {
    private static final String FILE_PATH = "C:/Dev/7680x4320-white-solid-color-background.jpg";  // Замените на актуальный путь
    private static final String INNER_CA = "ab89250d-d856-43ec-80a2-3d2c5b293136"; // Замените на актуальный alias

    /**
     * Подписывает PDF документ с использованием нескольких ЭЦП.
     *
     * @param aliases      - Имена контейнеров с ключами ЭП.
     * @param data         - Массив байтов с документом PDF.
     * @param pdfVersion   - Номер версии формата PDF.
     * @param signatureRectangle - Прямоугольник, определяющий местоположение подписи.
     * @param signaturePageNumber - Номер страницы, на которой будет размещена подпись.
     * @param signatureMode - Режим подписи (например, PdfSignatureAppearance.CRYPTOPRO_SIGNED).
     * @return Подписанный PDF документ в виде массива байтов.
     */
    public static byte[] samplePDFSignature(String[] aliases, byte[] data, char pdfVersion,
                                              Rectangle signatureRectangle, int signaturePageNumber,
                                              int signatureMode, Map<String, Object[]> Map){

        HashMap<X509Certificate, PrivateKey> currSignAttrMap = new HashMap<X509Certificate, PrivateKey>();

        for (String alias : aliases) {
            X509Certificate certificate = (X509Certificate) Map.get(alias)[0];
            PrivateKey privateKey = (PrivateKey) Map.get(alias)[1];
            
            currSignAttrMap.put(certificate, privateKey);
        }

        byte[] signedPdfData = data; // Инициализируем signedPdfData с исходными данными
        PdfReader reader = null;
        PdfStamper stp = null;
        ByteArrayOutputStream bais = new ByteArrayOutputStream(); 

        try {
            // Читаем изображение только один раз
            byte[] imageBytes = Files.readAllBytes(Paths.get(FILE_PATH));

            X509Certificate innerCA = obtainCertFromTrustStoreJKS(false, INNER_CA);
            System.out.println(innerCA);
            for (Entry<X509Certificate, PrivateKey> entry : currSignAttrMap.entrySet()) {

                try {
                    reader = new PdfReader(signedPdfData);  // Initialize inside the loop
                    stp = PdfStamper.createSignature(reader, bais, pdfVersion);
                    PdfSignatureAppearance sap = stp.getSignatureAppearance();
                    sap.setReason("Подписание ГОСТ-сертификатом");
                    sap.setLocation("Москва");
                    sap.setContact("email@example.ru");
                    sap.setProvider("JCP");
                    PrivateKey pk = entry.getValue();
                    Certificate[] certChain = new Certificate[]{entry.getKey(), innerCA};
                    

                    sap.setCrypto(pk, certChain, null, PdfSignatureAppearance.CRYPTOPRO_SIGNED);

                    Image image = Image.getInstance(imageBytes);
                    sap.setImage(image);
                    sap.setVisibleSignature(signatureRectangle, signaturePageNumber, "");

                    signedPdfData = bais.toByteArray();  // Обновляем signedPdfData
                    bais.reset();  // Сброс ByteArrayOutputStream
                } catch (Exception e) {
                    // Обработка исключения
                    System.err.println("Ошибка при чтении файла: " + e.getMessage());
                } finally {
                    if (stp != null) {
                        try {
                            stp.close();
                        } catch (Exception ex) {
                            System.err.println("Ошибка при чтении файла: " + ex.getMessage());
                        }
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception ex) {
                            System.err.println("Error closing PdfReader: " + ex.getMessage());
                        }
                    }
                }
            }
            return signedPdfData;  // Возвращаем конечный подписанный PDF
        } catch (Exception ex) {
            System.err.println("Error closing ByteArrayOutputStream: " + ex.getMessage());
        } finally {
            try {
                bais.close();
            } catch (IOException ex) {
                System.err.println("Error closing ByteArrayOutputStream: " + ex.getMessage());
            }
        }
        return signedPdfData;
    }

    private static X509Certificate obtainCertFromTrustStoreJKS(boolean b, String innerCa) {
        // TODO: Implement this method.  This is a stub.
        return null;
    }
}