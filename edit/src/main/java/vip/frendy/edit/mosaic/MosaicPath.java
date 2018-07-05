package vip.frendy.edit.mosaic;

import android.graphics.Path;

/**
 * @author jarlen
 * @modified frendy
 */
public class MosaicPath {
	/**
	 * 绘画路径
	 */
	public Path drawPath;
	
	/**
	 * 绘画粗细
	 */
	public int paintWidth;

	/**
	 * 马赛克类型 Mosaic: 打码 erase: 橡皮擦
	 */
	public MosaicUtil.MosaicType type = MosaicUtil.MosaicType.MOSAIC;
}
