package exh;

import main.Main;
import util.Printer;
import util.Utility;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class ExhentaiConnection {

    public ExhentaiConnection() throws Exception {
        getPage("https://exhentai.org");
    }


    public String getPage(String pageUrl) throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509ExtendedTrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {

                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string, SSLEngine ssle) throws CertificateException {

                    }

                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        /*
         * end of the fix
         */

        HttpsURLConnection.setFollowRedirects(false);
        URL url = new URL(pageUrl);
        HttpsURLConnection con = (HttpsURLConnection)url.openConnection();
        setRequestProperties(con);

        String result = "";
        Printer.printToLog("Page request " + url.toString() + " returned " + con.getResponseCode(), Printer.LOGTYPE.DEBUG);
        if(con.getResponseCode() != 200) {
            Printer.printToLog("Request failed, request headers appear to be incorrect", Printer.LOGTYPE.ERROR);
        } else {
            InputStream is = (InputStream) con.getContent();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            //System.out.println(decompress(buffer.toByteArray()));
            result = decompress(buffer.toByteArray());
        }
        return result;
    }



    private void setRequestProperties(HttpsURLConnection con){
        for(Map.Entry<String, String> entry : Utility.reqHeaders.entrySet()){
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }


    private String decompress(byte[] bytes) throws Exception {
        StringBuilder outStr = new StringBuilder();
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            outStr.append(line);
        }
        return outStr.toString();
    }
}
