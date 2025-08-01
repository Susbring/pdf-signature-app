package ru.company.signature;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.itextpdf.text.Rectangle;

public class App 
{
    public static void main( String[] args )
    {
        try {
            System.out.println("Classpath: " + System.getProperty("java.class.path"));
            System.out.println(System.getProperty("java.version"));
            TreeSet<String> algorithms = new TreeSet<String>();

            // Блок для просмотра доступных алгоритмов и провайдеров
            //for (Provider provider : Security.getProviders())
            //    System.out.println(provider);
            //    for (Provider.Service service : provider.getServices())
            //        if (service.getType().equals("Signature"))
            //            algorithms.add(service.getAlgorithm());
            //for (String algorithm : algorithms)
            //    System.out.println(algorithm);

            String keyStorePassword = "Uffhfuffhf1";
            String keyStorePath = "C:/Dev/test.pfx";
            String alias = "ab89250d-d856-43ec-80a2-3d2c5b293136";
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            String keyPassword = "Uffhfuffhf1";
            try (FileInputStream fis = new FileInputStream(keyStorePath)) {
                keyStore.load(fis, keyStorePassword.toCharArray());
            }
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword != null ? keyPassword.toCharArray() : keyStorePassword.toCharArray());

            Map<String, Object[]> signAttributesMap1 = new HashMap<>();
            signAttributesMap1.put(alias, new Object[]{cert, privateKey});

            String inputPdfPath = "C:/Dev/test.pdf";
            String outputPdfPath = "C:/Dev/test/test_out.pdf";

            byte[] pdfData = Files.readAllBytes(Paths.get(inputPdfPath));
            Rectangle signatureRectangle = new Rectangle(100, 100, 300, 200);
            String[] aliases = {"ab89250d-d856-43ec-80a2-3d2c5b293136"};
            byte[] signedPdfData = SignatureProcessor.samplePDFSignature(
                aliases,
                pdfData,
                '0',
                signatureRectangle,
                1,
                0,
                signAttributesMap1
            );
            try (FileOutputStream fos = new FileOutputStream(outputPdfPath)) {
                fos.write(signedPdfData);
            }
            System.out.println("Документ успешно подписан и сохранен: " + outputPdfPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
