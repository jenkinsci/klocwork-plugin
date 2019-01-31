package com.emenda.klocwork.util;

import com.emenda.klocwork.KlocworkConstants;
import jenkins.security.MasterToSlaveCallable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;

public class KlocworkLtokenFetcher extends MasterToSlaveCallable<String[],IOException>  {

    private String url;
    private String ltoken;

    public KlocworkLtokenFetcher(String url, String ltoken) {
        this.url = url;
        this.ltoken = ltoken;
    }

    public String[] call() throws IOException {
        URL urlObj = new URL(url);
        String host = urlObj.getHost();
        String port = Integer.toString(urlObj.getPort());
        File ltokenFile = null;

        if (StringUtils.isEmpty(ltoken)) {
            ltokenFile = new File(System.getProperty("user.home") + File.separator +
                 ".klocwork" + File.separator + "ltoken");
        } else {
            ltokenFile = new File(ltoken);
        }


         if (!ltokenFile.exists()) {
             throw new IOException("Error: could not find Klocwork ltoken at \"" +
                 ltokenFile.getAbsolutePath() + "\"");
         }
         String[] splitLine;
         try(Scanner scanner =  new Scanner(ltokenFile)) {
             while (scanner.hasNextLine()) {
                 splitLine = scanner.nextLine().split(KlocworkConstants.LTOKEN_SEPARATOR);
                 if (isCorrectLtokenLine(splitLine, host, port)) {
                     return splitLine;
                 }
             }
         }

         // if we reach this point, we could not find the correct ltoken entry...
         // throw exception to indicate error
         throw new IOException("Error: could not find a matching ltoken entry in " +
             "\"" + ltokenFile.getAbsolutePath() + "\" for host \"" + host +
             " and port \"" + port);
    }

    private boolean isCorrectLtokenLine(String[] splitLine, String host, String port) {
        if (splitLine.length == 4 &&
                    verifyHostAddress(splitLine[KlocworkConstants.LTOKEN_HOST_INDEX],host) &&
                    splitLine[KlocworkConstants.LTOKEN_PORT_INDEX].equals(port)) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Function to determine if two hosts are equivalent by resolving their absolute addresses
     */
    private boolean verifyHostAddress(String hostA, String hostB) {
        try {
            InetAddress addressA = InetAddress.getByName(hostA);
            InetAddress addressB = InetAddress.getByName(hostB);
            if (addressA.equals(addressB)) {
              return true;
            } else {
              return false;
            }
        } catch (UnknownHostException ex) {
            return false;
        }
    }
}
