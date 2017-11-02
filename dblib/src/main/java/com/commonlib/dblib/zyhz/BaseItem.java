package com.commonlib.dblib.zyhz;

/**
 * 基础抽象bean
 * @version 1.0
 *
 */
public abstract class BaseItem {

	protected long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
