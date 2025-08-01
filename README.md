# Подписание PDF-файлов с помощью КриптоПро + Java
## Research материалы
### В соответствии с ГОСТ Р 7.0.97 -2016 (стр. 13)
**Обязательные параметры для подтверждения юр. значимости:**
*  фраза "Документ подписан электронной подписью";
*  номер сертификата ключа электронной подписи;
*  ФИО владельца сертификата;
*  срок действия сертификата ключа сэлектронной подписи.

**Опционально:**
*  изображение герба;
*  эмблемы органа власти;
*  товарный знак (знак обслуживания) организации в соответствии с действующим законодательством.

Так же есть ГОСТ 34.10-2018 (Процессы формирования и проверки ЭЦП). ГОСТ включает в себя положения по математическим объектам и основные процессы.

### Стек
Использовалась версия Java 21
В качестве сертифицированного средства защиты информации от лицензированного разработчика применен криптографический провайдер КриптоПро CSP v5.0 и КриптоПро JCP v2.0.
После установки КриптоПро, провадер JCS или JCSP (в зависимости от того какой был выбран (по сложности установки одинково больно)) отображается в списке всех доступных в файле ../java/jdk-21.0.XXX/jre/lib/security/java.security. В данном файле можно настроить предпочтительные провайдер для использования по умолчанию, если явно не указано:

`security.provider.1=ru.CryptoPro.JCSP.JCSP`

Теперь немного извращений:

У платформы Java есть экспортные ограничения, связанные с тем, что мы с ними не дружим, поэтому наши провайдеры не доступны для использования в данной среде разработки. Для того, чтобы все работало необходимо их снять. 

Теперь еще глубже:

У разных версий Java есть сови нюансы, поэтому проще всего использовать версии 9 - самая свежая, там уже учтены моменты с ограничениями, для этого есть две папки *limited** и **unlimited* в ../java/jdk-21.0.XXX/jre/lib/security, соответственно объяснять не надо, что нужно использовать. Но есть один нюанс, в любом случае необходимо забрать к себе файлы *local_policy.jar* и *US_export_policy.jar*, они предоставлены по адресу [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) и заменить их в папке.

Для работы с PDF файлами использовал [iTextPDF](http://itextpdf.com/), такое решение было продиктовано провайдером КриптоПро.

В составе КриптоПро JCP поставляется файл Git patch для библиотеки iTextpdf dthcbb 5.1.3 (на самом деле можно использовать любую версию библиотеки, но патч я видел только у 3-х 5.1.3, 5.1.5, 5.1.13.4 (остальные тупо не работают с отчечественной криптографией)).
Собственно, этот патч адаптирует itextpdf к работе с провайдером КриптоПро. Для применения его - нужно скачать исходный код библиотеки версии 5.1.3 (можно найти хоть где), затем с помощью командной строки применяем патч:

`git apply --stat itextpdf_5.1.3.gost.user.patch`

<img width="772" height="114" alt="image" src="https://github.com/user-attachments/assets/0f2b0245-68d0-42ba-9edc-401660cca007" />

Далее нужно собрать полученную библиотеку из обновленного исходного кода и подключить к приложению (удачи).

### Проблемы сборки

После того как мы чудом обновили исходный код itextpdf, а потом еще и собрали это все во едино - в нем появляются зависимости на пакеты ru.CryptoPro.JCP и ru.CryptoPro.reprov.x509.

Без них проект с исходным кодом itextpdf_5.1.3.gost не соберется. А вот и ошибки (у меня их не ыбло, так что они были нагло украдены):
```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:2.3.2:compile (default-compile) on project itextpdf: Compilation failure: Compilation failure:

[ERROR] \github\iTextpdf_5.1.3_patched_cryptopro_bc1.50\src\main\java\com\itextpdf\text\pdf\PdfPKCS7.java:[138,23] error: package ru.CryptoPro.JCP does not exist

[ERROR] \github\iTextpdf_5.1.3_patched_cryptopro_bc1.50\src\main\java\com\itextpdf\text\pdf\PdfPKCS7.java:[139,31] error: package ru.CryptoPro.reprov.x509 does not exist 
```

Нужно взять из КриптоПро 2.0 файлы JCP.jar и JCPRevTools.jar и поместить их в каталог JRE, которую использует Maven. Само собой они должны быть и в classPath.

А теперь немного о моей боли. iText содержит зависимости Bouncy Castle версии 1.46, с помощью этой библиотеки реализуется криптопрвайдер и поддержка ASN.1 структур, а вот криптопро jcp 2.0 использует абсолютно ту же самую библиотеку, но версии 1.5. В итоге, при запуске кода мы получаем что-то типа такого:
```
Exception in thread "main" java.lang.NoClassDefFoundError: org/bouncycastle/asn1/DEREncodable

at com.itextpdf.text.pdf.PdfSigGenericPKCS.setSignInfo(PdfSigGenericPKCS.java:97)

at com.itextpdf.text.pdf.PdfSignatureAppearance.preClose(PdfSignatureAppearance.java:1003)

at com.itextpdf.text.pdf.PdfSignatureAppearance.preClose(PdfSignatureAppearance.java:904)

at com.itextpdf.text.pdf.PdfStamper.close(PdfStamper.java:194)

at ru.alfabank.ccjava.trustcore.logic.SignatureProcessor.pdfSignature(SignatureProcessor.java:965)

at ru.alfabank.ccjava.trustcore.logic.SignatureProcessor.main(SignatureProcessor.java:1363)

Caused by: java.lang.ClassNotFoundException: org.bouncycastle.asn1.DEREncodable

at java.net.URLClassLoader.findClass(Unknown Source)

at java.lang.ClassLoader.loadClass(Unknown Source)

at sun.misc.Launcher$AppClassLoader.loadClass(Unknown Source)

at java.lang.ClassLoader.loadClass(Unknown Source)

... 6 more
```

В общем, чтобы это починить надо менять зависимости и некоторый код в библиотеке itext, сильно углубляться не буду. Если будете что-то реализовывать от этого всего дела, то берите библиотеку, которая лежит тут, я там все пропатчил и исправил по гайдам. Кстати, немного о гайдах, я использовал несолько пока не дошел до нужного решения, они все неочень полные (по крайней мере, которые я нашел), но друг-друга дополняют.

Как итог, после выполнения всех этих действий itext начинает собирасться, конфликтов нет, КриптоПро и itext ссылаются на одну версию org.bouncycastle 1.50.

Еще нужно сказать, что при использовании Java 6, 7, 8 jcp нужно прям устанавливать, но если используете 9+ установка не требуется.

Если не очень хочется лезть в policy и security файлы после настройки проекта, но почему-то что-то не работает в коде присутствует закомментированный блок:

```java
for (Provider provider : Security.getProviders())
  System.out.println(provider);
  for (Provider.Service service : provider.getServices())
    if (service.getType().equals("Signature"))
      algorithms.add(service.getAlgorithm());
for (String algorithm : algorithms)
  System.out.println(algorithm);
```

Он выведет что-то типа такого (если смотреть только провайдеры):

<img width="179" height="195" alt="image" src="https://github.com/user-attachments/assets/f8135031-8374-4005-927a-f88932ad4dca" />

**На последок хочу предостеречь от использования vscode, лучше используйте IntelliJ IDEA**
