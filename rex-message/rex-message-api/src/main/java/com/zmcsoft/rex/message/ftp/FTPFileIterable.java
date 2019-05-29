package com.zmcsoft.rex.message.ftp;

import org.apache.commons.net.ftp.FTPFile;

import java.util.function.Consumer;

public interface FTPFileIterable extends FTPMessage {
    String getPath();

    Consumer<FTPFile> getFileConsumer();
}
