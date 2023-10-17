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

    public int lastResponse = 0;

    public ExhentaiConnection() throws Exception {
        getPage("https://exhentai.org");
    }

    public HttpsURLConnection establishConnection(String pageUrl) throws Exception {
        Printer.printToLog("Establishing connection to " + pageUrl, Printer.LOGTYPE.DEBUG);
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

        lastResponse = con.getResponseCode();

        if(con.getResponseCode() != 200) {
            if(con.getResponseCode() == 404) return null;
            Printer.printToLog("Page request " + url.toString() + " returned \u001b[31;1m" + con.getResponseCode() + "\u001b[0m", Printer.LOGTYPE.DEBUG);
            Printer.printError("Request failed, request headers appear to be incorrect");
            System.exit(4);
        } else {
            Printer.printToLog("Page request " + url.toString() + " returned \u001b[32m" + con.getResponseCode() + "\u001b[0m", Printer.LOGTYPE.DEBUG);
        }
        return con;
    }

    public String getPage(String pageUrl) throws Exception {
            HttpsURLConnection con = establishConnection(pageUrl);
            if(con == null) return "";
            InputStream is = (InputStream) con.getContent();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            //System.out.println(decompress(buffer.toByteArray()));
            return decompress(buffer.toByteArray());
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
