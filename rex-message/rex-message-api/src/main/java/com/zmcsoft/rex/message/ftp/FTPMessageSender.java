package com.zmcsoft.rex.message.ftp;

import com.zmcsoft.rex.message.MessageSender;
import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Consumer;

public interface FTPMessageSender extends MessageSender {

    FTPMessageSender message(FTPMessage message);

    default FTPMessageSender delete(String fileName) {
        return message((FTPFileRemove) () -> fileName);
    }

    default FTPMessageSender rename(String source, String target) {
        return message(new FTPRename() {
            @Override
            public String getSource() {
                return source;
            }

            @Override
            public String getTargetName() {
                return target;
            }
        });
    }

    default FTPMessageSender download(String fileName, OutputStream outputStream) {
        return message(new FTPDownload() {
            @Override
            public String getFileName() {
                return fileName;
            }

            @Override
            public OutputStream getOutPut() {
                return outputStream;
            }

            @Override
            public String toString() {
                return "下载文件" + fileName;
            }
        });
    }


    default FTPMessageSender upload(String fileName, InputStream input) {
        return message(new FTPUpload() {
            @Override
            public String getFileName() {
                return fileName;
            }

            @Override
            public InputStream getInput() {
                return input;
            }

            @Override
            public String toString() {
                return input.toString();
            }
        });
    }

    default FTPMessageSender upload(String fileName, String text) {
        return message(new FTPUpload() {
            @Override
            public String getFileName() {
                return fileName;
            }

            @Override
            public InputStream getInput() {
                return new ByteArrayInputStream(text.getBytes());
            }

            @Override
            public String toString() {
                return text;
            }
        });
    }

    default FTPMessageSender list(String path, Consumer<FTPFile> consumer) {
        return message(new FTPFileIterable() {
            @Override
            public String getPath() {
                return path;
            }

            @Override
            public Consumer<FTPFile> getFileConsumer() {
                return consumer;
            }

            @Override
            public String toString() {
                return "遍历ftp文件:" + path;
            }
        });
    }

}
