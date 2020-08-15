/*******************************************************************************
 * Copyright (c) 2019, 2019 Alex.
 ******************************************************************************/
package com.alex.demo.ftppool.service;

import java.io.FilenameFilter;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPFile;

/**
 * @Author Alex
 * @Created Jun 13, 2019 3:45:36 PM
 * @Description
 *              <p>
 */
public interface FTPService {

	/**
	 * 连接并登陆FTP
	 */
	boolean connectFtpServer();

	/**
	 * 关闭FTP连接
	 */
	boolean closeFTP();

	/**
	 * 上传的文件
	 *
	 * @param remotePath
	 *            上传文件的路径地址（文件夹地址）
	 * @param localPath
	 *            本地文件的地址
	 * @throws IOException
	 *             异常
	 */
	boolean upload(String remotePath, String localPath) throws IOException;

	/**
	 * 删除pathname路径对应的文件
	 */
	boolean deleteFile(String pathname);

	/**
	 * 删除指定路径（directory）下的所有文件
	 */
	boolean delete(String directory);

	/**
	 * 文件下载
	 *
	 * @param remoteDir
	 *            ftp上文件所在文件夹
	 * @param localDirectoryPath
	 *            本地存放路径
	 * @param suffix
	 *            文件后缀名
	 */
	boolean download(String remoteDir, String localDirectoryPath, String suffix);

	/**
	 * @param remote
	 * @param filter
	 * @return
	 * @throws IOException
	 */
	FTPFile[] listFiles(String remote, FilenameFilter filter) throws IOException;

	/**
	 * @param remoteDir
	 * @param localDir
	 * @param filename
	 * @return
	 * @throws Exception
	 */
	boolean downloadFile(String remoteDir, String localDir, String filename) throws Exception;
}
