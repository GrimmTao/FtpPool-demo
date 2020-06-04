/*******************************************************************************
 * Copyright (c) 2019, 2019 Hirain Technologies Corporation.
 ******************************************************************************/
package com.alex.demo.ftppool.service;

import java.io.*;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Autowired;

import com.alex.demo.ftppool.pool.FtpProperties;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author Alex
 * @Created Jun 13, 2019 3:45:54 PM
 * @Description
 *              <p>
 */
@Slf4j
public class FTPServiceImpl implements FTPService {

	private FTPClient ftpClient;

	@Autowired
	private FtpProperties property;

	@Override
	public boolean connectFtpServer() {
		ftpClient = new FTPClient();
		ftpClient.setConnectTimeout(property.getTimeout());// 设置连接超时时间
		ftpClient.setControlEncoding(property.getEncoding());// 设置ftp字符集
		ftpClient.enterLocalPassiveMode();// 设置被动模式，文件传输端口设置
		try {
			ftpClient.connect(property.getHostname(), property.getPort());
			ftpClient.login(property.getUsername(), property.getPassword());
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);// 设置文件传输模式为二进制，可以保证传输的内容不会被改变
			int replyCode = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				log.error("connect ftp {} {} failed", property.getHostname(), property.getPort());
				ftpClient.disconnect();
				return false;
			}
			log.info("replyCode==========={}", replyCode);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("connect fail ------->>>{}", e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * @see com.alex.demo.ftppool.service.FTPService#closeFTP()
	 */
	@Override
	public boolean closeFTP() {
		try {
			if (ftpClient != null && ftpClient.isConnected()) {
				ftpClient.logout();
				ftpClient.disconnect();
			}
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public FTPFile[] listFiles(String remote, FilenameFilter filter) throws IOException {
		return ftpClient.listFiles(remote, file -> filter.accept(null, file.getName()));
	}

	@Override
	public boolean downloadFile(String remoteDir, String localDir, String filename) throws Exception {
		OutputStream outputStream = null;
		try {
			if (ftpClient == null) {
				return false;
			}
			ftpClient.changeWorkingDirectory(remoteDir);
			outputStream = new FileOutputStream(localDir + File.separator + filename);
			return ftpClient.retrieveFile(filename, outputStream);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	@Override
	public boolean download(String remoteDir, String localDirectoryPath, String suffix) {
		if (ftpClient == null) {
			return false;
		}
		OutputStream outputStream = null;
		try {
			ftpClient.changeWorkingDirectory(remoteDir);
			FTPFile[] ftpFiles = ftpClient.listFiles(remoteDir);
			File localDirectory = new File(localDirectoryPath);
			if (!localDirectory.exists()) {
				localDirectory.mkdirs();
			}
			boolean flag = false;
			boolean downloadSuccess = false;
			// 遍历当前目录下的文件，并下载后缀符合要求的文件
			for (FTPFile ftpFile : ftpFiles) {
				String fileName = ftpFile.getName();
				if (fileName.endsWith(suffix)) {
					flag = true;
					outputStream = new FileOutputStream(localDirectoryPath + System.getProperty("file.separator") + fileName);// 创建文件输出流
					downloadSuccess = ftpClient.retrieveFile(fileName, outputStream); // 下载文件
					if (!downloadSuccess) {
						log.error("download file 【{ }】 fail", fileName);
						return false;
					}
				}
			}
			if (!flag) {
				log.error("directory：{}下没有后缀 {} 的文件", remoteDir, suffix);
				return false;
			}
			log.info("download file success");
			ftpClient.logout();
		} catch (IOException e) {
			log.error("download file from 【{}】 fail ------->>>{}", remoteDir, e.getCause());
			return false;
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					log.error("outputStream close fail ------->>>{}", e.getCause());
				}
			}
		}
		return true;
	}

	@Override
	public boolean deleteFile(String pathname) {
		boolean result = false;
		try {
			result = ftpClient.deleteFile(pathname);
		} catch (IOException e) {
			return false;
		}
		return result;
	}

	@Override
	public boolean delete(String directory) {
		try {
			ftpClient.changeWorkingDirectory(directory);
			FTPFile[] ftpFiles = ftpClient.listFiles(directory);
			for (FTPFile file : ftpFiles) {
				ftpClient.dele(file.getName());
			}
			ftpClient.logout();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean upload(String remotePath, String localPath) throws IOException {
		// 进入被动模式
		ftpClient.enterLocalPassiveMode();
		// 以二进制进行传输数据
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		File localFile = new File(localPath);
		if (!localFile.exists()) {
			System.err.println("本地文件不存在");
			return false;
		}
		String fileName = localFile.getName();
		if (remotePath.contains("/")) {
			boolean isCreateOk = createDirectory(remotePath, ftpClient);
			if (!isCreateOk) {
				System.err.println("文件夹创建失败");
				return false;
			}
		}

		// 列出ftp服务器上的文件
		FTPFile[] ftpFiles = ftpClient.listFiles(remotePath);
		long remoteSize = 0l;
		String remoteFilePath = remotePath + "/" + fileName;
		if (ftpFiles.length > 0) {
			FTPFile mFtpFile = null;
			for (FTPFile ftpFile : ftpFiles) {
				if (ftpFile.getName().endsWith(fileName)) {
					mFtpFile = ftpFile;
					break;
				}
			}
			if (mFtpFile != null) {
				remoteSize = mFtpFile.getSize();
				if (remoteSize == localFile.length()) {
					System.err.println("文件已经上传成功");
					return true;
				}
				if (remoteSize > localFile.length()) {
					if (!ftpClient.deleteFile(remoteFilePath)) {
						System.err.println("服务端文件操作失败");
					} else {
						boolean isUpload = uploadFile(remoteFilePath, localFile, 0);
						System.err.println("是否上传成功：" + isUpload);
					}
					return true;
				}
				if (!uploadFile(remoteFilePath, localFile, remoteSize)) {
					System.err.println("文件上传成功");
					return true;
				} else {
					// 断点续传失败删除文件，重新上传
					if (!ftpClient.deleteFile(remoteFilePath)) {
						System.err.println("服务端文件操作失败");
					} else {
						boolean isUpload = uploadFile(remoteFilePath, localFile, 0);
						System.err.println("是否上传成功：" + isUpload);
					}
					return true;
				}
			}
		}
		boolean isUpload = uploadFile(remoteFilePath, localFile, remoteSize);
		System.err.println("是否上传成功：" + isUpload);
		return isUpload;
	}

	/**
	 * 上传文件
	 *
	 * @param remoteFile
	 *            包含文件名的地址
	 * @param localFile
	 *            本地文件
	 * @param remoteSize
	 *            服务端已经存在的文件大小
	 * @return 是否上传成功
	 * @throws IOException
	 */
	private boolean uploadFile(String remoteFile, File localFile, long remoteSize) throws IOException {
		long step = localFile.length() / 10;
		long process = 0;
		long readByteSize = 0;
		RandomAccessFile randomAccessFile = new RandomAccessFile(localFile, "r");
		OutputStream os = ftpClient.appendFileStream(remoteFile);
		if (remoteSize > 0) {
			// 已经上传一部分的时候就要进行断点续传
			process = remoteSize / step;
			readByteSize = remoteSize;
			randomAccessFile.seek(remoteSize);
			ftpClient.setRestartOffset(remoteSize);
		}
		byte[] buffers = new byte[1024];
		int len = -1;
		while ((len = randomAccessFile.read(buffers)) != -1) {
			os.write(buffers, 0, len);
			readByteSize += len;
			long newProcess = readByteSize / step;
			if (newProcess > process) {
				process = newProcess;
				System.err.println("当前上传进度为：" + process);
			}
		}
		os.flush();
		randomAccessFile.close();
		os.close();
		boolean result = ftpClient.completePendingCommand();
		return result;
	}

	/**
	 * 创建远程目录
	 *
	 * @param remote
	 *            远程目录
	 * @param ftpClient
	 *            ftp客户端
	 * @return 是否创建成功
	 * @throws IOException
	 */
	private boolean createDirectory(String remote, FTPClient ftpClient) throws IOException {
		String dirctory = remote.substring(0, remote.lastIndexOf("/") + 1);
		if (!dirctory.equalsIgnoreCase("/") && !ftpClient.changeWorkingDirectory(dirctory)) {
			int start = 0;
			int end = 0;
			if (dirctory.startsWith("/")) {
				start = 1;
			}
			end = dirctory.indexOf("/", start);
			while (true) {
				String subDirctory = remote.substring(start, end);
				if (!ftpClient.changeWorkingDirectory(subDirctory)) {
					if (ftpClient.makeDirectory(subDirctory)) {
						ftpClient.changeWorkingDirectory(subDirctory);
					} else {
						System.err.println("创建目录失败");
						return false;
					}
				}
				start = end + 1;
				end = dirctory.indexOf("/", start);
				if (end <= start) {
					break;
				}
			}
		}
		return true;
	}

}
