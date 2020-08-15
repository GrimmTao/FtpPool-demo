/*******************************************************************************
 * Copyright (c) 2019, 2019 Alex.
 ******************************************************************************/
package com.alex.demo.ftppool.pool;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * @Author Alex
 * @Created Sep 17, 2019 6:43:47 PM
 * @Description
 *              <p>
 */
@Component
@Data
public class FtpProperties {

	@Value("${ftppool.demo.ftp.hostname}")
	private String hostname;

	@Value("${ftppool.demo.ftp.port}")
	private int port = 21;

	@Value("${ftppool.demo.ftp.username}")
	private String username = "root";

	@Value("${ftppool.demo.ftp.password}")
	private String password = "123456";

	@Value("${ftppool.demo.ftp.timeout}")
	private Integer timeout = 5000;

	@Value("${ftppool.demo.ftp.encoding}")
	private String encoding = "UTF-8";

	@Value("${ftppool.demo.ftp.destPath}")
	private String destPath;
}
