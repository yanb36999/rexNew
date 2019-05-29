package com.zmcsoft.rex.message.ftp;

import com.zmcsoft.rex.message.AbstractMessageSenderProvider;
import com.zmcsoft.rex.message.exception.MessageException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class FTPMessageSenderProvider extends AbstractMessageSenderProvider<FTPMessageSender> {

    private ObjectPool<FTPClient> pool;

    public FTPMessageSenderProvider(ObjectPool<FTPClient> pool) {
        this.pool = pool;
    }

    protected Logger logger = LoggerFactory.getLogger("FTPMessageSenderProvider");

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private FTPClient init() {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            throw new MessageException(e.getMessage(), e);
        }
    }

    private void destroy(FTPClient client) {
        try {
            if (null != client) {
                pool.returnObject(client);
            }
        } catch (Exception e) {
            logger.error("returnObject error : {}", client);
        }
    }

    private MessageExecutor<FTPRename> rename = (request, msg) -> {
        try {
            return request.rename(msg.getSource(), msg.getTargetName());
        } catch (IOException e) {
            throw new MessageException(e.getMessage(), e);
        }
    };

    private MessageExecutor<FTPFileRemove> remover = (request, msg) -> {
        try {
           return request.deleteFile(msg.getFileName());
        } catch (IOException e) {
            throw new MessageException(e.getMessage(), e);
        }
    };

    private MessageExecutor<FTPDownload> downloader = (request, msg) -> {
        try {
            return request.retrieveFile(msg.getFileName(), msg.getOutPut());

        } catch (IOException e) {
            throw new MessageException(e.getMessage(), e);
        }
    };

    private MessageExecutor<FTPUpload> uploader = (request, msg) -> {
        try {
            return request.storeFile(msg.getFileName(), msg.getInput());
        } catch (IOException e) {
            throw new MessageException(e.getMessage(), e);
        }
    };


    private MessageExecutor<FTPFileIterable> iterable = (request, msg) -> {
        try {
            FTPFile[] ftpFiles = request.listFiles(msg.getPath());
            Consumer<FTPFile> ftpFileConsumer = msg.getFileConsumer();
            for (FTPFile ftpFile : ftpFiles) {
                ftpFileConsumer.accept(ftpFile);
            }
            return true;
        } catch (IOException e) {
            throw new MessageException(e.getMessage(), e);
        }
    };


    @Override
    public FTPMessageSender create() {
        return new FTPMessageSender() {
            List<Function<FTPClient, Boolean>> executors = new ArrayList<>();

            @Override
            public FTPMessageSender message(FTPMessage message) {
                Function<FTPClient, Boolean> executor;

                if (message instanceof FTPUpload) {
                    executor = (request) -> {
                        FTPUpload ftpUpload = (FTPUpload) message;
                        logger.debug("开始执行ftp文件上传:{} payload:\n{}", ftpUpload.getFileName(), message);
                        boolean success = uploader.execute(request, (FTPUpload) message);
                        logger.debug("执行ftp文件上传:{} {}", ftpUpload.getFileName(), success);
                        return success;
                    };
                } else if (message instanceof FTPDownload) {
                    executor = (request) -> downloader.execute(request, ((FTPDownload) message));
                } else if (message instanceof FTPRename) {
                    executor = (request) -> rename.execute(request, ((FTPRename) message));
                } else if (message instanceof FTPFileIterable) {
                    executor = (request) -> iterable.execute(request, ((FTPFileIterable) message));
                } else if (message instanceof FTPFileRemove) {
                    executor = (request) -> remover.execute(request, ((FTPFileRemove) message));
                } else {
                    throw new UnsupportedOperationException("type " + (message.getClass()) + " not support yet");
                }
                executors.add(executor);
                return this;
            }

            @Override
            public boolean send() {
                FTPClient client = init();
                try {
                    //logger.info("开始发送ftp消息,数量:{}",executors.size());
                    return executors
                            .stream()
                            .map(call -> call.apply(client))
                            .allMatch(Boolean.TRUE::equals);

                } finally {
                    destroy(client);
                }
            }
        };
    }


    interface MessageExecutor<T extends FTPMessage> {
        boolean execute(FTPClient request, T msg);
    }

}
