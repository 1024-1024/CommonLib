package com.commonlib.dblib.qihoo.persistence;

import java.util.UUID;

public class AbsTableEntity {
	/*
	 * 主键
	 */
	@AbsTable.PrimaryKey
	String _id;
	
	/**
	 * 设置主键
	 */
	synchronized void generateId() {
		String uuid = UUID.randomUUID().toString();
		_id = uuid;
	}
	
	/**
	 * 获取主键
	 * 
	 * @return
	 */
	public String getId() {
		return _id;
	}
}
