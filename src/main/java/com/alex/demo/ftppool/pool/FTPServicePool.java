/*******************************************************************************
 * Copyright (c) 2019, 2019 Alex.
 ******************************************************************************/
package com.alex.demo.ftppool.pool;

import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alex.demo.ftppool.service.FTPService;

/**
 * @Author Alex
 * @Created Jun 14, 2019 2:54:54 PM
 * @Description
 *              <p>
 *              FTPService的缓存池，为了支持并发
 */
@Component
public class FTPServicePool implements ObjectPool<FTPService> {

	private BlockingQueue<FTPService> pool;

	@Autowired
	private FtpServiceFactory factory;

	@Value("${synapsis.ftp.service.pool.size}")
	private int poolSize;

	@PostConstruct
	public void init() throws Exception {
		pool = new ArrayBlockingQueue<>(poolSize * 2);
		initPool(poolSize);
	}

	/**
	 * @param maxPoolSize
	 * @throws Exception
	 */
	private void initPool(int maxPoolSize) throws Exception {
		for (int i = 0; i < maxPoolSize; i++) {
			// 往池中添加对象
			addObject();
		}
	}

	/**
	 * 从池中借出一个对象
	 * 
	 * @see org.apache.commons.pool.ObjectPool#borrowObject()
	 */
	@Override
	public FTPService borrowObject() throws Exception, NoSuchElementException, IllegalStateException {
		FTPService ftpService = pool.take();
		if (ftpService == null) {
			// ftpService = factory.makeObject();
			addObject();
			ftpService = pool.take();
		} else if (!factory.validateObject(ftpService)) {// 验证不通过
			// 使对象在池中失效
			invalidateObject(ftpService);
			// 制造并添加新对象到池中
			// ftpService = factory.makeObject();
			addObject();
			ftpService = pool.take();
		}
		return ftpService;
	}

	/**
	 * 归还一个对象到池中
	 * 
	 * @see org.apache.commons.pool.ObjectPool#returnObject(Object)
	 */
	@Override
	public void returnObject(FTPService service) throws Exception {
		pool.offer(service);
		// if ((service != null) && !pool.offer(service, 3, TimeUnit.SECONDS)) {
		// try {
		// factory.destroyObject(service);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
	}

	/**
	 * @see org.apache.commons.pool.ObjectPool#invalidateObject(Object)
	 */
	@Override
	public void invalidateObject(FTPService service) throws Exception {
		// 移除无效的ftpservice
		pool.remove(service);
	}

	/**
	 * @see org.apache.commons.pool.ObjectPool#addObject()
	 */
	@Override
	public void addObject() throws Exception, IllegalStateException, UnsupportedOperationException {
		// 插入对象到队列
		pool.offer(factory.makeObject(), 3, TimeUnit.SECONDS);
	}

	/**
	 * @see org.apache.commons.pool.ObjectPool#getNumIdle()
	 */
	@Override
	public int getNumIdle() throws UnsupportedOperationException {
		return 0;
	}

	/**
	 * @see org.apache.commons.pool.ObjectPool#getNumActive()
	 */
	@Override
	public int getNumActive() throws UnsupportedOperationException {
		return 0;
	}

	/**
	 * @see org.apache.commons.pool.ObjectPool#clear()
	 */
	@Override
	public void clear() throws Exception, UnsupportedOperationException {
		if (pool != null) {
			pool.clear();
		}
	}

	/**
	 * @see org.apache.commons.pool.ObjectPool#close()
	 */
	@Override
	public void close() throws Exception {
		while (pool.iterator().hasNext()) {
			FTPService service = pool.take();
			factory.destroyObject(service);
		}
	}

	/**
	 * @see org.apache.commons.pool.ObjectPool#setFactory(org.apache.commons.pool.PoolableObjectFactory)
	 */
	@Override
	public void setFactory(PoolableObjectFactory<FTPService> factory) throws IllegalStateException, UnsupportedOperationException {

	}

}
