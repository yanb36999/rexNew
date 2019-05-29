package com.zmcsoft.rex.message.ftp;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface FTPRename extends FTPMessage {
    String getSource();

    String getTargetName();
}
