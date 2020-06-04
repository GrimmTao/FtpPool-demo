/*******************************************************************************
 * Copyright (c) 2020, 2020 Hirain Technologies Corporation.
 ******************************************************************************/
package com.alex.demo.ftppool.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alex.demo.ftppool.pool.FTPServicePool;
import com.alex.demo.ftppool.service.FTPService;

/**
 * @Author Alex
 * @Created Dec 2020/6/4 18:30
 * @Description
 *              <p>
 *              使用例程
 */
@RestController
@RequestMapping("/ftppooltest")
public class FtpController {

	@Autowired
	private FTPServicePool ftpPool;

	@PostMapping("/user")
	public void use() throws Exception {
		FTPService ftpService = ftpPool.borrowObject();
		ftpService.connectFtpServer();
		/****************** 实现ftpService 各种接口功能 *****************/

		/************************************************************/
		ftpService.closeFTP();
		ftpPool.returnObject(ftpService);
	}
}
