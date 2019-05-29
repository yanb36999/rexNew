package com.zmcsoft.rex.message.ftp;

import java.io.OutputStream;

public interface FTPDownload extends FTPMessage{
    String getFileName();

    OutputStream getOutPut();
}
