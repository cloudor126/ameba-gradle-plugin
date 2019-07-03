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

	private String buildshipTxt;

	private String gradleTxt;
	
	private boolean withPde;

	/**
	 * @return the buildshipTxt
	 */
	public String getBuildshipTxt() {
		return buildshipTxt==null?"connection.project.dir=..\r\neclipse.preferences.version=1":buildshipTxt;
	}

    /**
	 * @return the gradleTxt
	 */
	public String getGradleTxt() {
		return gradleTxt;
	}

    /**
     * @return the withPde
     */
    public boolean isWithPde()
    {
        return withPde;
    }

	/**
	 * @param buildshipTxt
	 *                         the buildshipTxt to set
	 */
	public void setBuildshipTxt(String buildshipTxt) {
		this.buildshipTxt = buildshipTxt;
	}

	/**
	 * @param gradleTxt
	 *                      the gradleTxt to set
	 */
	public void setGradleTxt(String gradleTxt) {
		this.gradleTxt = gradleTxt;
	}

	/**
     * @param withPde the withPde to set
     */
    public void setWithPde(boolean withPde)
    {
        this.withPde = withPde;
    }

}
