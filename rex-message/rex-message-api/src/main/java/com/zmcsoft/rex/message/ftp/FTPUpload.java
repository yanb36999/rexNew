package com.zmcsoft.rex.message.ftp;

import java.io.InputStream;

public interface FTPUpload extends FTPMessage {
    String getFileName();

    InputStream getInput();
}
