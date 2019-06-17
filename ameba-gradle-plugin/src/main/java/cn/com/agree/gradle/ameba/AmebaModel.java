/**
 * Copyright 1993-2019 Agree Tech.
 * All rights reserved.
 */
package cn.com.agree.gradle.ameba;

/**
 *
 * @author PuYun pu.yun@agree.com.cn
 */
public class AmebaModel {

	private String gradleTxt;

	private String buildshipTxt;

	/**
	 * @return the buildshipTxt
	 */
	public String getBuildshipTxt() {
		return buildshipTxt==null?"connection.project.dir=..\r\neclipse.preferences.version=1":buildshipTxt;
	}

	/**
	 * @param buildshipTxt
	 *                         the buildshipTxt to set
	 */
	public void setBuildshipTxt(String buildshipTxt) {
		this.buildshipTxt = buildshipTxt;
	}

	/**
	 * @return the gradleTxt
	 */
	public String getGradleTxt() {
		return gradleTxt;
	}

	/**
	 * @param gradleTxt
	 *                      the gradleTxt to set
	 */
	public void setGradleTxt(String gradleTxt) {
		this.gradleTxt = gradleTxt;
	}

}
