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
            // Блок для просмотра доступных алгоритмов и провайдеров
            //System.out.println("Classpath: " + System.getProperty("java.class.path"));
            //System.out.println(System.getProperty("java.version"));
            //TreeSet<String> algorithms = new TreeSet<String>();
            //for (Provider provider : Security.getProviders())
            //    System.out.println(provider);
            //    for (Provider.Service service : provider.getServices())
            //        if (service.getType().equals("Signature"))
            //            algorithms.add(service.getAlgorithm());
            //for (String algorithm : algorithms)
            //    System.out.println(algorithm);

            String keyStorePassword = "<пароль от хранилища ключей>";
            String keyStorePath = "<путь до хранилища ключей>";
            String alias = "<алиас нужно подставить свой>";
            KeyStore keyStore = KeyStore.getInstance("<зависит от используемого провадера, можно посмотреть в расширении хранилища>");
            String keyPassword = "<пароль от ключа>";
            try (FileInputStream fis = new FileInputStream(keyStorePath)) {
                keyStore.load(fis, keyStorePassword.toCharArray());
            }
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keyPassword != null ? keyPassword.toCharArray() : keyStorePassword.toCharArray());

            Map<String, Object[]> signAttributesMap1 = new HashMap<>();
            signAttributesMap1.put(alias, new Object[]{cert, privateKey});

            String inputPdfPath = "<путь до директории с PDF-ками, которые нужно подписать>";
            String outputPdfPath = "<Путь до директории с подписаными PDF-ками>";

            byte[] pdfData = Files.readAllBytes(Paths.get(inputPdfPath));
            Rectangle signatureRectangle = new Rectangle(100, 100, 300, 200);
            String[] aliases = {"<alias1>", "<alias2>"};
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
