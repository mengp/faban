/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testMain;

import com.sun.faban.driver.HttpTransport;
import com.sun.faban.driver.transport.htmlunit.ExtendedJSTransport;
import com.sun.faban.driver.transport.sunhttp.SunHttpTransport;
import java.io.IOException;

/**
 *
 * @author lmp
 */
public class testMain {

    public static void main(String[] args) throws IOException {
//        HttpTransport transport = new HttpTransport();
//        String url = "http://www.baidu.com/";
//        StringBuilder result =  transport.fetchURL(url);
//        System.out.println(result);
        testMain main = new testMain();
        //main.test2();
        // main.test3();
        main.test4();
    }

    public void test1() throws IOException {
        String url = "http://www.baidu.com/";
        HttpTransport.setProvider(
                "com.sun.faban.driver.transport.hc3.ApacheHC3Transport");
        HttpTransport http = HttpTransport.newInstance();
        int size = http.readURL(url);
        StringBuilder result = http.fetchURL(url);
        System.out.println(size);
        System.out.println(result);
    }

    public void test2() throws IOException {
        String url = "http://www.baidu.com/";
        // Faban提供两种连接server的方式，SunHttpTransport和ApacheHC3Transport，
        // 你使用哪种需要setProvider。
        // 而这两种方式提供自动timing。
        // 所以如果我希望提供js文件的执行和下载，其实可以提供另外一种Transport方式。
        HttpTransport.setProvider(
                "com.sun.faban.driver.transport.hc3.ApacheHC3Transport");
        HttpTransport http = HttpTransport.newInstance();
        int size = http.readURL(url);
        //StringBuilder result = http.fetchURLWithJS(url);
        System.out.println(size);
        //  System.out.println(result);
    }

    public void test3() throws IOException {
        String url = "http://www.google.com/";
        // Faban提供两种连接server的方式，SunHttpTransport和ApacheHC3Transport，
        // 你使用哪种需要setProvider。
        // 而这两种方式提供自动timing。
        // 所以如果我希望提供js文件的执行和下载，其实可以提供另外一种Transport方式。
        // HttpTransport.setProvider(
        //      "com.sun.faban.driver.transport.sunhttp.SunHttpTransport");
        HttpTransport http = HttpTransport.newInstance();
        http.fetchURL(url);
        int size = http.readURL(url);

        // StringBuilder result = http.fetchURLWithJS(url);
        System.out.println(size);
        // System.out.println(result);
    }

    public void test4() throws IOException {
        String url = "http://www.baidu.com/";
        // Faban提供两种连接server的方式，SunHttpTransport和ApacheHC3Transport，
        // 你使用哪种需要setProvider。
        // 而这两种方式提供自动timing。
        // 所以如果我希望提供js文件的执行和下载，其实可以提供另外一种Transport方式。
        HttpTransport.setProvider(
                "com.sun.faban.driver.transport.hc3.ApacheHC3Transport");
        HttpTransport http = HttpTransport.newInstance();
        StringBuilder builder0 = http.fetchURL(url);
        System.out.println("builder0:" + builder0);

        ExtendedJSTransport extend = new ExtendedJSTransport(http);

//        StringBuilder builder1 = extend.fetchURL(url);
//        System.out.println("builder1:" + builder1);
        
        StringBuilder builder2 = extend.fetchURLWithJS(url);
        System.out.println("builder2:" + builder2);
    }
}
