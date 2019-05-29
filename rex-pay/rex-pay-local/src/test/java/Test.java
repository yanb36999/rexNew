import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Test {
    public static void main(String[] args) {
        String input = args[0];
        String socketIp = "192.168.56.97";

        int port = 10016;
        input=input+'\n';
        byte[] data = input.getBytes();
        System.out.println(toHex(data));
        // 1、创建客户端Socket，指定服务器地址和端口
        try (Socket socket = new Socket()) {
            System.out.println(String.format("向socket%s:%s发送报文:\n%s", socketIp, port, input));
            //设置超时时间
            socket.connect(new InetSocketAddress(socketIp, port), 45000);
            try (OutputStream outputStream = socket.getOutputStream();//2.得到socket读写流
                 BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "GBK"))) {
                //3.利用流按照一定的操作，对socket进行读写操作
                outputStream.write(input.getBytes());
                outputStream.flush();
                //接收服务器的响应
                String result;
                String tmp;
                while ((tmp = br.readLine()) != null) {
                    result = tmp;
                    System.out.println(String.format("从socket%s:%s获取到报文:\n%s", socketIp, port, result));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static String toHex(byte[] data){
        char[] DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        int l = data.length;
        char[] out = new char[l << 1];
        int i = 0;

        for(int var5 = 0; i < l; ++i) {
            out[var5++] = DIGITS_LOWER[(240 & data[i]) >>> 4];
            out[var5++] = DIGITS_LOWER[15 & data[i]];
        }

        return new String(out);
    }
}

