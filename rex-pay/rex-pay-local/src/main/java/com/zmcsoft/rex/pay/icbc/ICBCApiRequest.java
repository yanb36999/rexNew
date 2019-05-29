package com.zmcsoft.rex.pay.icbc;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.hswebframework.web.Maps;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ICBCApiRequest {

    private static final KeyStore trustStore;

    private static final char[] passwd = "p@ssw0rd".toCharArray();

    private PoolingHttpClientConnectionManager connManager;


    public static final Map<String, String> errorMsg = new HashMap<>();
    private HttpClient client;


    static {
        try {
            String keyStoreData = "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";

            trustStore = KeyStore.getInstance("jks");
//            Resource[] resources = (new PathMatchingResourcePatternResolver()).getResources("classpath*:/key/icbc/icbc-rex.jks");
//
//            InputStream jks = resources[0].getInputStream();
            trustStore.load(new ByteArrayInputStream(Base64.decodeBase64(keyStoreData.getBytes())), passwd);
        } catch (Exception e) {
            throw new RuntimeException("init key store error", e);
        }
        errorMsg.put("40972", "API查询的订单不存在");
        errorMsg.put("40973", "API查询过程中系统异常");
        errorMsg.put("40976", "API查询系统异常");
        errorMsg.put("40977", "商户证书信息错");
        errorMsg.put("40978", "解包商户请求数据报错");
        errorMsg.put("40979", "查询的订单不存在");
        errorMsg.put("40980", "API查询过程中系统异常");
        errorMsg.put("40981", "给商户打包返回数据错");
        errorMsg.put("40982", "系统错误");
        errorMsg.put("40983", "查询的订单不唯一");
        errorMsg.put("40987", "请求数据中接口名错误");
        errorMsg.put("40947", "商户代码或者商城账号有误");
        errorMsg.put("40948", "商城状态非法");
        errorMsg.put("40949", "商城类别非法");
        errorMsg.put("40950", "商城应用类别非法");
        errorMsg.put("40951", "商户证书id状态非法");
        errorMsg.put("40952", "商户证书id未绑定");
        errorMsg.put("40953", "商户id权限非法");
        errorMsg.put("40954", "检查商户状态时数据库异常");
        errorMsg.put("42022", "业务类型上送有误");
        errorMsg.put("42023", "商城种类上送有误");
        errorMsg.put("42020", "ID未开通汇总记账清单功能");
        errorMsg.put("42021", "汇总记账明细清单功能已到期");
        errorMsg.put("40990", "商户证书格式错误");
        errorMsg.put("41160", "商户未开通外卡支付业务");
        errorMsg.put("41161", "商户id对商城账号没有退货权限");
        errorMsg.put("41177", "外卡的当日退货必须为全额退货");
        errorMsg.put("26012", "找不到记录");
        errorMsg.put("26002", "数据库操作异常");
        errorMsg.put("26034", "退货交易重复提交");
        errorMsg.put("26036", "更新支付表记录失败");
        errorMsg.put("26042", "退货对应的支付订单未清算，不能退货");
    }

    @PostConstruct
    public void init() throws Exception {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        ConnectionSocketFactory plainSF = new PlainConnectionSocketFactory();
        registryBuilder.register("http", plainSF);
        SSLContext sslContext = SSLContexts.custom()
                .useProtocol("SSL")
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .loadKeyMaterial(trustStore, "p@ssw0rd".toCharArray())
                .build();
        SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        registryBuilder.register("https", sslSF);

        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        //设置连接管理器
        connManager = new PoolingHttpClientConnectionManager(registry);
        client = createClient();
    }

    protected HttpClient createClient() {
        return HttpClientBuilder.create()
                .setConnectionManager(connManager)
                .build();
    }

    public String doPost(String url, Map<String, String> parameters) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setConfig(RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .build());
        post.setEntity(createUrlEncodedFormEntity(parameters));

        HttpResponse response = client.execute(post);

        String responseString = StreamUtils.copyToString(response.getEntity().getContent(), Charset.forName("gbk"));
        return URLDecoder.decode(responseString, "gbk");
    }


    protected static UrlEncodedFormEntity createUrlEncodedFormEntity(Map<String, String> params) {
        List<NameValuePair> nameValuePair = params.entrySet()
                .stream().map(stringStringEntry ->
                        new BasicNameValuePair(stringStringEntry.getKey(), stringStringEntry.getValue()))
                .collect(Collectors.toList());
        try {
            return new UrlEncodedFormEntity(nameValuePair, "gbk");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {

        ICBCApiRequest request = new ICBCApiRequest();
        request.init();

        String xml = request.doPost("https://corporbank.icbc.com.cn:446/servlet/ICBCINBSEBusinessServlet", Maps.<String, String>buildMap()
                .put("APIName", "EAPI")
                .put("APIVersion", "001.001.002.001")
                .put("MerReqData", "<?xml  version=\"1.0\" encoding=\"GBK\" standalone=\"no\" ?>" +
                        "<ICBCAPI>" +
                        "<in>" +
                        "<orderNum>5101031455564477</orderNum>" +
                        "<tranDate>20171130</tranDate>" +
                        "<ShopCode>4402EE20210014</ShopCode>" +
                        "<ShopAccount>4402208011921001241</ShopAccount>" +
                        "</in>" +
                        "</ICBCAPI>")
                .get());
        System.out.println(xml);


    }
}
