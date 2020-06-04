/*******************************************************************************
 * Copyright (c) 2019, 2019 Hirain Technologies Corporation.
 ******************************************************************************/
package com.alex.demo.ftppool.pool;

import org.apache.commons.pool.PoolableObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alex.demo.ftppool.service.FTPService;
import com.alex.demo.ftppool.service.FTPServiceImpl;

/**
 * @Author Alex
 * @Created Jun 14, 2019 2:56:21 PM
 * @Description
 *              <p>
 */
@Component
@Configuration
public class FtpServiceFactory implements PoolableObjectFactory<FTPService> {

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
	 */
	@Override
	public FTPService makeObject() throws Exception {
		return getFtpService();
	}

	@Bean
	@Scope("prototype")
	public FTPService getFtpService() {
		return new FTPServiceImpl();
	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#destroyObject(Object)
	 */
	@Override
	public void destroyObject(FTPService obj) throws Exception {

	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#validateObject(Object)
	 */
	@Override
	public boolean validateObject(FTPService obj) {
		return true;
	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#activateObject(Object)
	 */
	@Override
	public void activateObject(FTPService obj) throws Exception {

	}

	/**
	 * @see org.apache.commons.pool.PoolableObjectFactory#passivateObject(Object)
	 */
	@Override
	public void passivateObject(FTPService obj) throws Exception {

	}

}
