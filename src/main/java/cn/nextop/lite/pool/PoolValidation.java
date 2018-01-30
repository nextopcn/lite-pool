/*
 * Copyright 2016-2018 Nextop Co.,Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nextop.lite.pool;

import cn.nextop.lite.pool.glossary.Copyable;

/**
 * 
 * @author Jingqi Xu
 */
public class PoolValidation implements Copyable<PoolValidation> {
	//
	public static final byte PULSE = (byte)(1 << 0);
	public static final byte ACQUIRE = (byte)(1 << 1);
	public static final byte RELEASE = (byte)(1 << 2);
	
	//
	private byte value;
	
	/**
	 * 
	 */
	public PoolValidation() {
		this((byte)0);
	}
	
	public PoolValidation(byte value) {
		this.value = value;
	}
	
	/**
	 * 
	 */
	public byte getValue() {
		return this.value;
	}
	
	@Override
	public final int hashCode() {
		return 629 + this.value;
	}
	
	@Override
	public PoolValidation copy() {
		return new PoolValidation(this.value);
	}
	
	@Override
	public String toString() {
		return Integer.toBinaryString(this.value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(!(obj instanceof PoolValidation)) return false;
		return this.value == ((PoolValidation) obj).value;
	}
	
	/**
	 * 
	 */
	public boolean isEnabled(byte type) {
		return (this.value & type) != 0;
	}
	
	public void setEnabled(byte type, boolean enabled) {
		if(enabled) this.value |= type; else this.value &= ~type;
	}
	
	public boolean isPulseEnabled() {
		return (this.value & PULSE) != 0;
	}
	
	public void setPulseEnabled(boolean enabled) {
		if(enabled) this.value |= PULSE; else this.value &= ~PULSE;
	}
	
	public boolean isAcquireEnabled() {
		return (this.value & ACQUIRE) != 0;
	}
	
	public void setAcquireEnabled(boolean enabled) {
		if(enabled) this.value |= ACQUIRE; else this.value &= ~ACQUIRE;
	}
	
	public boolean isReleaseEnabled() {
		return (this.value & RELEASE) != 0;
	}
	
	public void setReleaseEnabled(boolean enabled) {
		if(enabled) this.value |= RELEASE; else this.value &= ~RELEASE;
	}
}
